package com.invento.domain.production.entity;

import com.invento.domain.product.entity.Product;
import com.invento.domain.supplier.entity.Supplier;
import com.invento.global.common.BaseEntity;
import com.invento.global.exception.BusinessException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.invento.global.exception.ErrorCode.INVALID_STATUS;

// 공장 발주. 항목(상품·수량)은 입고 전(ORDERED)에만 수정 가능
@Getter
@Entity
@Table(name = "production_orders")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductionOrder extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productionOrderId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(nullable = false)
    private LocalDate orderDate;

    private LocalDate expectedDate; // 입고 예정일

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductionStatus status = ProductionStatus.ORDERED;

    @Column(length = 500)
    private String memo;

    @Column(nullable = false)
    private Long createdBy;

    @Builder.Default
    @OneToMany(mappedBy = "productionOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("productionOrderItemId")
    private List<ProductionOrderItem> items = new ArrayList<>();

    public void addItem(Product product, int orderedQty) {
        items.add(ProductionOrderItem.builder().productionOrder(this).product(product).orderedQty(orderedQty).build());
    }

    // 발주 내용 수정: 입고가 하나도 없을 때만. 항목은 비우고 다시 추가
    public void update(Supplier supplier, LocalDate orderDate, LocalDate expectedDate, String memo) {
        if (status != ProductionStatus.ORDERED) throw new BusinessException(INVALID_STATUS);
        this.supplier = supplier;
        this.orderDate = orderDate;
        this.expectedDate = expectedDate;
        this.memo = memo;
        items.clear();
    }

    // 취소: 일부 입고된 발주도 가능 (남은 수량은 더 이상 안 들어옴, 입고된 재고는 그대로)
    public void cancel() {
        if (!isOpen()) throw new BusinessException(INVALID_STATUS);
        status = ProductionStatus.CANCELLED;
    }

    public void checkReceivable() {
        if (!isOpen()) throw new BusinessException(INVALID_STATUS);
    }

    // 입고 수량 수정은 입고 완료 후에도 가능 (취소된 발주만 불가)
    public void checkCorrectable() {
        if (status == ProductionStatus.CANCELLED) throw new BusinessException(INVALID_STATUS);
    }

    // 입고 후 호출: 전부 처리됐으면 입고 완료, 일부면 일부 입고
    public void refreshStatus() {
        boolean done = items.stream().allMatch(i -> i.getRemainingQty() == 0);
        boolean started = items.stream().anyMatch(i -> i.getReceivedQty() + i.getDefectiveQty() > 0);
        status = done ? ProductionStatus.RECEIVED : started ? ProductionStatus.PARTIAL : ProductionStatus.ORDERED;
    }

    // 입고 예정일이 지났는데 아직 다 안 들어온 발주
    public boolean isDelayed() {
        return isOpen() && expectedDate != null && expectedDate.isBefore(LocalDate.now());
    }

    private boolean isOpen() {
        return status == ProductionStatus.ORDERED || status == ProductionStatus.PARTIAL;
    }
}
