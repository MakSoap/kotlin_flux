package com.example.demo

import com.example.demo.model.Currency
import com.example.demo.service.CurrencyCalculator
import com.example.demo.util.AsciiHelper
import lombok.extern.slf4j.Slf4j
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.stream.IntStream
import java.util.stream.Stream


@Slf4j
@SpringBootTest(classes = [StreamsToFluxesDemoApplication::class])
class KotlinStreamToFluxesDemo {

    private val log = LoggerFactory.getLogger(StreamsToFluxesDemo::class.java)

    private val random = Random()

    @Autowired
    var currencyCalculator: CurrencyCalculator? = null

    @Test
    fun SimpleStreamTest() {
        IntStream.range(1, 300)
            .parallel()
            .map { _: Int -> random.nextInt(100, 1000) }
            .mapToObj { `val`: Int -> BigDecimal(`val`) }
            .map { rub: BigDecimal? ->
                currencyCalculator!!.getRates(
                    Currency.getRandomCurrency(),
                    rub
                )
            }
            .peek { result: BigDecimal? ->
                log.info(
                    AsciiHelper.GREEN.code + "Conversion result {}" + AsciiHelper.RESET.code,
                    result
                )
            }
            .toList()
    }

    @Test
    @Throws(Exception::class)
    fun TransformToFlux() {
        IntStream.range(1, 1000)
            .map { _: Int -> random.nextInt(100, 1000) }
            .mapToObj { `val`: Int -> BigDecimal(`val`) }
            .forEach { rub: BigDecimal? ->
                currencyCalculator!!.requestForRates(
                    Currency.getRandomCurrency(),
                    rub
                )
            }
        currencyCalculator!!.ratesFlux
            .subscribe { result: BigDecimal? ->
                log.info(
                    AsciiHelper.GREEN.code + "Conversion result {}" + AsciiHelper.RESET.code,
                    result
                )
            }
        Thread.sleep(15000) // Necessary for test only
    }

    @Test
    @Throws(Exception::class)
    fun AdvancedSubscribe() {
        IntStream.range(1, 10)
            .map { _: Int -> random.nextInt(100, 1000) }
            .mapToObj { `val`: Int -> BigDecimal(`val`) }
            .forEach { rub: BigDecimal? ->
                currencyCalculator!!.requestForRates(
                    Currency.getRandomCurrency(),
                    rub
                )
            }
        currencyCalculator!!.complete()
        currencyCalculator!!.ratesFlux
            .subscribe(
                { result: BigDecimal? ->
                    log.info(
                        AsciiHelper.GREEN.code + "Conversion result {}" + AsciiHelper.RESET.code,
                        result
                    )
                },
                { err: Throwable? ->
                    log.error(
                        AsciiHelper.RED.code + "Some Error" + AsciiHelper.RESET.code,
                        err
                    )
                }
            ) { log.info("Completed!") }
        Thread.sleep(1000) // Necessary for test only
    }

    @Test
    @Throws(Exception::class)
    fun ReadFileAsStreamDemo() {
        Files.lines(Path.of("in.txt"))
            .parallel()
            .map { rub: String? ->
                currencyCalculator!!.getRates(
                    Currency.getRandomCurrency(),
                    BigDecimal(rub)
                )
            }
            .peek { result: BigDecimal? ->
                log.info(
                    AsciiHelper.GREEN.code + "Conversion result {}" + AsciiHelper.RESET.code,
                    result
                )
            }
            .toList()
    }

    @Test
    @Throws(Exception::class)
    fun ReadFileAsFluxDemo() {
        Flux.using(
            { Files.lines(Path.of("in.txt")) },
            { s: Stream<String>? ->
                Flux.fromStream(
                    s
                )
            }
        ) { obj: Stream<String> -> obj.close() }
            .subscribeOn(Schedulers.newParallel("file-copy", 3))
            .flatMap { rub: String? ->
                currencyCalculator!!.getRatesMono(
                    Currency.getRandomCurrency(),
                    BigDecimal(rub)
                )
            }
            .subscribe(
                { result: BigDecimal? ->
                    log.info(
                        AsciiHelper.GREEN.code + "Conversion result {}" + AsciiHelper.RESET.code,
                        result
                    )
                },
                { err: Throwable? ->
                    log.error(
                        AsciiHelper.RED.code + "Some Error" + AsciiHelper.RESET.code,
                        err
                    )
                }
            ) { log.info("Completed!") }
        Thread.sleep(15000) // Necessary for test only
    }
}