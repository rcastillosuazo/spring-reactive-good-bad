package com.example.demo.service;

import com.example.demo.client.ExternalApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class IncorrectReactiveService {
    private static final Logger log = LoggerFactory.getLogger(IncorrectReactiveService.class);
    private final ExternalApiClient client;

    public IncorrectReactiveService(ExternalApiClient client) {
        this.client = client;
    }

    public Mono<String> blockingCall(String input) {
        log.info("⛔ Blocking start {}", input);
        String r = client.callSlowService(input).block();
        log.info("⛔ Blocking end {}", r);
        return Mono.just(r);
    }

    public Mono<String> blockingWithBoundedElastic(String input) {
        log.info("⛔ BlockingWithBoundedElastic start {}", input);
        return Mono.fromCallable(() -> {
                    // AHORA estamos en boundedElastic
                    return client.callSlowService(input).block();
                })
                .subscribeOn(Schedulers.boundedElastic());
    }
}