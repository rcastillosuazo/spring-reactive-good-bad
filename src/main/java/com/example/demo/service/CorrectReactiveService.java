package com.example.demo.service;

import com.example.demo.client.ExternalApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CorrectReactiveService {
    private static final Logger log = LoggerFactory.getLogger(CorrectReactiveService.class);
    private final ExternalApiClient client;

    public CorrectReactiveService(ExternalApiClient client) {
        this.client = client;
    }

    public Mono<String> nonBlockingCall(String input) {
        return client.callSlowService(input)
                .doOnNext(r -> log.info("✅ Non-blocking {}", r))
                .map(String::toUpperCase);
    }
}