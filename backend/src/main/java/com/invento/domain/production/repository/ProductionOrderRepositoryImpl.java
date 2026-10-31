package com.invento.domain.production.repository;

import com.invento.domain.production.dto.ProductionDto.Search;
import com.invento.domain.production.entity.ProductionOrder;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.invento.domain.production.entity.QProductionOrder.productionOrder;
import static com.invento.domain.supplier.entity.QSupplier.supplier;

// 발주 검색: 상태·공급처 (비어 있으면 무시). 항목은 default_batch_fetch_size로 묶어서 조회
@RequiredArgsConstructor
public class ProductionOrderRepositoryImpl implements ProductionOrderRepositoryCustom {

    private final JPAQueryFactory query;

    @Override
    public List<ProductionOrder> search(Search s) {
        BooleanBuilder where = new BooleanBuilder();
        if (s.status() != null) where.and(productionOrder.status.eq(s.status()));
        if (s.supplierId() != null) where.and(productionOrder.supplier.supplierId.eq(s.supplierId()));

        return query.selectFrom(productionOrder)
                .join(productionOrder.supplier, supplier).fetchJoin()
                .where(where)
                .orderBy(productionOrder.productionOrderId.desc())
                .fetch();
    }
}
