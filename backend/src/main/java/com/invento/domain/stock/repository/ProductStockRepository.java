package com.invento.domain.stock.repository;

import com.invento.domain.stock.entity.ProductStock;
import com.invento.global.common.BaseRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductStockRepository extends BaseRepository<ProductStock> {

    // 상품 목록의 위치별 재고를 한 번에 조회 (상품마다 쿼리 나가지 않게)
    @EntityGraph(attributePaths = "location")
    List<ProductStock> findByProductProductIdIn(Collection<Long> productIds);

    Optional<ProductStock> findByProductProductIdAndLocationLocationId(Long productId, Long locationId);
}
