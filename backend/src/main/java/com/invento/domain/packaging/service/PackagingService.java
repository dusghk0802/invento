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

import java.util.List;
import java.util.Map;

import static com.invento.global.exception.ErrorCode.NOT_FOUND;

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
        Map<Long, List<PackagingStock>> stocks = stockService.groupVisible(
                stockRepo.findByPackagingPackagingIdIn(list.stream().map(Packaging::getPackagingId).toList()),
                s -> s.getPackaging().getPackagingId());
        return list.stream().map(p -> Response.of(p, stocks.getOrDefault(p.getPackagingId(), List.of()))).toList();
    }

    public Response create(Request r) {
        return Response.of(repo.save(fill(Packaging.builder().build(), r)), List.of());
    }

    public Response update(Long id, Request r) {
        return Response.of(fill(getActive(id), r), stockRepo.findByPackagingPackagingIdIn(List.of(id)));
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
