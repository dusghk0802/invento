package com.invento.domain.product.repository;

import com.invento.domain.product.entity.Product;
import com.invento.global.common.BaseRepository;

import java.util.Optional;

public interface ProductRepository extends BaseRepository<Product>, ProductRepositoryCustom {

    boolean existsByProductCode(String productCode);

    boolean existsByProductCodeAndProductIdNot(String productCode, Long productId);

    Optional<Product> findByBarcodeAndDeletedAtIsNull(String barcode);

    Optional<Product> findByProductCodeAndDeletedAtIsNull(String productCode);
}
