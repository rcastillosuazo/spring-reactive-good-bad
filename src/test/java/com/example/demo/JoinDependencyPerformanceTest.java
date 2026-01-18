package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@SpringBootTest
class JoinDependencyPerformanceTest {
    final static int requests = 20;
    final static int concurrency = 10;
    @Test
    void correct_join_should_scale_simultaneous() {

        long start = System.currentTimeMillis();

        WebClient client = WebClient.create("http://localhost:8080");

        Flux.range(1, requests)
                .flatMap(i ->
                                client.get()
                                        .uri("/join/correct?v=req-" + i)
                                        .retrieve()
                                        .bodyToMono(String.class),
                        concurrency
                )
                .blockLast();

        long duration = System.currentTimeMillis() - start;
        System.out.println("CORRECT join (simultaneous) = " + duration + " ms");
    }


    @Test
    void incorrect_join_should_fail_or_be_slow() {

        long start = System.currentTimeMillis();

        WebClient client = WebClient.create("http://localhost:8080");

        Flux.range(1, requests)
                .flatMap(i ->
                        client.get()
                                .uri("/join/incorrect?v=req-" + i)
                                .retrieve()
                                .bodyToMono(String.class),
                        concurrency
                )
                .blockLast();

        long duration = System.currentTimeMillis() - start;
        System.out.println("INCORRECT join time = " + duration + " ms");
    }
}
