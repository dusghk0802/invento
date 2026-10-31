package com.invento.domain.admin.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthController {

    @Value("${app.features.sales-sites:true}")
    private boolean salesSites;

    // role: 역할별 메뉴·버튼 표시, features: 개인용/포폴에 따라 켜고 끄는 메뉴
    @GetMapping("/api/admin/auth/me")
    public Map<String, Object> me(Authentication auth) {
        String role = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return Map.of("loginId", auth.getName(), "role", role, "features", Map.of("salesSites", salesSites));
    }
}
