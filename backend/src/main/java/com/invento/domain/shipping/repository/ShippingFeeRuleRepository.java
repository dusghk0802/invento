package com.invento.domain.shipping.repository;

import com.invento.domain.shipping.entity.ShippingFeeRule;
import com.invento.global.common.BaseRepository;

import java.util.List;

public interface ShippingFeeRuleRepository extends BaseRepository<ShippingFeeRule> {

    List<ShippingFeeRule> findByShippingMethodShippingMethodId(Long shippingMethodId);

    void deleteByShippingMethodShippingMethodId(Long shippingMethodId);
}
