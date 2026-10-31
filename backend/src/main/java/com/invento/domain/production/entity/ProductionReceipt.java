package com.invento.domain.production.entity;

import com.invento.domain.location.entity.Location;
import com.invento.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

// 입고 기록: 한 발주 항목을 여러 번 나눠 입고 가능. 정상 수량만 창고 재고(PRODUCTION_IN)로 들어감
@Getter
@Entity
@Table(name = "production_receipts")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductionReceipt extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productionReceiptId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "production_order_item_id")
    private ProductionOrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id")
    private Location location; // 입고 창고

    @Column(nullable = false)
    private LocalDate receivedDate;

    @Column(nullable = false)
    private int receivedQty;

    @Column(nullable = false)
    private int defectiveQty;

    @Column(nullable = false)
    private Long receivedBy;

    public void correct(int receivedQty, int defectiveQty) {
        this.receivedQty = receivedQty;
        this.defectiveQty = defectiveQty;
    }
}
