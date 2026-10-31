package com.invento.domain.stock.entity;

import com.invento.domain.location.entity.Location;
import com.invento.domain.packaging.entity.Packaging;
import com.invento.global.common.BaseEntity;
import com.invento.global.exception.BusinessException;
import jakarta.persistence.*;
import lombok.*;

import static com.invento.global.exception.ErrorCode.STOCK_NOT_ENOUGH;

// 위치별 포장재 재고. 주문 예약 없이 사용할 때 바로 차감. 수량은 StockService를 통해서만 변경
@Getter
@Entity
@Table(name = "packaging_stocks",
        uniqueConstraints = @UniqueConstraint(columnNames = {"packaging_id", "location_id"}))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PackagingStock extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long packagingStockId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "packaging_id")
    private Packaging packaging;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(nullable = false)
    private int onHandQty;

    @Column(nullable = false)
    private int minimumQty;

    @Version
    private Long version;

    public void changeOnHand(int delta) {
        if (onHandQty + delta < 0) throw new BusinessException(STOCK_NOT_ENOUGH);
        onHandQty += delta;
    }

    public void changeMinimum(int minimumQty) {
        this.minimumQty = minimumQty;
    }
}
