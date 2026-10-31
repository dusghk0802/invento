package com.invento.domain.production.repository;

import com.invento.domain.production.dto.ProductionDto.Search;
import com.invento.domain.production.entity.ProductionOrder;

import java.util.List;

public interface ProductionOrderRepositoryCustom {

    List<ProductionOrder> search(Search search);
}
