package com.example.demo.service;

import com.example.demo.model.Currency;
import kotlin.Result;
import kotlin.ResultKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.CoroutineScopeKt;
import kotlinx.coroutines.Job;
import kotlinx.coroutines.channels.BufferOverflow;
import kotlinx.coroutines.flow.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.math.BigDecimal;


@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyCalculator {

    private final NewShinyCurrencyRateProvider newShinyCurrencyRateProvider;
    private final OldDullCurrencyRateProvider oldDullCurrencyRateProvider;
    private final Sinks.Many<Request> sinks = Sinks.many().multicast().onBackpressureBuffer();

    private final MutableSharedFlow<Request> _flow = SharedFlowKt.MutableSharedFlow(1000, 1000, BufferOverflow.SUSPEND);
    private final SharedFlow<Request> flow = _flow;


    public BigDecimal getRates(Currency currency, BigDecimal rub) {
        return oldDullCurrencyRateProvider.getRate(currency).multiply(rub);
    }

    public Mono<BigDecimal> getRatesMono(Currency currency, BigDecimal rub) {
        return newShinyCurrencyRateProvider.getRateMono(currency)
                .map(rate -> rate.multiply(rub));
    }

    public void complete() {
        sinks.tryEmitComplete();
    }

    public void requestForRates(Currency currency, BigDecimal rub) {
        sinks.tryEmitNext(new Request(currency, rub));
    }

    public Flux<BigDecimal> getRatesFlux() {
        return sinks.asFlux()
                .flatMap(req ->
                        newShinyCurrencyRateProvider
                                .getRateMono(req.currency())
                                .map(rate -> rate.multiply(req.rub()))
                );
    }

    public record Request(Currency currency, BigDecimal rub) {
    }

    //kotlin Flow
    public void requestForRates2(Currency currency, BigDecimal rub) {
        _flow.tryEmit(new Request(currency, rub));
    }

    public Flow<Mono<BigDecimal>> getRatesFlow() {
        return FlowKt.map(flow, (request, continuation) -> {
                    return newShinyCurrencyRateProvider.getRateMono(request.currency()).map(rate -> rate.multiply(request.rub()));
                }
        );
    }
}
