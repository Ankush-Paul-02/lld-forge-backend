package com.devmare.lldforge.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String holla() {
        return "Welcome to LLD FORGE";
    }
}
