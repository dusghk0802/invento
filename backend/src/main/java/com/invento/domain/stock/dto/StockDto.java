package com.invento.domain.stock.dto;

import com.invento.domain.stock.entity.AdjustReason;
import com.invento.domain.stock.entity.StockMovement;
import com.invento.domain.stock.entity.StockMovementType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class StockDto {

    // 재고 조정: quantity는 증감값 (예: 파손 2개 → -2, 실사 결과 1개 더 있음 → +1)
    public record AdjustRequest(
            @NotNull Long productId,
            @NotNull Long locationId,
            @NotNull Integer quantity,
            @NotNull AdjustReason reason,
            @Size(max = 500) String memo) {
    }

    public record MinimumRequest(
            @NotNull Long productId,
            @NotNull Long locationId,
            @NotNull @PositiveOrZero Integer minimumQty) {
    }

    public record PackagingAdjustRequest(
            @NotNull Long packagingId,
            @NotNull Long locationId,
            @NotNull Integer quantity,
            @NotNull AdjustReason reason,
            @Size(max = 500) String memo) {
    }

    // 포장재 구매 입고 (등록할 때 초기 수량도 이걸로)
    public record PackagingPurchaseRequest(@NotNull Long packagingId, @NotNull Long locationId,
                                           @NotNull @Positive Integer quantity, @Size(max = 500) String memo) {}

    public record PackagingMinimumRequest(
            @NotNull Long packagingId,
            @NotNull Long locationId,
            @NotNull @PositiveOrZero Integer minimumQty) {
    }

    public record MovementResponse(Long stockMovementId, Long locationId, String locationName,
                                   StockMovementType movementType, int quantity, AdjustReason adjustReason,
                                   String refType, Long refId, String memo, LocalDateTime createdAt) {
        public static MovementResponse from(StockMovement m) {
            var l = m.getLocation();
            return new MovementResponse(m.getStockMovementId(), l == null ? null : l.getLocationId(),
                    l == null ? null : l.getLocationName(), m.getMovementType(), m.getQuantity(),
                    m.getAdjustReason(), m.getRefType(), m.getRefId(), m.getMemo(), m.getCreatedAt());
        }
    }
}
