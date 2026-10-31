package com.invento.domain.order.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.invento.domain.order.entity.OrderItem;
import com.invento.domain.order.entity.PaymentMethod;
import com.invento.domain.order.entity.SalesOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

// 매장 판매(영수증). 원가·이익은 사장에게만 (null이면 JSON에서 빠짐)
public class StoreSaleDto {

    public record ItemRequest(@NotNull Long productId, @NotNull @Positive Integer quantity) {}

    public record Request(@NotNull Long locationId, @NotNull PaymentMethod paymentMethod, @NotEmpty List<@Valid ItemRequest> items) {}

    // 기간·매장 조회 (비우면 오늘, 사장은 전체 매장 / 매장 직원은 담당 매장)
    public record Search(Long locationId, LocalDate from, LocalDate to) {}

    public record Summary(Long orderId, String orderNo, LocalDateTime orderDate, Long locationId, String locationName,
                          int itemCount, int totalQty, long totalAmount, long receivedAmount,
                          PaymentMethod paymentMethod, @JsonInclude(NON_NULL) Long finalProfit) {
        public static Summary of(SalesOrder o, boolean owner) {
            return new Summary(o.getOrderId(), o.getOrderNo(), o.getOrderDate(), o.getLocation().getLocationId(),
                    o.getLocation().getLocationName(), o.getItems().size(),
                    o.getItems().stream().mapToInt(OrderItem::getQuantity).sum(), o.getTotalAmount(), o.getReceivedAmount(),
                    o.getPaymentMethod(), owner ? o.getFinalProfit() : null);
        }
    }

    public record ItemResponse(Long productId, String productCode, String productName, int quantity, long unitPrice,
                               long lineTotalAmount, @JsonInclude(NON_NULL) Long unitCost, @JsonInclude(NON_NULL) Long lineProfit) {
        public static ItemResponse of(OrderItem i, boolean owner) {
            var p = i.getProduct();
            return new ItemResponse(p.getProductId(), p.getProductCode(), p.getProductName(), i.getQuantity(),
                    i.getUnitPrice(), i.getLineTotalAmount(), owner ? i.getUnitCost() : null, owner ? i.getLineProfit() : null);
        }
    }

    public record Detail(Summary receipt, List<ItemResponse> items) {
        public static Detail of(SalesOrder o, boolean owner) {
            return new Detail(Summary.of(o, owner), o.getItems().stream().map(i -> ItemResponse.of(i, owner)).toList());
        }
    }
}
