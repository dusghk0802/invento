package com.invento.domain.stock.repository;

import com.invento.domain.stock.dto.UsedQty;
import com.invento.domain.stock.entity.StockItemType;
import com.invento.domain.stock.entity.StockMovement;
import com.invento.domain.stock.entity.StockMovementType;
import com.invento.global.common.BaseRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface StockMovementRepository extends BaseRepository<StockMovement> {

    @EntityGraph(attributePaths = "location")
    List<StockMovement> findTop200ByItemTypeAndItemIdOrderByStockMovementIdDesc(StockItemType itemType, Long itemId);

    // 품목·위치별 수량 합계 (예: 포장재 사용 합계 = PACKAGING_OUT, 출고라서 음수)
    @Query("""
            select new com.invento.domain.stock.dto.UsedQty(m.itemId, m.location.locationId, sum(m.quantity))
            from StockMovement m where m.itemType = :itemType and m.movementType = :type and m.itemId in :itemIds
            group by m.itemId, m.location.locationId""")
    List<UsedQty> sumByItemAndLocation(StockItemType itemType, StockMovementType type, Collection<Long> itemIds);
}
