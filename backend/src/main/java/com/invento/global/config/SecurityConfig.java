package com.invento.global.config;

import com.invento.domain.admin.repository.AdminRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import java.time.LocalDateTime;

@Configuration
public class SecurityConfig {

    // 조회는 모든 직원, 등록·수정·삭제는 사장만
    private static final String[] OWNER_WRITE = {
            "/api/admin/products/**", "/api/admin/series/**", "/api/admin/gift-rules/**", "/api/admin/suppliers/**"
    };

    // 사장 전용 (직원 계정, 활동 로그, 위치 관리)
    private static final String[] OWNER_ONLY = {
            "/api/admin/admins/**", "/api/admin/activity-logs/**", "/api/admin/locations/**"
    };

    // 관리자 백오피스 API(/api/admin/**) 전용 보안 설정. 쇼핑몰(/api/shop/**)은 나중에 별도 체인으로 분리
    @Bean
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http, AdminRepository repo) throws Exception {
        http.securityMatcher("/api/admin/**")
                .csrf(AbstractHttpConfigurer::disable)
                // 위에서부터 먼저 맞는 규칙이 적용됨
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/admin/auth/login").permitAll()
                        .requestMatchers(OWNER_ONLY).hasRole("OWNER")
                        .requestMatchers(HttpMethod.GET, OWNER_WRITE).authenticated()
                        .requestMatchers(OWNER_WRITE).hasRole("OWNER")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginProcessingUrl("/api/admin/auth/login")
                        .usernameParameter("loginId")
                        .successHandler((req, res, auth) -> repo.findByLoginId(auth.getName()).ifPresent(admin -> {
                            admin.setLastLoginAt(LocalDateTime.now());
                            repo.save(admin);
                        }))
                        .failureHandler((req, res, e) -> res.setStatus(HttpStatus.UNAUTHORIZED.value())))
                .logout(logout -> logout
                        .logoutUrl("/api/admin/auth/logout")
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler()))
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(AdminRepository repo) {
        return loginId -> repo.findByLoginId(loginId)
                .map(admin -> User.withUsername(admin.getLoginId())
                        .password(admin.getPasswordHash())
                        .roles(admin.getRole().name())
                        .disabled(!admin.isActive())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException(loginId));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}