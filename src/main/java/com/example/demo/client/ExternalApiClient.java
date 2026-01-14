package com.example.demo.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class ExternalApiClient {
    private static final Logger log = LoggerFactory.getLogger(ExternalApiClient.class);

    public Mono<String> callSlowService(String input) {
        return Mono.delay(Duration.ofMillis(200))
                .doOnSubscribe(s -> log.info("➡️ Async start {}", input))
                .map(i -> "response API: " + input)
                .doOnNext(r -> log.info("⬅️ Async end {}", r));
    }
}