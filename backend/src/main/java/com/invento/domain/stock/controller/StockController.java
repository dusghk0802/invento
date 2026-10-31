package com.invento.domain.stock.controller;

import com.invento.domain.packaging.dto.PackagingDto;
import com.invento.domain.product.dto.ProductDto.StockResponse;
import com.invento.domain.stock.dto.StockDto.*;
import com.invento.domain.stock.entity.StockItemType;
import com.invento.domain.stock.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService service;

    @PostMapping("/adjust")
    public StockResponse adjust(@Valid @RequestBody AdjustRequest request) {
        return service.adjust(request);
    }

    @PutMapping("/minimum")
    public StockResponse changeMinimum(@Valid @RequestBody MinimumRequest request) {
        return service.changeMinimum(request);
    }

    @PostMapping("/packaging/adjust")
    public PackagingDto.StockResponse adjustPackaging(@Valid @RequestBody PackagingAdjustRequest request) {
        return service.adjustPackaging(request);
    }

    @PutMapping("/packaging/minimum")
    public PackagingDto.StockResponse changePackagingMinimum(@Valid @RequestBody PackagingMinimumRequest request) {
        return service.changePackagingMinimum(request);
    }

    // 재고 변동 이력 (최근 200건). 예: ?productId=1 또는 ?packagingId=3
    @GetMapping("/movements")
    public List<MovementResponse> movements(@RequestParam(required = false) Long productId,
                                            @RequestParam(required = false) Long packagingId) {
        return productId != null ? service.movements(StockItemType.PRODUCT, productId)
                : service.movements(StockItemType.PACKAGING, packagingId);
    }
}
