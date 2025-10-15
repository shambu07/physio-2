package com.clinic.physioclinic.controller;

import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
public class RootController {
    private final Environment env;
    public RootController(Environment env) { this.env = env; }

    @GetMapping("/api/status")
    public Map<String, Object> status() {
        return Map.of(
                "app", "physio-clinic",
                "status", "OK",
                "profiles", List.of(env.getActiveProfiles())
        );
    }
}
