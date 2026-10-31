package com.invento.domain.packaging.entity;

import com.invento.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// 포장재 정보만 저장. 재고 수량은 위치별로 PackagingStock에 있음
@Getter
@Entity
@Table(name = "packaging")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Packaging extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long packagingId;

    @Column(nullable = false, length = 200)
    private String packagingName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PackagingUsage usageType;

    @Column(nullable = false, length = 100)
    private String packagingType; // 예: 박스, 봉투, 완충재

    @Column(nullable = false)
    private long unitCost; // 개당 단가 (포장비 자동 계산에 사용)

    @Column(length = 1000)
    private String purchaseUrl;

    private LocalDateTime deletedAt; // Soft Delete

    public void update(String packagingName, PackagingUsage usageType, String packagingType, long unitCost,
                       String purchaseUrl) {
        this.packagingName = packagingName;
        this.usageType = usageType;
        this.packagingType = packagingType;
        this.unitCost = unitCost;
        this.purchaseUrl = purchaseUrl;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
