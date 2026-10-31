package com.invento.domain.packaging.service;

import com.invento.domain.packaging.dto.PackagingDto.Request;
import com.invento.domain.packaging.dto.PackagingDto.Response;
import com.invento.domain.packaging.entity.Packaging;
import com.invento.domain.packaging.repository.PackagingRepository;
import com.invento.domain.stock.entity.PackagingStock;
import com.invento.domain.stock.repository.PackagingStockRepository;
import com.invento.domain.stock.service.StockService;
import com.invento.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.invento.global.exception.ErrorCode.NOT_FOUND;
import static java.util.stream.Collectors.groupingBy;

@Service
@RequiredArgsConstructor
@Transactional
public class PackagingService {

    private final PackagingRepository repo;
    private final PackagingStockRepository stockRepo;
    private final StockService stockService;

    @Transactional(readOnly = true)
    public List<Response> findAll() {
        List<Packaging> list = repo.findByDeletedAtIsNullOrderByUsageTypeAscPackagingNameAsc();
        var visible = stockService.visibleLocations(); // 권한별로 볼 수 있는 위치의 재고만
        Map<Long, List<PackagingStock>> stocks = stockRepo
                .findByPackagingPackagingIdIn(list.stream().map(Packaging::getPackagingId).toList()).stream()
                .filter(s -> visible.test(s.getLocation()))
                .sorted(Comparator.comparing(s -> s.getLocation().getLocationId()))
                .collect(groupingBy(s -> s.getPackaging().getPackagingId()));
        return list.stream().map(p -> Response.of(p, stocks.getOrDefault(p.getPackagingId(), List.of()))).toList();
    }

    public Response create(Request r) {
        return Response.of(repo.save(Packaging.builder()
                .packagingName(r.packagingName()).usageType(r.usageType()).packagingType(r.packagingType())
                .unitCost(r.unitCost()).purchaseUrl(r.purchaseUrl()).build()), List.of());
    }

    public Response update(Long id, Request r) {
        Packaging p = getActive(id);
        p.update(r.packagingName(), r.usageType(), r.packagingType(), r.unitCost(), r.purchaseUrl());
        return Response.of(p, stockRepo.findByPackagingPackagingIdIn(List.of(id)));
    }

    // 주문 포장 기록에서 참조하므로 deleted_at만 기록
    public void delete(Long id) {
        getActive(id).delete();
    }

    private Packaging getActive(Long id) {
        Packaging p = repo.getOrThrow(id);
        if (p.getDeletedAt() != null) throw new BusinessException(NOT_FOUND);
        return p;
    }
}
