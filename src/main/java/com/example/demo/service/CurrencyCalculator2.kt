package com.example.demo.service

import com.example.demo.model.Currency
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import lombok.extern.slf4j.Slf4j
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Slf4j
@Service
class CurrencyCalculator2(
    var newShinyCurrencyRateProvider: NewShinyCurrencyRateProviderImplKotlin
) {

    private val _flow = MutableSharedFlow<CurrencyCalculator.Request>(1000, 1000)
    val flow: SharedFlow<CurrencyCalculator.Request> = _flow

    private val channel = Channel<CurrencyCalculator.Request>()

    fun requestForRates(currency: Currency, rub: BigDecimal) {
        _flow.tryEmit(CurrencyCalculator.Request(currency, rub))
//        channel.trySend(CurrencyCalculator.Request(currency, rub))
    }

    fun getRatesFlow(): Flow<BigDecimal> =
        flow.map {  req ->
            newShinyCurrencyRateProvider.getRateMono(req.currency).multiply(req.rub)
        }
}