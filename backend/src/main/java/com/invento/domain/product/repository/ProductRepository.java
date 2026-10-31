package com.invento.domain.product.repository;

import com.invento.domain.product.entity.Product;
import com.invento.global.common.BaseRepository;

public interface ProductRepository extends BaseRepository<Product>, ProductRepositoryCustom {

    boolean existsByProductCode(String productCode);

    boolean existsByProductCodeAndProductIdNot(String productCode, Long productId);
}
