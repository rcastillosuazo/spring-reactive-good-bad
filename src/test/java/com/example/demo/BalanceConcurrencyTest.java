package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class BalanceConcurrencyTest {
    final static int requests = 5;
    final static int concurrency = 5;

    @Test
    void unsafe_vs_safe_balance_comparison() {

        int amount = 10;

        WebClient client = WebClient.create("http://localhost:8080");

        // -------- UNSAFE --------
        Flux.range(1, requests)
            .flatMap(i ->
                    client.post()
                    .uri("/balance/unsafe/withdraw?amount=" + amount)
                            .retrieve()
                            .bodyToMono(Integer.class),
                    concurrency
            )
            .blockLast();

        Integer unsafe =
                client.get()
                .uri("/balance/unsafe")
                        .retrieve()
                        .bodyToMono(Integer.class).block();

        System.out.println("UNSAFE balance = " + unsafe);

        // -------- SAFE -------- CORRECTO: 50
        Flux.range(1, requests)
            .flatMap(i ->
                    client.post()
                    .uri("/balance/safe/withdraw?amount=" + amount)
                            .retrieve()
                            .bodyToMono(Integer.class),
                    concurrency
            )
            .blockLast();
        Integer safe =
            client.get()
                .uri("/balance/safe")
                    .retrieve()
                    .bodyToMono(Integer.class)
                    .block();

        System.out.println("SAFE balance = " + safe);

    }
}
