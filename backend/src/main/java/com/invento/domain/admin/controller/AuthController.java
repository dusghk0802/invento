package com.invento.domain.admin.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthController {

    @GetMapping("/api/auth/me")
    public Map<String, String> me(Authentication auth) {
        return Map.of("loginId", auth.getName());
    }
}