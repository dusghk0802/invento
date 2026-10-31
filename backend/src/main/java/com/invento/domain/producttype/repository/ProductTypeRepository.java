package com.invento.domain.producttype.repository;

import com.invento.domain.producttype.entity.ProductType;
import com.invento.global.common.BaseRepository;

public interface ProductTypeRepository extends BaseRepository<ProductType> {

    boolean existsByProductTypeName(String productTypeName);

    boolean existsByProductTypeNameAndProductTypeIdNot(String productTypeName, Long productTypeId);
}
