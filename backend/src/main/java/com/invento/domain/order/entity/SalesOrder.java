package com.invento.domain.order.entity;

import com.invento.domain.location.entity.Location;
import com.invento.domain.product.entity.Product;
import com.invento.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

// 판매 주문 (매장 영수증 / 온라인 주문 공통). 온라인 배송 정보는 6단계에서 추가
@Entity @Table(name = "sales_orders", indexes = {@Index(columnList = "channel, status, order_date"), @Index(columnList = "location_id, order_date")})
@Getter @Builder @NoArgsConstructor(access = PROTECTED) @AllArgsConstructor(access = PRIVATE)
public class SalesOrder extends BaseEntity {

    @Id @GeneratedValue(strategy = IDENTITY) private Long orderId;
    @Enumerated(STRING) @Column(nullable = false) private SalesChannel channel;
    @Column(nullable = false, unique = true, length = 30) private String orderNo; // 주문번호 / 매장은 영수증 번호
    @ManyToOne(fetch = LAZY, optional = false) @JoinColumn(name = "location_id") private Location location; // 온라인=창고, 매장=매장
    @Column(nullable = false) private LocalDateTime orderDate;
    @Enumerated(STRING) @Column(nullable = false) private OrderStatus status;
    @Column(nullable = false) private long receivedAmount; // 실제 받은 금액 (매장 = 상품 합계)
    @Enumerated(STRING) private PaymentMethod paymentMethod; // 매장만
    @Column(nullable = false) private long productProfit; // Σ (판매가 - 원가) × 수량
    @Column(nullable = false) private long finalProfit; // 상품 이익 (증정 원가는 7단계에서 차감)
    @Column(nullable = false) private Long createdBy;

    @Builder.Default @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL) @OrderBy("orderItemId")
    private List<OrderItem> items = new ArrayList<>();

    public void addItem(Product product, int quantity) { items.add(OrderItem.of(this, product, quantity)); }

    public long getTotalAmount() { return items.stream().mapToLong(OrderItem::getLineTotalAmount).sum(); }

    // 매장 영수증 확정: 받은 금액·이익 계산
    public void complete() {
        receivedAmount = getTotalAmount();
        productProfit = items.stream().mapToLong(OrderItem::getLineProfit).sum();
        finalProfit = productProfit;
        status = OrderStatus.COMPLETED;
    }
}
