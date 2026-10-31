package com.invento.domain.stock.entity;

import com.invento.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

// 위치별 상품 재고 (stock_movements 합계를 저장해 둔 값)
@Entity @Table(name = "product_stocks", uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "location_id"}))
@Getter @SuperBuilder @NoArgsConstructor(access = PROTECTED)
public class ProductStock extends LocationStock {

    @Id @GeneratedValue(strategy = IDENTITY) private Long productStockId;
    @ManyToOne(fetch = LAZY, optional = false) @JoinColumn(name = "product_id") private Product product;
    @Column(nullable = false) private int reservedQty; // 온라인·대량 주문으로 예약된 수량 (출고 전)

    public int getAvailableQty() { return getOnHandQty() - reservedQty; }

    // 예약된 물건은 다른 데로 못 뺌
    @Override
    protected int floorQty() { return reservedQty; }
}
