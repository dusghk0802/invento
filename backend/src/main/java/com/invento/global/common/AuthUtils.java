package com.invento.global.common;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

// 현재 로그인한 관리자 정보 (서비스에서 권한별 처리할 때 사용). 로그인 안 했으면 null / false
public final class AuthUtils {

    private AuthUtils() {
    }

    public static String loginId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? null : auth.getName();
    }

    public static boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    public static boolean isOwner() {
        return hasRole("OWNER");
    }
}
