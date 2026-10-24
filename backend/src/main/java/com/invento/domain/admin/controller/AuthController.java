package com.invento.domain.admin.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthController {

    // role: 프론트에서 SUPER_ADMIN 전용 메뉴를 숨길 때 사용
    @GetMapping("/api/auth/me")
    public Map<String, String> me(Authentication auth) {
        String role = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return Map.of("loginId", auth.getName(), "role", role);
    }
}