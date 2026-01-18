package com.example.demo.service;

import com.example.demo.client.ExternalApiClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class MvcVirtualThreadService {

    private final ExternalApiClient client = new ExternalApiClient();
    private static final ExecutorService VT_EXECUTOR =
            Executors.newVirtualThreadPerTaskExecutor();

    public CompletableFuture<String> correctFlow(String input) {
        CompletableFuture<String> a =
                client.callAsync("X1-" + input);

        CompletableFuture<String> b =
                a.thenCompose(r ->
                        client.callAsync("X2-" + r)
                );

        CompletableFuture<String> c =
                client.callAsync("X3-" + input);

        CompletableFuture<String> d =
                c.thenCompose(r ->
                        client.callAsync("X4-" + r)
                );

        CompletableFuture<String> e =
                client.callAsync("X5-" + input);

        return CompletableFuture.allOf(a, b, c, d, e)
                .thenApply(v ->
                        String.join("|",
                                a.join(),
                                b.join(),
                                c.join(),
                                d.join(),
                                e.join()
                        )
                );
    }

    // ❌ Falla: fan-out masivo sin control
    public String fanOutFlow(String input) {
        List<CompletableFuture<String>> futures =
                List.of(
                        CompletableFuture.supplyAsync(() -> client.callSlowServiceSleep("X1-" + input)),
                        CompletableFuture.supplyAsync(() -> client.callSlowServiceSleep("X2-" + input)),
                        CompletableFuture.supplyAsync(() -> client.callSlowServiceSleep("X3-" + input)),
                        CompletableFuture.supplyAsync(() -> client.callSlowServiceSleep("X4-" + input)),
                        CompletableFuture.supplyAsync(() -> client.callSlowServiceSleep("X5-" + input))
                );

        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.joining("|"));
    }

    public String fanOutFixed(String input) {

        List<CompletableFuture<String>> futures = List.of(
                CompletableFuture.supplyAsync(() -> client.callSlowServiceSleep("X1-" + input),VT_EXECUTOR),
                CompletableFuture.supplyAsync(() -> client.callSlowServiceSleep("X2-" + input),VT_EXECUTOR),
                CompletableFuture.supplyAsync(() -> client.callSlowServiceSleep("X3-" + input),VT_EXECUTOR),
                CompletableFuture.supplyAsync(() -> client.callSlowServiceSleep("X4-" + input),VT_EXECUTOR),
                CompletableFuture.supplyAsync(() -> client.callSlowServiceSleep("X5-" + input),VT_EXECUTOR)
        );

        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.joining("|"));
    }

    public Mono<String> fanOutReactive(String input) {

        return Flux.merge(
                        client.callSlowService("X1-" + input),
                        client.callSlowService("X2-" + input),
                        client.callSlowService("X3-" + input),
                        client.callSlowService("X4-" + input),
                        client.callSlowService("X5-" + input)
                ).collectList()
                .map(list -> String.join("|", list));
    }

    public Mono<String> fanOutReactiveOrder(String input) {

        return Mono.zip(
                        client.callSlowService("X1-" + input),
                        client.callSlowService("X2-" + input),
                        client.callSlowService("X3-" + input),
                        client.callSlowService("X4-" + input),
                        client.callSlowService("X5-" + input)
                )
                .map(t -> String.join("|",
                        t.getT1(),
                        t.getT2(),
                        t.getT3(),
                        t.getT4(),
                        t.getT5()
                ));
    }
}
