package com.invento.domain.producttype.entity;

import com.invento.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;
import static lombok.AccessLevel.PRIVATE;

// 상품종류 (계층: parent가 없으면 최상위). 예) 문구 > 스티커, 특전
@Entity @Table(name = "product_types")
@Getter @Builder @NoArgsConstructor(access = PROTECTED) @AllArgsConstructor(access = PRIVATE)
public class ProductType extends BaseEntity {

    @Id @GeneratedValue(strategy = IDENTITY) private Long productTypeId;
    @ManyToOne(fetch = LAZY) @JoinColumn(name = "parent_type_id") private ProductType parent;
    @Column(nullable = false, unique = true, length = 100) private String productTypeName;
    @Builder.Default @Column(nullable = false) private boolean active = true;

    public void update(ProductType parent, String productTypeName, boolean active) {
        this.parent = parent;
        this.productTypeName = productTypeName;
        this.active = active;
    }
}
