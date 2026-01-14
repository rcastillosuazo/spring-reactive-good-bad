package com.example.demo.service;

import com.example.demo.client.ExternalApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ComplexFlowService {
    private static final Logger log = LoggerFactory.getLogger(ComplexFlowService.class);
    private final ExternalApiClient client;

    public ComplexFlowService(ExternalApiClient client) {
        this.client = client;
    }

    public Mono<String> chainedProcess(String input) {
        return client.callSlowService(input)
                .doOnNext(v -> log.info("Step A {}", v))
                .flatMap(client::callSlowService)
                .doOnNext(v -> log.info("Step B {}", v))
                .map(v -> "final-" + v);
    }

    public Mono<String> parallelJoin(String input) {
        Mono<String> a = client.callSlowService(input + "-A");
        Mono<String> b = client.callSlowService(input + "-B");
        return Mono.zip(a, b)
                .map(t -> "r1: "+t.getT1() + "| r2: " + t.getT2());
    }
}