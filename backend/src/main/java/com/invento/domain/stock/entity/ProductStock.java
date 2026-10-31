package com.invento.domain.stock.entity;

import com.invento.domain.location.entity.Location;
import com.invento.domain.product.entity.Product;
import com.invento.global.common.BaseEntity;
import com.invento.global.exception.BusinessException;
import jakarta.persistence.*;
import lombok.*;

import static com.invento.global.exception.ErrorCode.STOCK_NOT_ENOUGH;

// 위치별 현재 재고 (stock_movements 합계를 저장해 둔 값). 수량은 StockService를 통해서만 변경
@Getter
@Entity
@Table(name = "product_stocks",
        uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "location_id"}))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductStock extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productStockId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(nullable = false)
    private int onHandQty; // 실제 보유 수량

    @Column(nullable = false)
    private int reservedQty; // 온라인·대량 주문으로 예약된 수량 (출고 전)

    @Column(nullable = false)
    private int minimumQty; // 이하로 떨어지면 알림

    @Version
    private Long version; // 낙관적 잠금: 동시에 같은 재고를 바꾸면 늦은 쪽이 실패

    public int getAvailableQty() {
        return onHandQty - reservedQty;
    }

    public void changeOnHand(int delta) {
        if (onHandQty + delta < 0) throw new BusinessException(STOCK_NOT_ENOUGH);
        onHandQty += delta;
    }

    public void changeMinimum(int minimumQty) {
        this.minimumQty = minimumQty;
    }
}
