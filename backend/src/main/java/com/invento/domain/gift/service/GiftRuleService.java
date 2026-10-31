package com.invento.domain.gift.service;

import com.invento.domain.gift.dto.GiftRuleDto.Request;
import com.invento.domain.gift.dto.GiftRuleDto.Response;
import com.invento.domain.gift.entity.GiftRule;
import com.invento.domain.gift.repository.GiftRuleRepository;
import com.invento.domain.producttype.entity.ProductType;
import com.invento.domain.producttype.repository.ProductTypeRepository;
import com.invento.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.invento.global.exception.ErrorCode.DUPLICATE_GIFT_AMOUNT;
import static com.invento.global.exception.ErrorCode.GIFT_TYPE_REQUIRED;

@Service
@RequiredArgsConstructor
@Transactional
public class GiftRuleService {

    private final GiftRuleRepository repo;
    private final ProductTypeRepository productTypeRepo;

    @Transactional(readOnly = true)
    public List<Response> findAll() {
        return repo.findAll(Sort.by("minimumAmount")).stream().map(Response::from).toList();
    }

    public Response create(Request r) {
        if (repo.existsByMinimumAmount(r.minimumAmount())) throw new BusinessException(DUPLICATE_GIFT_AMOUNT);
        GiftRule rule = GiftRule.builder().build();
        rule.update(r.minimumAmount(), r.giftQty(), r.freeShipping(), r.active(), typesOf(r));
        return Response.from(repo.save(rule));
    }

    public Response update(Long id, Request r) {
        if (repo.existsByMinimumAmountAndGiftRuleIdNot(r.minimumAmount(), id)) throw new BusinessException(DUPLICATE_GIFT_AMOUNT);
        GiftRule rule = repo.getOrThrow(id);
        rule.update(r.minimumAmount(), r.giftQty(), r.freeShipping(), r.active(), typesOf(r));
        return Response.from(rule);
    }

    // 증정 개수가 있으면 대상 상품종류가 하나 이상 있어야 함
    private Set<ProductType> typesOf(Request r) {
        if (r.giftQty() > 0 && r.productTypeIds().isEmpty()) throw new BusinessException(GIFT_TYPE_REQUIRED);
        return new HashSet<>(r.productTypeIds().stream().map(productTypeRepo::getOrThrow).toList());
    }
}
