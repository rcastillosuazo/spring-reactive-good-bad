package com.example.demo.controller;

import com.example.demo.service.BalanceReactiveService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/balance")
public class BalanceController {

    private final BalanceReactiveService service;

    public BalanceController(BalanceReactiveService service) {
        this.service = service;
    }

    // ❌ INCORRECTO
    @PostMapping("/unsafe/withdraw")
    public Mono<Integer> withdrawUnsafe(@RequestParam("amount") int amount) {
        return service.withdrawUnsafe(amount);
    }

    @GetMapping("/unsafe")
    public int unsafeBalance() {
        return service.getUnsafeBalance();
    }

    // ✅ CORRECTO
    @PostMapping("/safe/withdraw")
    public Mono<Integer> withdrawSafe(@RequestParam("amount") int amount) {
        return service.withdrawSafe(amount);
    }

    @GetMapping("/safe")
    public int safeBalance() {
        return service.getSafeBalance();
    }
}
