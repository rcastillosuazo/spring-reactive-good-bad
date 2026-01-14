package com.example.demo.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class BalanceReactiveService {

    /* ==============================
       CASO INCORRECTO
       ============================== */

    private int unsafeBalance = 100;

    public Mono<Integer> withdrawUnsafe(int amount) {
        return Mono.fromCallable(() -> {
            int current = unsafeBalance;
            Thread.sleep(100); // simula latencia real
            unsafeBalance = current - amount;
            return unsafeBalance;
        });
    }

    public int getUnsafeBalance() {
        return unsafeBalance;
    }

    /* ==============================
       CASO CORRECTO
       ============================== */

    private final AtomicInteger safeBalance = new AtomicInteger(100);

    public Mono<Integer> withdrawSafe(int amount) {
        return Mono.fromCallable(() -> safeBalance.addAndGet(-amount));
    }

    public int getSafeBalance() {
        return safeBalance.get();
    }
}
