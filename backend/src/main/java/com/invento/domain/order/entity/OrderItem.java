package com.invento.domain.order.entity;

import com.invento.domain.product.entity.Product;
import com.invento.global.common.BaseEntity;
import com.invento.global.exception.BusinessException;
import jakarta.persistence.*;
import lombok.*;

import static com.invento.global.exception.ErrorCode.INVALID_QUANTITY;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

// 판매 상품. 원가·판매가는 판매 시점 값으로 고정 (나중에 상품 가격이 바뀌어도 지난 이익은 그대로)
@Entity @Table(name = "order_items")
@Getter @Builder @NoArgsConstructor(access = PROTECTED) @AllArgsConstructor(access = PRIVATE)
public class OrderItem extends BaseEntity {

    @Id @GeneratedValue(strategy = IDENTITY) private Long orderItemId;
    @ManyToOne(fetch = LAZY, optional = false) @JoinColumn(name = "order_id") private SalesOrder salesOrder;
    @ManyToOne(fetch = LAZY, optional = false) @JoinColumn(name = "product_id") private Product product;
    @Column(nullable = false) private int quantity;
    @Column(nullable = false) private long unitCost; // 원가 스냅샷
    @Column(nullable = false) private long unitPrice; // 판매가 스냅샷
    @Column(nullable = false) private long lineTotalAmount; // 판매가 × 수량
    @Column(nullable = false) private long lineProfit; // (판매가 - 원가) × 수량

    static OrderItem of(SalesOrder order, Product p, int quantity) {
        if (quantity <= 0) throw new BusinessException(INVALID_QUANTITY);
        return OrderItem.builder().salesOrder(order).product(p).quantity(quantity)
                .unitCost(p.getCostPrice()).unitPrice(p.getSellingPrice())
                .lineTotalAmount(p.getSellingPrice() * quantity)
                .lineProfit((p.getSellingPrice() - p.getCostPrice()) * quantity).build();
    }
}
