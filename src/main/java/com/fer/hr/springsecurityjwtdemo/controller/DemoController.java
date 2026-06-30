package com.fer.hr.springsecurityjwtdemo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo")
public class DemoController {

    // GET all users
    @GetMapping
    public ResponseEntity<String> demo() {
        return ResponseEntity.ok("hello");
    }

}
