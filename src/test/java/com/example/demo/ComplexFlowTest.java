package com.example.demo;

import com.example.demo.service.ComplexFlowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

@SpringBootTest
class ComplexFlowTest {
    @Autowired
    ComplexFlowService service;

    @Test
    void chainedWorks() {
        StepVerifier.create(service.chainedProcess("test")
                        .doOnNext(System.out::println))
                .expectNextMatches(v -> v.startsWith("final-"))
                .verifyComplete();
    }

    @Test
    void parallelWorks() {
        StepVerifier.create(service.parallelJoin("test"))
                .expectNextMatches(v -> v.contains("|"))
                .verifyComplete();
    }

    @Test
    void compareComplexFlowsPerformance() {

        int calls = 50;

        // -------------------------
        // Chained (dependiente)
        // -------------------------
        long startChained = System.currentTimeMillis();

        Flux.range(1, calls)
                .flatMap(i ->
                        service.chainedProcess("req-" + i)
                )
                .blockLast(Duration.ofSeconds(30));

        long chainedTime = System.currentTimeMillis() - startChained;

        // -------------------------
        // Parallel + join
        // -------------------------
        long startParallel = System.currentTimeMillis();

        Flux.range(1, calls)
                .flatMap(i ->
                        service.parallelJoin("req-" + i)
                )
                .blockLast(Duration.ofSeconds(30));

        long parallelTime = System.currentTimeMillis() - startParallel;

        // -------------------------
        // Resultados
        // -------------------------
        System.out.println("======================================");
        System.out.println("Complex Flow Performance Test");
        System.out.println("Calls: " + calls);
        System.out.println("--------------------------------------");
        System.out.println("🔗 Chained (A -> B) time   : " + chainedTime + " ms");
        System.out.println("⚡ Parallel (A || B) time  : " + parallelTime + " ms");
        System.out.println("======================================");
    }
    @Test
    void chainedButBlockedAntiPattern() {

        int calls = 20;

        long start = System.currentTimeMillis();

        Flux.range(1, calls)
                .map(i -> service.chainedProcess("req-" + i).block())
                .blockLast();

        System.out.println("⛔ Chained + block() ms: " +
                (System.currentTimeMillis() - start));
    }
}