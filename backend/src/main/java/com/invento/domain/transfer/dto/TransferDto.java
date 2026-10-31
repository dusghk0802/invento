package com.invento.domain.transfer.dto;

import com.invento.domain.transfer.entity.StockTransfer;
import com.invento.domain.transfer.entity.StockTransferItem;
import com.invento.domain.transfer.entity.TransferStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public class TransferDto {

    public record ItemRequest(@NotNull Long productId,
                              @NotNull @PositiveOrZero Integer boxCount,
                              @NotNull @PositiveOrZero Integer looseQty) {
    }

    public record Request(@NotNull Long fromLocationId, @NotNull Long toLocationId, @Size(max = 500) String memo,
                          @NotEmpty List<@Valid ItemRequest> items) {
    }

    // 출고 처리: 창고에서 실제로 보내는 박스·낱개 수 (안 보내면 요청 수량 그대로)
    public record ShipItem(@NotNull Long stockTransferItemId,
                           @NotNull @PositiveOrZero Integer boxCount,
                           @NotNull @PositiveOrZero Integer looseQty) {
    }

    public record ShipRequest(List<@Valid ShipItem> items) {
    }

    public record RejectRequest(@NotBlank @Size(max = 500) String reason) {}

    public record Summary(Long stockTransferId, Long fromLocationId, String fromLocationName, Long toLocationId,
                          String toLocationName, TransferStatus status, int itemCount, int totalQty,
                          LocalDateTime requestedAt, LocalDateTime decidedAt, String rejectReason,
                          LocalDateTime shippedAt, LocalDateTime receivedAt, String memo) {
        public static Summary from(StockTransfer t) {
            return new Summary(t.getStockTransferId(), t.getFromLocation().getLocationId(),
                    t.getFromLocation().getLocationName(), t.getToLocation().getLocationId(),
                    t.getToLocation().getLocationName(), t.getStatus(), t.getItems().size(), t.getTotalQty(),
                    t.getCreatedAt(), t.getDecidedAt(), t.getRejectReason(), t.getShippedAt(), t.getReceivedAt(), t.getMemo());
        }
    }

    public record ItemResponse(Long stockTransferItemId, Long productId, String productCode, String productName,
                               Integer boxQty, int boxCount, int looseQty, int quantity) {
        public static ItemResponse from(StockTransferItem i) {
            var p = i.getProduct();
            return new ItemResponse(i.getStockTransferItemId(), p.getProductId(), p.getProductCode(),
                    p.getProductName(), p.getBoxQty(), i.getBoxCount(), i.getLooseQty(), i.getQuantity());
        }
    }

    public record Detail(Summary transfer, List<ItemResponse> items) {
        public static Detail from(StockTransfer t) {
            return new Detail(Summary.from(t), t.getItems().stream().map(ItemResponse::from).toList());
        }
    }
}
