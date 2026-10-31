package com.invento.domain.stock.controller;

import com.invento.domain.stock.dto.StockDto.*;
import com.invento.domain.stock.entity.StockItemType;
import com.invento.domain.stock.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 재고 조정·최소 재고 (상품 / 포장재). 저장 후 화면은 목록을 다시 불러오므로 응답 본문 없음
@RestController
@RequestMapping("/api/admin/stocks")
@RequiredArgsConstructor
@ResponseStatus(HttpStatus.NO_CONTENT)
public class StockController {

    private final StockService service;

    @PostMapping("/adjust")
    public void adjust(@Valid @RequestBody AdjustRequest request) { service.adjust(request); }

    @PutMapping("/minimum")
    public void changeMinimum(@Valid @RequestBody MinimumRequest request) { service.changeMinimum(request); }

    @PostMapping("/packaging/adjust")
    public void adjustPackaging(@Valid @RequestBody PackagingAdjustRequest request) {
        service.adjustPackaging(request);
    }

    @PostMapping("/packaging/purchase")
    public void purchasePackaging(@Valid @RequestBody PackagingPurchaseRequest request) { service.purchasePackaging(request); }

    @PutMapping("/packaging/minimum")
    public void changePackagingMinimum(@Valid @RequestBody PackagingMinimumRequest request) {
        service.changePackagingMinimum(request);
    }

    // 재고 변동 이력 (최근 200건). 예: ?productId=1 또는 ?packagingId=3
    @GetMapping("/movements")
    @ResponseStatus(HttpStatus.OK)
    public List<MovementResponse> movements(@RequestParam(required = false) Long productId,
                                            @RequestParam(required = false) Long packagingId) {
        return productId != null ? service.movements(StockItemType.PRODUCT, productId)
                : service.movements(StockItemType.PACKAGING, packagingId);
    }
}
