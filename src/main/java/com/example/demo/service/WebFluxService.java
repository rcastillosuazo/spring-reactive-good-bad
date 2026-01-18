package com.example.demo.service;

import com.example.demo.client.ExternalApiClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@Service
public class WebFluxService {

    private final ExternalApiClient client = new ExternalApiClient();

    public Mono<String> complexReactiveFlow(String input) {

        Mono<String> a = client.callSlowService("X1-" + input);

        Mono<String> b = a.flatMap(r -> client.callSlowService("X2-" + r));

        Mono<String> c = client.callSlowService("X3-" + input);
        Mono<String> d = client.callSlowService("X4-" + input);
        Mono<String> e = client.callSlowService("X5-" + input);

        return Mono.zip(a, b, c, d,e)
                .map(t -> t.getT1() + "|" + t.getT2() + "|" + t.getT3() + "|" + t.getT4() + "|" + t.getT5())
                .timeout(Duration.ofSeconds(3))
                .subscribeOn(Schedulers.parallel());
    }
}
