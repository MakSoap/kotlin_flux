package com.example.demo;

import com.example.demo.service.CurrencyCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.stream.IntStream;

import static com.example.demo.model.Currency.getRandomCurrency;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class StreamsToFluxesDemoApplication implements CommandLineRunner {

    private final CurrencyCalculator currencyCalculator;

    public static void main(String[] args) {
        SpringApplication.run(StreamsToFluxesDemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        var start = Instant.now();
        var random = new Random();

        IntStream.range(1, 1000)
                .map(__ -> random.nextInt(100, 1000))
                .mapToObj(BigDecimal::new)
                .map(rub -> currencyCalculator.getRates(getRandomCurrency(), rub))
                .peek(result -> log.info("\u001b[32m Conversion result {} \u001b[0m", result))
                .toList();


        var end = Instant.now();
        log.info("Time {}", Duration.between(start, end).toSeconds());
    }

}