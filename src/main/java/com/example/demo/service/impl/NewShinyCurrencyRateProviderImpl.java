package com.example.demo.service.impl;

import com.example.demo.model.Currency;
import com.example.demo.service.NewShinyCurrencyRateProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewShinyCurrencyRateProviderImpl implements NewShinyCurrencyRateProvider {

    private static final AtomicInteger counter = new AtomicInteger();
    private final Map<Currency, Integer> currencyMap;

    @Override
    public Mono<BigDecimal> getRateMono(Currency currency) {
        return Mono.fromFuture(CompletableFuture.supplyAsync(() -> {
                    var i = counter.incrementAndGet();
                    log.info("{} Request for currency {}", i, currency);
                    var fractionalPart = new Random().nextInt(0, 100);
                    var integralPart = currencyMap.get(currency);
                    return new BigDecimal(integralPart * 100 + fractionalPart).movePointLeft(2);
                }))
                .delayElement(Duration.ofSeconds(1));
    }
}
