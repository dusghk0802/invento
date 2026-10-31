package com.invento.domain.admin;

import com.invento.domain.admin.entity.Admin;
import com.invento.domain.admin.entity.AdminRole;
import com.invento.domain.admin.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminInitializer {

    @Bean
    ApplicationRunner initAdmin(AdminRepository repo, PasswordEncoder encoder,
                                @Value("${app.admin.login-id}") String loginId,
                                @Value("${app.admin.password}") String password) {
        return args -> {
            if (repo.count() > 0) return;
            repo.save(Admin.builder()
                    .loginId(loginId)
                    .passwordHash(encoder.encode(password))
                    .adminName("사장")
                    .role(AdminRole.OWNER)
                    .build());
        };
    }
}