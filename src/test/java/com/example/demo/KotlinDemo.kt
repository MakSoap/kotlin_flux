package com.example.demo

import com.example.demo.model.Currency
import com.example.demo.service.CurrencyCalculator
import com.example.demo.service.CurrencyCalculator2
import com.example.demo.util.AsciiHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.Executors
import java.util.function.Function
import kotlin.coroutines.Continuation
import kotlin.system.measureTimeMillis
@SpringBootTest(classes = [StreamsToFluxesDemoApplication::class])
class KotlinDemo(
    @Autowired
    val currencyCalculator: CurrencyCalculator,
    @Autowired
    val currencyCalculator2: CurrencyCalculator2,
) {

    private val random = Random()
    private val log = logger<KotlinDemo>()

    @Test
    fun simpleStreamTest() {
        runBlocking {
            val executor = Executors.newCachedThreadPool().asCoroutineDispatcher().limitedParallelism(16)
            val time = measureTimeMillis {
                (1..300)
                    .asSequence()
                    .map {
                        async(executor) {
                            currencyCalculator.getRates(Currency.getRandomCurrency(), random.nextInt(100, 1000).toBigDecimal())
                        }
                    }
                    .onEach { result ->
                        launch(coroutineContext) { log.info("${AsciiHelper.GREEN.code}Conversion result {}${AsciiHelper.RESET.code}", result.await()) }
                    }
                    .toList()
                    .awaitAll()
            }
            println(time)
        }
    }

    @Test
    fun transformToFlux() {
        runBlocking {
            (1..1000)
                .asSequence()
                .map { random.nextInt(100, 1000).toBigDecimal() }
                .map { rub -> currencyCalculator2.requestForRates(Currency.getRandomCurrency(), rub) }
                .onEach { println("Send $it") }
                .toList()

            val list = arrayListOf<Deferred<BigDecimal>>()
            GlobalScope.launch {
                currencyCalculator2.getRatesFlow()
                    .map { async { it } }
                    .toCollection(list)
//                    .collect {
//                        log.info("${AsciiHelper.GREEN.code}Conversion result {}${AsciiHelper.RESET.code}", it)
//                    }
            }
            println("List size: $list")
            list
                .awaitAll()
                .forEach {
                    log.info("${AsciiHelper.GREEN.code}Conversion result {}${AsciiHelper.RESET.code}", it)
                }
            Thread.sleep(15000) // Necessary for test only
            println("List size: $list")
        }
    }

    @Test
    fun main2() {
        runBlocking {
            val startTime = System.currentTimeMillis()

            val deferreds = mutableListOf<Deferred<Int>>()
            getList()
                .map {
                    async {
                        delay(1000)
                        it * 50
                    }
                }
                .onEach { println("Emit $it") }
                .collect { deferreds.add(it) }

            deferreds.awaitAll()
            println("${System.currentTimeMillis() - startTime}")
            deferreds.map { it.await() }.forEach { println(it) }
        }
    }

    fun getList() = flow {
        for (i in 1..5) {
            emit(i)
        }
    }

}


