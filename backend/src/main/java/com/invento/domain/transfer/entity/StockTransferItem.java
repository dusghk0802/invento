package com.invento.domain.transfer.entity;

import com.invento.domain.product.entity.Product;
import com.invento.global.exception.BusinessException;
import jakarta.persistence.*;
import lombok.*;

import static com.invento.global.exception.ErrorCode.BOX_QTY_REQUIRED;
import static com.invento.global.exception.ErrorCode.INVALID_QUANTITY;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;
import static lombok.AccessLevel.PRIVATE;

// 매장 출고는 박스 단위: 수량 = 박스 수 × 상품 박스 입수량 + 낱개
@Entity @Table(name = "stock_transfer_items")
@Getter @Builder @NoArgsConstructor(access = PROTECTED) @AllArgsConstructor(access = PRIVATE)
public class StockTransferItem {

    @Id @GeneratedValue(strategy = IDENTITY) private Long stockTransferItemId;
    @ManyToOne(fetch = LAZY, optional = false) @JoinColumn(name = "stock_transfer_id") private StockTransfer stockTransfer;
    @ManyToOne(fetch = LAZY, optional = false) @JoinColumn(name = "product_id") private Product product;
    @Column(nullable = false) private int boxCount;
    @Column(nullable = false) private int looseQty;
    @Column(nullable = false) private int quantity; // 재고 차감·증가 기준

    // 출고 처리 때 창고에서 박스 수를 바꿀 수 있음 (0이면 이번에 안 보냄)
    public void changeQty(int boxCount, int looseQty) {
        if (boxCount < 0 || looseQty < 0) throw new BusinessException(INVALID_QUANTITY);
        Integer boxQty = product.getBoxQty();
        if (boxCount > 0 && boxQty == null) throw new BusinessException(BOX_QTY_REQUIRED);
        this.boxCount = boxCount;
        this.looseQty = looseQty;
        this.quantity = boxCount * (boxQty == null ? 0 : boxQty) + looseQty;
    }
}
