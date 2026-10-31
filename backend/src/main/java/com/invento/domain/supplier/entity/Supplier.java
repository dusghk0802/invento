package com.invento.domain.supplier.entity;

import com.invento.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

// 공급처: 포폴=공장, 개인용=구매처
@Getter
@Entity
@Table(name = "suppliers")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Supplier extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long supplierId;

    @Column(nullable = false, unique = true, length = 100)
    private String supplierName;

    @Column(length = 100)
    private String contact;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    public void update(String supplierName, String contact, boolean active) {
        this.supplierName = supplierName;
        this.contact = contact;
        this.active = active;
    }
}
