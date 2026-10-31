package com.invento.domain.shipping.repository;

import com.invento.domain.shipping.entity.ShippingMethod;
import com.invento.global.common.BaseRepository;

public interface ShippingMethodRepository extends BaseRepository<ShippingMethod> {

    boolean existsByShippingMethodName(String shippingMethodName);

    boolean existsByShippingMethodNameAndShippingMethodIdNot(String shippingMethodName, Long shippingMethodId);
}
