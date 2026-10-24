package com.invento.global.config;

import com.invento.domain.admin.repository.AdminRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AdminRepository repo) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginProcessingUrl("/api/auth/login")
                        .usernameParameter("loginId")
                        .successHandler((req, res, auth) -> repo.findByLoginId(auth.getName()).ifPresent(admin -> {
                            admin.setLastLoginAt(LocalDateTime.now());
                            repo.save(admin);
                        }))
                        .failureHandler((req, res, e) -> res.setStatus(HttpStatus.UNAUTHORIZED.value())))
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
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