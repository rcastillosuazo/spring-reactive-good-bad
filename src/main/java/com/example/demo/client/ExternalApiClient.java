package com.example.demo.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static java.lang.Thread.sleep;

@Component
public class ExternalApiClient {
    private static final Logger log = LoggerFactory.getLogger(ExternalApiClient.class);
    protected static final int WAITTIME = 1000;

    private final Executor vtExecutor =
            Executors.newVirtualThreadPerTaskExecutor();

    public Mono<String> callSlowService(String input) {
        return Mono.delay(Duration.ofMillis(WAITTIME))
                .doOnSubscribe(s -> log.info("➡️ Async start {}", input))
                .map(i -> input)
                .doOnNext(r -> log.info("⬅️ Async end {}", r));
    }

    public String callSlowServiceSleep(String input) {
            log.info("Call Slow Service Block start {}", input);
            try {
                sleep(WAITTIME); // IO bloqueante
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log.info("Call Slow Service Block end {}", input);
            return input;

    }

    public CompletableFuture<String> callAsync(String value) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                sleep(WAITTIME);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return value;
        }, vtExecutor);
    }
}