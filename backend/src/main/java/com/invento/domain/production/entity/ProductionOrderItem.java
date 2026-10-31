package com.invento.domain.production.entity;

import com.invento.domain.product.entity.Product;
import com.invento.global.common.BaseEntity;
import com.invento.global.exception.BusinessException;
import jakarta.persistence.*;
import lombok.*;

import static com.invento.global.exception.ErrorCode.RECEIPT_EXCEEDS;

@Getter
@Entity
@Table(name = "production_order_items")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductionOrderItem extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productionOrderItemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "production_order_id")
    private ProductionOrder productionOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private int orderedQty;

    @Column(nullable = false)
    private int receivedQty; // 정상 입고 합계 (여러 번 나눠 입고 가능)

    @Column(nullable = false)
    private int defectiveQty; // 불량 합계 (재고에 넣지 않음)

    public int getRemainingQty() {
        return orderedQty - receivedQty - defectiveQty;
    }

    public void receive(int ok, int defective) {
        if (ok < 0 || defective < 0 || ok + defective > getRemainingQty()) throw new BusinessException(RECEIPT_EXCEEDS);
        receivedQty += ok;
        defectiveQty += defective;
    }

    // 입고 수량 수정: 입고 기록을 고친 차이만큼 합계도 고침
    public void correct(int okDelta, int defectiveDelta) {
        int ok = receivedQty + okDelta, defective = defectiveQty + defectiveDelta;
        if (ok < 0 || defective < 0 || ok + defective > orderedQty) throw new BusinessException(RECEIPT_EXCEEDS);
        receivedQty = ok;
        defectiveQty = defective;
    }
}
