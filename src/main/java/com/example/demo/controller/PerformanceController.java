package com.example.demo.controller;

import com.example.demo.service.*;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
public class PerformanceController {
    private final CorrectReactiveService correct;
    private final IncorrectReactiveService incorrect;
    private final ComplexFlowService complex;

    public PerformanceController(CorrectReactiveService c, IncorrectReactiveService i, ComplexFlowService x) {
        this.correct = c;
        this.incorrect = i;
        this.complex = x;
    }

    @GetMapping("/correct")
    public Mono<String> correct(@RequestParam("v") String v) {
        return correct.nonBlockingCall(v);
    }

    @GetMapping("/incorrect")
    public Mono<String> incorrect(@RequestParam("v") String v) {
        return incorrect.blockingCall(v);
    }

    @GetMapping("/complex/chained")
    public Mono<String> chained(@RequestParam("v") String v) {
        return complex.chainedProcess(v);
    }

    @GetMapping("/complex/parallel")
    public Mono<String> parallel(@RequestParam("v") String v) {
        return complex.parallelJoin(v);
    }

    @GetMapping("/incorrect/bounded-elastic")
    public Mono<String> incorrectBoundedElastic(@RequestParam("v") String v) {
        return incorrect.blockingWithBoundedElastic(v);
    }
}