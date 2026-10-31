package com.invento.domain.packaging.dto;

import com.invento.domain.packaging.entity.Packaging;
import com.invento.domain.packaging.entity.PackagingUsage;
import com.invento.domain.stock.entity.PackagingStock;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

public class PackagingDto {

    public record Request(
            @NotBlank @Size(max = 200) String packagingName,
            @NotNull PackagingUsage usageType,
            @NotBlank @Size(max = 100) String packagingType,
            @NotNull @PositiveOrZero Long unitCost,
            @Size(max = 1000) String purchaseUrl,
            Long locationId, @PositiveOrZero Integer initialQty) { // 등록할 때만: 처음 사 온 수량과 위치
    }

    // usedQty: 그 위치에서 포장에 쓴 합계
    public record StockResponse(Long locationId, String locationName, int onHandQty, int minimumQty, long usedQty) {
        public static StockResponse of(PackagingStock s, Map<Long, Long> usedByLocation) {
            Long locationId = s.getLocation().getLocationId();
            return new StockResponse(locationId, s.getLocation().getLocationName(), s.getOnHandQty(), s.getMinimumQty(),
                    usedByLocation.getOrDefault(locationId, 0L));
        }
    }

    public record Response(Long packagingId, String packagingName, PackagingUsage usageType, String packagingType,
                           long unitCost, String purchaseUrl, List<StockResponse> stocks) {
        public static Response of(Packaging p, List<PackagingStock> stocks, Map<Long, Long> usedByLocation) {
            return new Response(p.getPackagingId(), p.getPackagingName(), p.getUsageType(), p.getPackagingType(),
                    p.getUnitCost(), p.getPurchaseUrl(), stocks.stream().map(s -> StockResponse.of(s, usedByLocation)).toList());
        }
    }
}
