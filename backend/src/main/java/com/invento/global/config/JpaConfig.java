package com.invento.global.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaConfig {

    // QueryDSL 쿼리 작성 도구 (복잡한 목록 검색에서 주입받아 사용)
    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager em) { return new JPAQueryFactory(em); }
}
