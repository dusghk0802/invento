package com.invento.domain.shipping.entity;

import com.invento.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

// 배송방법 = 택배사 포함 (반택 GS/CU, CJ대한통운, 롯데택배, 우체국택배)
@Getter
@Entity
@Table(name = "shipping_methods")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ShippingMethod extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shippingMethodId;

    @Column(nullable = false, unique = true, length = 100)
    private String shippingMethodName;

    @Column(nullable = false)
    private long defaultFee; // 고객에게 받는 기본 배송비 (반택 1900 = 포장비 포함, 택배 3900)

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    public void update(String shippingMethodName, long defaultFee, boolean active) {
        this.shippingMethodName = shippingMethodName;
        this.defaultFee = defaultFee;
        this.active = active;
    }
}
