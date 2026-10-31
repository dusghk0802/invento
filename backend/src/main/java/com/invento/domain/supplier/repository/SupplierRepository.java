package com.invento.domain.supplier.repository;

import com.invento.domain.supplier.entity.Supplier;
import com.invento.global.common.BaseRepository;

public interface SupplierRepository extends BaseRepository<Supplier> {

    boolean existsBySupplierName(String supplierName);

    boolean existsBySupplierNameAndSupplierIdNot(String supplierName, Long supplierId);
}
