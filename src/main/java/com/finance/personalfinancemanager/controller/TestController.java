package com.finance.personalfinancemanager.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test")
    public String test() {
        return "Application is running! Spring Boot works!";
    }
    
    @GetMapping("/hello")
    public String hello() {
        return "Hello World!";
    }
}
