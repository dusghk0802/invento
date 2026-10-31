package com.invento.domain.stock.repository;

import com.invento.domain.stock.entity.PackagingStock;
import com.invento.global.common.BaseRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PackagingStockRepository extends BaseRepository<PackagingStock> {

    @EntityGraph(attributePaths = "location")
    List<PackagingStock> findByPackagingPackagingIdIn(Collection<Long> packagingIds);

    Optional<PackagingStock> findByPackagingPackagingIdAndLocationLocationId(Long packagingId, Long locationId);
}
