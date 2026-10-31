package com.invento.domain.production.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.invento.domain.production.entity.ProductionOrder;
import com.invento.domain.production.entity.ProductionOrderItem;
import com.invento.domain.production.entity.ProductionReceipt;
import com.invento.domain.production.entity.ProductionStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class ProductionDto {

    public record ItemRequest(@NotNull Long productId, @NotNull @Positive Integer orderedQty) {
    }

    public record Request(
            @NotNull Long supplierId,
            @NotNull LocalDate orderDate,
            LocalDate expectedDate,
            @Size(max = 500) String memo,
            @NotEmpty List<@Valid ItemRequest> items) {
    }

    public record ConfirmRequest(@NotNull LocalDate confirmedDate) {}

    public record Search(ProductionStatus status, Long supplierId) {
    }

    // 입고 처리: 항목마다 정상·불량 수량 (둘 다 0인 항목은 건너뜀)
    public record ReceiveItem(@NotNull Long productionOrderItemId,
                              @NotNull @PositiveOrZero Integer receivedQty,
                              @NotNull @PositiveOrZero Integer defectiveQty) {
    }

    public record CorrectRequest(@NotNull @PositiveOrZero Integer receivedQty,
                                 @NotNull @PositiveOrZero Integer defectiveQty) {
    }

    public record ReceiveRequest(@NotNull Long locationId, @NotNull LocalDate receivedDate,
                                 @NotEmpty List<@Valid ReceiveItem> items) {
    }

    // 목록 한 줄. totalCost(발주 금액 = 발주 수량 × 원가)는 사장에게만
    public record Summary(Long productionOrderId, Long supplierId, String supplierName, String supplierContact,
                          LocalDate orderDate, LocalDate expectedDate, LocalDate confirmedDate, ProductionStatus status,
                          boolean delayed, String memo,
                          int itemCount, int orderedQty, int receivedQty, int defectiveQty,
                          @JsonInclude(NON_NULL) Long totalCost) {
        public static Summary of(ProductionOrder o, boolean showCost) {
            var items = o.getItems();
            return new Summary(o.getProductionOrderId(), o.getSupplier().getSupplierId(),
                    o.getSupplier().getSupplierName(), o.getSupplier().getContact(), o.getOrderDate(), o.getExpectedDate(),
                    o.getConfirmedDate(), o.getStatus(), o.isDelayed(), o.getMemo(), items.size(),
                    items.stream().mapToInt(ProductionOrderItem::getOrderedQty).sum(),
                    items.stream().mapToInt(ProductionOrderItem::getReceivedQty).sum(),
                    items.stream().mapToInt(ProductionOrderItem::getDefectiveQty).sum(),
                    showCost ? items.stream().mapToLong(i -> i.getOrderedQty() * i.getProduct().getCostPrice()).sum() : null);
        }
    }

    public record ItemResponse(Long productionOrderItemId, Long productId, String productCode, String productName,
                               int orderedQty, int receivedQty, int defectiveQty, int remainingQty,
                               @JsonInclude(NON_NULL) Long costPrice) {
        public static ItemResponse of(ProductionOrderItem i, boolean showCost) {
            var p = i.getProduct();
            return new ItemResponse(i.getProductionOrderItemId(), p.getProductId(), p.getProductCode(),
                    p.getProductName(), i.getOrderedQty(), i.getReceivedQty(), i.getDefectiveQty(),
                    i.getRemainingQty(), showCost ? p.getCostPrice() : null);
        }
    }

    public record ReceiptResponse(Long productionReceiptId, String productName, Long locationId, String locationName,
                                  LocalDate receivedDate, int receivedQty, int defectiveQty) {
        public static ReceiptResponse from(ProductionReceipt r) {
            return new ReceiptResponse(r.getProductionReceiptId(), r.getOrderItem().getProduct().getProductName(),
                    r.getLocation().getLocationId(), r.getLocation().getLocationName(), r.getReceivedDate(), r.getReceivedQty(), r.getDefectiveQty());
        }
    }

    public record Detail(Summary order, List<ItemResponse> items, List<ReceiptResponse> receipts) {
    }
}
