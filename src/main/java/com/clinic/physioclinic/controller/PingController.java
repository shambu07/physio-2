// src/main/java/com/clinic/physioclinic/controller/PingController.java
package com.clinic.physioclinic.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {
    @GetMapping("/ping")
    public String ping() { return "pong"; }
}
