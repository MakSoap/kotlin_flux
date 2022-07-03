package com.example.demo.service

import com.example.demo.model.Currency
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.Duration
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.DurationUnit
import kotlin.time.seconds
import kotlin.time.toDuration

@Service
class NewShinyCurrencyRateProviderImplKotlin(
    private val currencyMap: Map<Currency, Int>
) {

    suspend fun getRateMono(currency: Currency): BigDecimal = coroutineScope {
            delay(1.toDuration(DurationUnit.SECONDS))
            val i = counter.incrementAndGet()
            log.info("{} Request for currency {}", i, currency)
            val fractionalPart = Random().nextInt(0, 100)
            val integralPart = currencyMap[currency]
            BigDecimal(integralPart!! * 100 + fractionalPart).movePointLeft(2)
    }

    companion object {
        private val counter = AtomicInteger()
        private val log = LoggerFactory.getLogger(NewShinyCurrencyRateProviderImplKotlin::class.java)
    }
}