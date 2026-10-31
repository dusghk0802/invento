package com.invento.domain.shipping.entity;

import com.invento.domain.producttype.entity.ProductType;
import jakarta.persistence.*;
import lombok.*;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;
import static lombok.AccessLevel.PRIVATE;

// 상품종류별 배송 규칙. 예) 포스터: 택배 4000원 / 반택(CU) 허용 / 반택(GS) 선택 불가
@Entity @Table(name = "shipping_fee_rules", uniqueConstraints = @UniqueConstraint(columnNames = {"shipping_method_id", "product_type_id"}))
@Getter @Builder @NoArgsConstructor(access = PROTECTED) @AllArgsConstructor(access = PRIVATE)
public class ShippingFeeRule {

    @Id @GeneratedValue(strategy = IDENTITY) private Long shippingFeeRuleId;
    @ManyToOne(fetch = LAZY, optional = false) @JoinColumn(name = "shipping_method_id") private ShippingMethod shippingMethod;
    @ManyToOne(fetch = LAZY, optional = false) @JoinColumn(name = "product_type_id") private ProductType productType;
    private Long fee; // 이 종류가 주문에 있으면 적용할 배송비 (null이면 기본 배송비)
    @Column(nullable = false) private boolean allowed; // false면 이 종류가 들어간 주문은 이 배송방법 선택 불가
}
