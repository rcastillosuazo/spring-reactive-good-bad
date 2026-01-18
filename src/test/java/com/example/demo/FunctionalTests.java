package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootTest
class FunctionalTests {

    private final WebClient client =
            WebClient.create("http://localhost:8080");

    @Test
    void mvc_correct_should_work() {
        long start = System.currentTimeMillis();
        client.get()
                .uri("/api/mvc/correct?v=test")
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(System.out::println)
                .block();
        long duration = System.currentTimeMillis() - start;
        System.out.println("mvc_correct_should_work = " + duration + " ms");
    }

    @Test
    void webflux_complex_should_work() {
        long start = System.currentTimeMillis();
        client.get()
                .uri("/api/webflux/complex?v=test")
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(System.out::println)
                .block();
        long duration = System.currentTimeMillis() - start;
        System.out.println("webflux_complex_should_work = " + duration + " ms");
    }
}
