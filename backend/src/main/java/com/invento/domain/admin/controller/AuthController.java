package com.invento.domain.admin.controller;

import com.invento.domain.admin.repository.AdminRepository;
import com.invento.domain.location.entity.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AdminRepository adminRepo;

    @Value("${app.features.sales-sites:true}")
    private boolean salesSites;

    // role: 역할별 메뉴·버튼 표시, location: 담당 위치(사장·출고 직원은 null), features: 개인용/포폴에 따라 켜고 끄는 메뉴
    @GetMapping("/api/admin/auth/me")
    public Map<String, Object> me(Authentication auth) {
        String role = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        Map<String, Object> me = new HashMap<>(Map.of("loginId", auth.getName(), "role", role,
                "features", Map.of("salesSites", salesSites)));
        Location l = adminRepo.current().getLocation();
        me.put("location", l == null ? null
                : Map.of("locationId", l.getLocationId(), "locationName", l.getLocationName(), "locationType", l.getLocationType()));
        return me;
    }
}
