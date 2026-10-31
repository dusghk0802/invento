package com.invento.domain.salessite.entity;

import com.invento.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

// 온라인 판매 채널 (자사몰, 스마트스토어 등)
@Getter
@Entity
@Table(name = "sales_sites")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SalesSite extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long salesSiteId;

    @Column(nullable = false, unique = true, length = 100)
    private String salesSiteName;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    public void update(String salesSiteName, boolean active) {
        this.salesSiteName = salesSiteName;
        this.active = active;
    }
}
