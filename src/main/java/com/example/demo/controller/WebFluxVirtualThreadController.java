package com.example.demo.controller;

import com.example.demo.service.MvcVirtualThreadService;
import com.example.demo.service.WebFluxService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api")
public class WebFluxVirtualThreadController {

    private final MvcVirtualThreadService mvcService;
    private final WebFluxService webFluxService;

    public WebFluxVirtualThreadController(MvcVirtualThreadService mvcService,
                                          WebFluxService webFluxService) {
        this.mvcService = mvcService;
        this.webFluxService = webFluxService;
    }
    // ===== MVC + Virtual Threads (correcto) =====
    @GetMapping("/mvc/correct")
    public CompletableFuture<String> mvcCorrect(@RequestParam("v") String v) {
        return mvcService.correctFlow(v);
    }

    // ===== MVC + Virtual Threads (falla por fan-out masivo) =====
    @GetMapping("/mvc/fanout")
    public String mvcFanOut(@RequestParam("v") String v) {
        return mvcService.fanOutFlow(v);
    }
    // ===== MVC + Virtual Threads FIXED =====
    @GetMapping("/mvc/fanout-fixed")
    public String mvcFanOutFixed(@RequestParam("v") String v) {
        return mvcService.fanOutFixed(v);
    }
    // ===== Fan out fixed with reactive =====
    @GetMapping("/webflux/fanout-fixed")
    public Mono<String> mvcFanOutFixedReactive(@RequestParam("v") String v) {
        return mvcService.fanOutReactive(v);
    }
    // ===== Fan out fixed with reactive =====
    @GetMapping("/webflux/fanout-fixed-order")
    public Mono<String> mvcFanOutFixedReactiveOrder(@RequestParam("v") String v) {
        return mvcService.fanOutReactiveOrder(v);
    }
    // ===== WebFlux (caso necesario) =====
    @GetMapping("/webflux/complex")
    public Mono<String> webfluxComplex(@RequestParam("v") String v) {
        return webFluxService.complexReactiveFlow(v);
    }
}
