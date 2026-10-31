package com.invento.domain.production.entity;

import com.invento.domain.location.entity.Location;
import com.invento.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;
import static lombok.AccessLevel.PRIVATE;

// 입고 기록: 한 발주 항목을 여러 번 나눠 입고 가능. 정상 수량만 창고 재고(PRODUCTION_IN)로 들어감
@Entity @Table(name = "production_receipts")
@Getter @Builder @NoArgsConstructor(access = PROTECTED) @AllArgsConstructor(access = PRIVATE)
public class ProductionReceipt extends BaseEntity {

    @Id @GeneratedValue(strategy = IDENTITY) private Long productionReceiptId;
    @ManyToOne(fetch = LAZY, optional = false) @JoinColumn(name = "production_order_item_id") private ProductionOrderItem orderItem;
    @ManyToOne(fetch = LAZY, optional = false) @JoinColumn(name = "location_id") private Location location; // 입고 창고
    @Column(nullable = false) private LocalDate receivedDate;
    @Column(nullable = false) private int receivedQty;
    @Column(nullable = false) private int defectiveQty;
    @Column(nullable = false) private Long receivedBy;

    public void correct(int receivedQty, int defectiveQty) {
        this.receivedQty = receivedQty;
        this.defectiveQty = defectiveQty;
    }
}
