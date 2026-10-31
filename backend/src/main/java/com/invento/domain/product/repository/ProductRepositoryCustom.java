package com.invento.domain.product.repository;

import com.invento.domain.product.dto.ProductDto.Search;
import com.invento.domain.product.entity.Product;

import java.util.List;

public interface ProductRepositoryCustom {

    List<Product> search(Search search);
}
