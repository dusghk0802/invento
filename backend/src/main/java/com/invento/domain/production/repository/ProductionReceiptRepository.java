package com.invento.domain.production.repository;

import com.invento.domain.production.entity.ProductionReceipt;
import com.invento.global.common.BaseRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface ProductionReceiptRepository extends BaseRepository<ProductionReceipt> {

    @EntityGraph(attributePaths = {"location", "orderItem.product"})
    List<ProductionReceipt> findByOrderItemProductionOrderProductionOrderIdOrderByProductionReceiptIdDesc(Long productionOrderId);
}
