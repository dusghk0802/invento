package com.invento.domain.product.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.invento.domain.product.entity.ItemKind;
import com.invento.domain.product.entity.Product;
import com.invento.domain.stock.entity.ProductStock;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public class ProductDto {

    public record Request(
            @NotNull ItemKind itemKind,
            @NotBlank @Size(max = 50) String productCode,
            Long seriesId,
            @NotNull Long productTypeId,
            @NotBlank @Size(max = 200) String productName,
            @Size(max = 1000) String imageUrl,
            String description,
            @NotNull Boolean shopVisible,
            @NotNull @PositiveOrZero Long costPrice,
            @NotNull @PositiveOrZero Long sellingPrice,
            LocalDate releaseDate,
            @Positive Integer boxQty) {
    }

    // 목록 검색 조건 (모두 선택)
    public record Search(String keyword, Long seriesId, Long productTypeId, ItemKind itemKind) {
    }

    public record StockResponse(Long locationId, String locationName, int onHandQty, int reservedQty,
                                int availableQty, int minimumQty) {
        public static StockResponse from(ProductStock s) {
            return new StockResponse(s.getLocation().getLocationId(), s.getLocation().getLocationName(),
                    s.getOnHandQty(), s.getReservedQty(), s.getAvailableQty(), s.getMinimumQty());
        }
    }

    // costPrice는 사장이 아니면 null → JSON에서 아예 빠짐
    public record Response(Long productId, ItemKind itemKind, String productCode, Long seriesId, String seriesName,
                           Long productTypeId, String productTypeName, String productName, String imageUrl,
                           String description, boolean shopVisible,
                           @JsonInclude(JsonInclude.Include.NON_NULL) Long costPrice,
                           long sellingPrice, LocalDate releaseDate, Integer boxQty, List<StockResponse> stocks) {
        public static Response of(Product p, List<ProductStock> stocks, boolean showCost) {
            var series = p.getSeries();
            return new Response(p.getProductId(), p.getItemKind(), p.getProductCode(),
                    series == null ? null : series.getSeriesId(), series == null ? null : series.getSeriesName(),
                    p.getProductType().getProductTypeId(), p.getProductType().getProductTypeName(),
                    p.getProductName(), p.getImageUrl(), p.getDescription(), p.isShopVisible(),
                    showCost ? p.getCostPrice() : null, p.getSellingPrice(), p.getReleaseDate(), p.getBoxQty(),
                    stocks.stream().map(StockResponse::from).toList());
        }
    }
}
