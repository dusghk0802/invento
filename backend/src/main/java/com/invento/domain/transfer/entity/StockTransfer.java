package com.invento.domain.transfer.entity;

import com.invento.domain.location.entity.Location;
import com.invento.domain.product.entity.Product;
import com.invento.global.common.BaseEntity;
import com.invento.global.exception.BusinessException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.invento.global.exception.ErrorCode.INVALID_STATUS;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;
import static lombok.AccessLevel.PRIVATE;

// 재고 이동 (주로 창고 → 매장). 보낸 수량과 받은 수량이 다르면 재고 조정(이동 수량 차이)으로 수기 처리
@Entity @Table(name = "stock_transfers")
@Getter @Builder @NoArgsConstructor(access = PROTECTED) @AllArgsConstructor(access = PRIVATE)
public class StockTransfer extends BaseEntity {

    @Id @GeneratedValue(strategy = IDENTITY) private Long stockTransferId;
    @ManyToOne(fetch = LAZY, optional = false) @JoinColumn(name = "from_location_id") private Location fromLocation;
    @ManyToOne(fetch = LAZY, optional = false) @JoinColumn(name = "to_location_id") private Location toLocation;
    @Builder.Default @Enumerated(STRING) @Column(nullable = false) private TransferStatus status = TransferStatus.REQUESTED;
    @Column(nullable = false) private Long requestedBy; // 요청일 = createdAt
    private Long shippedBy;
    private LocalDateTime shippedAt; // 출발지 TRANSFER_OUT
    private Long receivedBy;
    private LocalDateTime receivedAt; // 도착지 TRANSFER_IN
    @Column(length = 500) private String memo;

    @Builder.Default
    @OneToMany(mappedBy = "stockTransfer", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stockTransferItemId")
    private List<StockTransferItem> items = new ArrayList<>();

    public void addItem(Product product, int boxCount, int looseQty) {
        StockTransferItem item = StockTransferItem.builder().stockTransfer(this).product(product).build();
        item.changeQty(boxCount, looseQty);
        items.add(item);
    }

    public int getTotalQty() { return items.stream().mapToInt(StockTransferItem::getQuantity).sum(); }

    public void ship(Long adminId) {
        if (status != TransferStatus.REQUESTED) throw new BusinessException(INVALID_STATUS);
        status = TransferStatus.SHIPPED;
        shippedBy = adminId;
        shippedAt = LocalDateTime.now();
    }

    public void receive(Long adminId) {
        if (status != TransferStatus.SHIPPED) throw new BusinessException(INVALID_STATUS);
        status = TransferStatus.RECEIVED;
        receivedBy = adminId;
        receivedAt = LocalDateTime.now();
    }

    // 출고 전(요청 상태)에만 취소. 출고 후에는 재고가 이미 빠져서 도착 확인 후 조정으로 처리
    public void cancel() {
        if (status != TransferStatus.REQUESTED) throw new BusinessException(INVALID_STATUS);
        status = TransferStatus.CANCELLED;
    }
}
