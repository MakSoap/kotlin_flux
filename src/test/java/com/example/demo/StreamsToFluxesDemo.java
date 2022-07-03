package com.example.demo;

import com.example.demo.service.CurrencyCalculator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.example.demo.model.Currency.getRandomCurrency;
import static com.example.demo.util.AsciiHelper.*;

@Slf4j
@SpringBootTest(classes = StreamsToFluxesDemoApplication.class)
public class StreamsToFluxesDemo {

    private final Random random = new Random();
    @Autowired
    CurrencyCalculator currencyCalculator;

    @Test
    void SimpleStreamTest() {
        long before = System.currentTimeMillis();
        IntStream.range(1, 300)
                .parallel()
                .map(__ -> random.nextInt(100, 1000))
                .mapToObj(BigDecimal::new)
                .map(rub -> currencyCalculator.getRates(getRandomCurrency(), rub))
                .peek(result -> log.info(GREEN.getCode() + "Conversion result {}" + RESET.getCode(), result))
                .toList();
        System.out.println(System.currentTimeMillis() - before);
    }

    @Test
    void TransformToFlux() throws Exception {
        IntStream.range(1, 1000)
                .map(__ -> random.nextInt(100, 1000))
                .mapToObj(BigDecimal::new)
                .forEach(rub -> currencyCalculator.requestForRates(getRandomCurrency(), rub));

        currencyCalculator.getRatesFlux()
                .subscribe(result -> log.info(GREEN.getCode() + "Conversion result {}" + RESET.getCode(), result));

        Thread.sleep(15000); // Necessary for test only
    }


    @Test
    void AdvancedSubscribe() throws Exception {
        IntStream.range(1, 10)
                .map(__ -> random.nextInt(100, 1000))
                .mapToObj(BigDecimal::new)
                .forEach(rub -> currencyCalculator.requestForRates(getRandomCurrency(), rub));

        currencyCalculator.complete();

        currencyCalculator.getRatesFlux()
                .subscribe(result -> log.info(GREEN.getCode() + "Conversion result {}" + RESET.getCode(), result),
                        err -> log.error(RED.getCode() + "Some Error" + RESET.getCode(), err),
                        () -> log.info("Completed!"));

        Thread.sleep(1000); // Necessary for test only
    }

    @Test
    void ReadFileAsStreamDemo() throws Exception {
        Files.lines(Path.of("in.txt"))
                .parallel()
                .map(rub -> currencyCalculator.getRates(getRandomCurrency(), new BigDecimal(rub)))
                .peek(result -> log.info(GREEN.getCode() + "Conversion result {}" + RESET.getCode(), result))
                .toList();
    }

    @Test
    void ReadFileAsFluxDemo() throws Exception {
        Flux.using(() -> Files.lines(Path.of("in.txt")), Flux::fromStream, Stream::close)
                .subscribeOn(Schedulers.newParallel("file-copy", 3))
                .flatMap(rub -> currencyCalculator.getRatesMono(getRandomCurrency(), new BigDecimal(rub)))
                .subscribe(result -> log.info(GREEN.getCode() + "Conversion result {}" + RESET.getCode(), result),
                        err -> log.error(RED.getCode() + "Some Error" + RESET.getCode(), err),
                        () -> log.info("Completed!"));


        Thread.sleep(15000); // Necessary for test only
    }
}
