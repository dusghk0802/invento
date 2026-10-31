package com.invento.global.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

// 생성·수정 일시 자동 기록. @SuperBuilder는 위치별 재고(LocationStock) 상속 빌더용
@MappedSuperclass @EntityListeners(AuditingEntityListener.class)
@Getter @SuperBuilder @NoArgsConstructor
public abstract class BaseEntity {

    @CreatedDate @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    @LastModifiedDate @Column(nullable = false) private LocalDateTime updatedAt;
}
