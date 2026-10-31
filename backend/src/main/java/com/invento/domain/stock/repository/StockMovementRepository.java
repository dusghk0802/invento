package com.invento.domain.stock.repository;

import com.invento.domain.stock.entity.StockItemType;
import com.invento.domain.stock.entity.StockMovement;
import com.invento.global.common.BaseRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface StockMovementRepository extends BaseRepository<StockMovement> {

    @EntityGraph(attributePaths = "location")
    List<StockMovement> findTop200ByItemTypeAndItemIdOrderByStockMovementIdDesc(StockItemType itemType, Long itemId);
}
