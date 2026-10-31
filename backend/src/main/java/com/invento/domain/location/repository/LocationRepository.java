package com.invento.domain.location.repository;

import com.invento.domain.location.entity.Location;
import com.invento.global.common.BaseRepository;

public interface LocationRepository extends BaseRepository<Location> {

    boolean existsByLocationName(String locationName);

    boolean existsByLocationNameAndLocationIdNot(String locationName, Long locationId);
}
