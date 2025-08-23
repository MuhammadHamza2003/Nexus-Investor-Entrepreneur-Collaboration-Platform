package com.Nexus.Nexus.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TestController {
    
    @PostMapping("/echo")
    public ResponseEntity<?> echo(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Echo successful");
        response.put("receivedData", payload);
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "running");
        response.put("message", "Nexus authentication service is operational");
        response.put("endpoints", new String[]{
            "GET /api/public/health",
            "GET /api/public/test", 
            "POST /api/auth/register",
            "POST /api/auth/login",
            "GET /api/auth/profile",
            "PUT /api/auth/profile"
        });
        return ResponseEntity.ok(response);
    }
}
