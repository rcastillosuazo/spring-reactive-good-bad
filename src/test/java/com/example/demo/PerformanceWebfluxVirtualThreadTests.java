package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

class PerformanceWebfluxVirtualThreadTests {
    final static int request=100;
    final static int concurrent=100;

    private final WebClient client =
            WebClient.create("http://localhost:8080");

    @Test
    void mvc_virtual_threads_high_load() {
        long start = System.currentTimeMillis();

        Flux.range(1, request)
                .flatMap(i ->
                        client.get()
                                .uri("/api/mvc/correct?v=req-virtual-th-" + i)
                                .retrieve()
                                .bodyToMono(String.class)
                        , concurrent
                )
                .doOnNext(System.out::println)
                .blockLast();

        System.out.println("MVC + VT time = " +
                (System.currentTimeMillis() - start));
    }

    @Test
    void webflux_high_load() {
        long start = System.currentTimeMillis();

        Flux.range(1, request)
                .flatMap(i ->
                        client.get()
                                .uri("/api/webflux/complex?v=req-webflux-" + i)
                                .retrieve()
                                .bodyToMono(String.class)
                        ,concurrent)
                .doOnNext(System.out::println)
                .blockLast();

        System.out.println("WebFlux time = " +
                (System.currentTimeMillis() - start));
    }

    @Test
    void mvc_fanout_should_degrade() {
        long start = System.currentTimeMillis();

        Flux.range(1, request)
                .flatMap(i ->
                        client.get()
                                .uri("/api/mvc/fanout?v=fanout-" + i)
                                .retrieve()
                                .bodyToMono(String.class)
                        , concurrent)
                .doOnNext(System.out::println)
                .blockLast();

        System.out.println("MVC fan-out time = " +
                (System.currentTimeMillis() - start));
    }

    @Test
    void mvc_fanout_should_degrade_fixed() {
        long start = System.currentTimeMillis();

        Flux.range(1, request)
                .flatMap(i ->
                        client.get()
                                .uri("/api/mvc/fanout-fixed?v=fanout-fixed-" + i)
                                .retrieve()
                                .bodyToMono(String.class)
                        , concurrent)
                .doOnNext(System.out::println)
                .blockLast();

        System.out.println("MVC mvc_fanout_fixed time = " +
                (System.currentTimeMillis() - start));
    }

    @Test
    void webflux_fanout_fixed_reactive() {
        long start = System.currentTimeMillis();

        Flux.range(1, request)
                .flatMap(i ->
                        client.get()
                                .uri("/api/webflux/fanout-fixed?v=fanout-fixed-" + i)
                                .retrieve()
                                .bodyToMono(String.class)
                        , concurrent)
                .doOnNext(System.out::println)
                .blockLast();

        System.out.println("MVC webflux_fanout_fixed_reactive time = " +
                (System.currentTimeMillis() - start));
    }

    @Test
    void webflux_fanout_fixed_reactive_order() {
        long start = System.currentTimeMillis();

        Flux.range(1, request)
                .flatMap(i ->
                                client.get()
                                        .uri("/api/webflux/fanout-fixed-order?v=fanout-fixed-order" + i)
                                        .retrieve()
                                        .bodyToMono(String.class)
                        , concurrent)
                .doOnNext(System.out::println)
                .blockLast();

        System.out.println("MVC webflux_fanout_fixed_reactive_order time = " +
                (System.currentTimeMillis() - start));
    }
}
