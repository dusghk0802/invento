package com.invento.domain.packaging.repository;

import com.invento.domain.packaging.entity.Packaging;
import com.invento.global.common.BaseRepository;

import java.util.List;

public interface PackagingRepository extends BaseRepository<Packaging> {

    List<Packaging> findByDeletedAtIsNullOrderByUsageTypeAscPackagingNameAsc();
}
