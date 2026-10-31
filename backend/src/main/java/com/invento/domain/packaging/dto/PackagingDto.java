package com.invento.domain.packaging.dto;

import com.invento.domain.packaging.entity.Packaging;
import com.invento.domain.packaging.entity.PackagingUsage;
import com.invento.domain.stock.entity.PackagingStock;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

public class PackagingDto {

    public record Request(
            @NotBlank @Size(max = 200) String packagingName,
            @NotNull PackagingUsage usageType,
            @NotBlank @Size(max = 100) String packagingType,
            @NotNull @PositiveOrZero Long unitCost,
            @Size(max = 1000) String purchaseUrl) {
    }

    public record StockResponse(Long locationId, String locationName, int onHandQty, int minimumQty) {
        public static StockResponse from(PackagingStock s) {
            return new StockResponse(s.getLocation().getLocationId(), s.getLocation().getLocationName(),
                    s.getOnHandQty(), s.getMinimumQty());
        }
    }

    public record Response(Long packagingId, String packagingName, PackagingUsage usageType, String packagingType,
                           long unitCost, String purchaseUrl, List<StockResponse> stocks) {
        public static Response of(Packaging p, List<PackagingStock> stocks) {
            return new Response(p.getPackagingId(), p.getPackagingName(), p.getUsageType(), p.getPackagingType(),
                    p.getUnitCost(), p.getPurchaseUrl(), stocks.stream().map(StockResponse::from).toList());
        }
    }
}
