package com.invento.domain.stock.entity;

import com.invento.domain.location.entity.Location;
import com.invento.global.common.BaseEntity;
import com.invento.global.exception.BusinessException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import static com.invento.global.exception.ErrorCode.STOCK_NOT_ENOUGH;
import static jakarta.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

// 위치별 재고 공통 (상품·포장재). 수량은 StockService를 통해서만 변경
@MappedSuperclass
@Getter @SuperBuilder @NoArgsConstructor(access = PROTECTED)
public abstract class LocationStock extends BaseEntity {

    @ManyToOne(fetch = LAZY, optional = false) @JoinColumn(name = "location_id") private Location location;
    @Column(nullable = false) private int onHandQty; // 실제 보유 수량
    @Column(nullable = false) private int minimumQty; // 이하로 떨어지면 알림
    @Version private Long version; // 낙관적 잠금: 동시에 같은 재고를 바꾸면 늦은 쪽이 실패

    // 보유 수량 하한: 상품은 주문 예약 수량, 포장재는 0
    protected int floorQty() { return 0; }

    public void changeOnHand(int delta) {
        if (onHandQty + delta < floorQty()) throw new BusinessException(STOCK_NOT_ENOUGH);
        onHandQty += delta;
    }

    public void changeMinimum(int minimumQty) { this.minimumQty = minimumQty; }
}
