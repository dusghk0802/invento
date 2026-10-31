package com.invento.domain.producttype.entity;

import com.invento.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

// 상품종류 (계층: parent가 없으면 최상위). 예) 문구 > 스티커, 특전
@Getter
@Entity
@Table(name = "product_types")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductType extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_type_id")
    private ProductType parent;

    @Column(nullable = false, unique = true, length = 100)
    private String productTypeName;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    public void update(ProductType parent, String productTypeName, boolean active) {
        this.parent = parent;
        this.productTypeName = productTypeName;
        this.active = active;
    }
}
