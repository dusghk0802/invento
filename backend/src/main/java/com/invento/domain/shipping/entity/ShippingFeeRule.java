package com.invento.domain.shipping.entity;

import com.invento.domain.producttype.entity.ProductType;
import jakarta.persistence.*;
import lombok.*;

// 상품종류별 배송 규칙. 예) 포스터: 택배 4000원 / 반택(CU) 허용 / 반택(GS) 선택 불가
@Getter
@Entity
@Table(name = "shipping_fee_rules",
        uniqueConstraints = @UniqueConstraint(columnNames = {"shipping_method_id", "product_type_id"}))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ShippingFeeRule {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shippingFeeRuleId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipping_method_id")
    private ShippingMethod shippingMethod;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_type_id")
    private ProductType productType;

    private Long fee; // 이 종류가 주문에 있으면 적용할 배송비 (null이면 기본 배송비)

    @Column(nullable = false)
    private boolean allowed; // false면 이 종류가 들어간 주문은 이 배송방법 선택 불가
}
