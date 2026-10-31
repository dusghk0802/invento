package com.invento.domain.gift.entity;

import com.invento.domain.producttype.entity.ProductType;
import com.invento.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

// 증정 구간: 상품 합계가 minimumAmount 이상이면 대상 상품종류 중에서 giftQty개 증정
// 실제 어떤 상품을 줄지는 주문 처리할 때 직원이 대상 종류의 재고 있는 상품 중에서 선택
@Getter
@Entity
@Table(name = "gift_rules")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GiftRule extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long giftRuleId;

    @Column(nullable = false, unique = true)
    private long minimumAmount; // 상품 합계 기준 (배송비 제외)

    @Column(nullable = false)
    private int giftQty; // 정확히 이 개수만큼 선택

    @Column(nullable = false)
    private boolean freeShipping; // 온라인 주문만

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    // 증정 대상 상품종류 (예: 특전, 스티커)
    @Builder.Default
    @ManyToMany
    @JoinTable(name = "gift_rule_product_types",
            joinColumns = @JoinColumn(name = "gift_rule_id"),
            inverseJoinColumns = @JoinColumn(name = "product_type_id"))
    private Set<ProductType> productTypes = new HashSet<>();

    public void update(long minimumAmount, int giftQty, boolean freeShipping, boolean active, Set<ProductType> productTypes) {
        this.minimumAmount = minimumAmount;
        this.giftQty = giftQty;
        this.freeShipping = freeShipping;
        this.active = active;
        this.productTypes.clear();
        this.productTypes.addAll(productTypes);
    }
}
