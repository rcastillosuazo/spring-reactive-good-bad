package com.example.demo.service;

import com.example.demo.client.ExternalApiClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class JoinDependencyService {

    private final ExternalApiClient client;

    public JoinDependencyService(ExternalApiClient client) {
        this.client = client;
    }

    public Mono<String> incorrectJoinWithDependency(String input) {

        Mono<String> a = client.callSlowService(input + "-A");
        Mono<String> b = client.callSlowService(input + "-B");

        // ❌ BLOQUEO
        String aValue = a.block();
        String bValue = b.block();

        Mono<String> c = client.callSlowService(aValue + "-C");
        Mono<String> d = client.callSlowService(aValue + "-" + bValue + "-D");

        return Mono.zip(c, d)
                .map(t -> "C=" + t.getT1() + ", D=" + t.getT2());
    }
    public Mono<String> correctJoinWithDependency(String input) {

        Mono<String> a = client.callSlowService(input + "-A").cache();
        Mono<String> b = client.callSlowService(input + "-B").cache();

        Mono<String> c = a.flatMap(aVal ->
                client.callSlowService(aVal + "-C")
        );

        Mono<String> d = Mono.zip(a, b)
                .flatMap(t ->
                        client.callSlowService(t.getT1() + "-" + t.getT2() + "-D")
                );

        return Mono.zip(c, d)
                .map(t -> "C=" + t.getT1() + ", D=" + t.getT2());
    }
}
