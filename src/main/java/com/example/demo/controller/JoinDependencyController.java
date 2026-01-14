package com.example.demo.controller;

import com.example.demo.service.JoinDependencyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/join")
public class JoinDependencyController {

    private final JoinDependencyService service;

    public JoinDependencyController(JoinDependencyService service) {
        this.service = service;
    }

    @GetMapping("/correct")
    public Mono<String> correct(@RequestParam("v") String v) {
        return service.correctJoinWithDependency(v);
    }

    @GetMapping("/incorrect")
    public Mono<String> incorrect(@RequestParam("v") String v) {
        return service.incorrectJoinWithDependency(v);
    }
}
