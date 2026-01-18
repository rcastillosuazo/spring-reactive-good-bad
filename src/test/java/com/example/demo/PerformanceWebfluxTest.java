package com.example.demo;

import com.example.demo.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;

@SpringBootTest
class PerformanceWebfluxTest {

    final static int requests = 600;

    @Autowired
    CorrectReactiveService correct;
    @Autowired
    IncorrectReactiveService incorrect;

    @Test
    void compare() {
        int calls = 50;
        long s = System.currentTimeMillis();
        Flux.range(1, calls).flatMap(i -> correct.nonBlockingCall("x" + i)).blockLast();
        System.out.println("✅ Non-blocking ms: " + (System.currentTimeMillis() - s));
        s = System.currentTimeMillis();
        Flux.range(1, calls).flatMap(i -> incorrect.blockingCall("x" + i)).blockLast();
        System.out.println("⛔ Blocking ms: " + (System.currentTimeMillis() - s));
    }

    private WebClient client() {
        return WebClient.builder()
                .baseUrl("http://localhost:8080")
                .build();
    }

    @Test
    void reactive_correct_non_blocking() {
        long start = System.currentTimeMillis();
        client()
                .get()
                .uri("/correct?v=test")
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(r -> System.out.println("CORRECT => " + r))
                .block();
        long duration = System.currentTimeMillis() - start;
        System.out.println("reactive_correct_non_blocking = " + duration + " ms");
    }

    //Da error por bloqueo de hilo en tiempo de ejecución,
    // no da error en tiempo de compilación
    @Test
    void reactive_incorrect_blocking() {
        long start = System.currentTimeMillis();
        client()
                .get()
                .uri("/incorrect?v=test")
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(r -> System.out.println("INCORRECT => " + r))
                .block();
        long duration = System.currentTimeMillis() - start;
        System.out.println("eactive_incorrect_blocking = " + duration + " ms");
    }

    @Test
    void complex_chained_flow() {
        long start = System.currentTimeMillis();
        client()
                .get()
                .uri("/complex/chained?v=test")
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(r -> System.out.println("CHAINED => " + r))
                .block();
        long duration = System.currentTimeMillis() - start;
        System.out.println("complex_chained_flow = " + duration + " ms");
    }

    @Test
    void complex_parallel_join() {
        long start = System.currentTimeMillis();
        client()
                .get()
                .uri("/complex/parallel?v=test")
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(r -> System.out.println("PARALLEL => " + r))
                .block();
        long duration = System.currentTimeMillis() - start;
        System.out.println("complex_parallel_join = " + duration + " ms");
    }

    @Test
    void real_concurrency_correct_endpoint() {

        long start = System.currentTimeMillis();
        Flux.range(1, requests)
                .flatMap(i ->
                        client()
                                .get()
                                .uri("/correct?v=req-" + i)
                                .retrieve()
                                .bodyToMono(String.class)
                                .doOnNext(r ->
                                        System.out.println("CORRECT REQ-" + i + " => " + r)
                                )
                )
                .blockLast(Duration.ofSeconds(10));

        long duration = System.currentTimeMillis() - start;
        System.out.println("Correct concurrency = " + duration + " ms");
    }

    @Test
    void incorrect_bounded_elastic_using_webclient() {

        WebClient client = WebClient.builder()
                .baseUrl("http://localhost:8080")
                .build();

        long start = System.currentTimeMillis();

        Flux.range(1, requests)
                .flatMap(i ->
                        client.get()
                                .uri("/incorrect/bounded-elastic?v=req-" + i)
                                .retrieve()
                                .bodyToMono(String.class)
                )
                .blockLast();

        long duration = System.currentTimeMillis() - start;
        System.out.println("INCORRECT boundedElastic (WebClient) = " + duration + " ms");
    }
}