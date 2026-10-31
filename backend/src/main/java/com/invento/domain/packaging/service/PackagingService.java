package com.invento.domain.packaging.service;

import com.invento.domain.packaging.dto.PackagingDto.Request;
import com.invento.domain.packaging.dto.PackagingDto.Response;
import com.invento.domain.packaging.entity.Packaging;
import com.invento.domain.packaging.repository.PackagingRepository;
import com.invento.domain.stock.dto.StockDto.PackagingPurchaseRequest;
import com.invento.domain.stock.dto.UsedQty;
import com.invento.domain.stock.entity.PackagingStock;
import com.invento.domain.stock.repository.PackagingStockRepository;
import com.invento.domain.stock.repository.StockMovementRepository;
import com.invento.domain.stock.service.StockService;
import com.invento.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.invento.domain.stock.entity.StockItemType.PACKAGING;
import static com.invento.domain.stock.entity.StockMovementType.PACKAGING_OUT;
import static com.invento.global.exception.ErrorCode.NOT_FOUND;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
@Transactional
public class PackagingService {

    private final PackagingRepository repo;
    private final PackagingStockRepository stockRepo;
    private final StockMovementRepository movementRepo;
    private final StockService stockService;

    @Transactional(readOnly = true)
    public List<Response> findAll() {
        List<Packaging> list = repo.findByDeletedAtIsNullOrderByUsageTypeAscPackagingNameAsc();
        List<Long> ids = list.stream().map(Packaging::getPackagingId).toList();
        Map<Long, List<PackagingStock>> stocks = stockService.groupVisible(stockRepo.findByPackagingPackagingIdIn(ids),
                s -> s.getPackaging().getPackagingId());
        // 포장재별 → 위치별 사용 합계 (원장에 음수로 기록되므로 부호 반대로)
        Map<Long, Map<Long, Long>> used = movementRepo.sumByItemAndLocation(PACKAGING, PACKAGING_OUT, ids).stream()
                .collect(groupingBy(UsedQty::itemId, toMap(UsedQty::locationId, u -> -u.qty())));
        return list.stream().map(p -> Response.of(p, stocks.getOrDefault(p.getPackagingId(), List.of()),
                used.getOrDefault(p.getPackagingId(), Map.of()))).toList();
    }

    // 등록 + 초기 수량이 있으면 그 위치에 구매 입고
    public Response create(Request r) {
        Packaging p = repo.save(fill(Packaging.builder().build(), r));
        if (r.initialQty() != null && r.initialQty() > 0 && r.locationId() != null) {
            stockService.purchasePackaging(new PackagingPurchaseRequest(p.getPackagingId(), r.locationId(), r.initialQty(), "최초 등록"));
        }
        return Response.of(p, stockRepo.findByPackagingPackagingIdIn(List.of(p.getPackagingId())), Map.of());
    }

    public Response update(Long id, Request r) {
        return Response.of(fill(getActive(id), r), stockRepo.findByPackagingPackagingIdIn(List.of(id)), Map.of());
    }

    // 주문 포장 기록에서 참조하므로 deleted_at만 기록
    public void delete(Long id) { getActive(id).delete(); }

    private Packaging fill(Packaging p, Request r) {
        p.update(r.packagingName(), r.usageType(), r.packagingType(), r.unitCost(), r.purchaseUrl());
        return p;
    }

    private Packaging getActive(Long id) {
        Packaging p = repo.getOrThrow(id);
        if (p.getDeletedAt() != null) throw new BusinessException(NOT_FOUND);
        return p;
    }
}
