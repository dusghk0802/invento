package com.invento.domain.gift.repository;

import com.invento.domain.gift.entity.GiftRule;
import com.invento.global.common.BaseRepository;

public interface GiftRuleRepository extends BaseRepository<GiftRule> {

    boolean existsByMinimumAmount(long minimumAmount);

    boolean existsByMinimumAmountAndGiftRuleIdNot(long minimumAmount, Long giftRuleId);
}
