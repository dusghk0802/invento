package com.invento.domain.shipping.service;

import com.invento.domain.producttype.repository.ProductTypeRepository;
import com.invento.domain.shipping.dto.ShippingDto.FeeRuleRequest;
import com.invento.domain.shipping.dto.ShippingDto.FeeRuleResponse;
import com.invento.domain.shipping.dto.ShippingDto.Request;
import com.invento.domain.shipping.dto.ShippingDto.Response;
import com.invento.domain.shipping.entity.ShippingFeeRule;
import com.invento.domain.shipping.entity.ShippingMethod;
import com.invento.domain.shipping.repository.ShippingFeeRuleRepository;
import com.invento.domain.shipping.repository.ShippingMethodRepository;
import com.invento.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.invento.global.exception.ErrorCode.DUPLICATE_NAME;

@Service
@RequiredArgsConstructor
@Transactional
public class ShippingService {

    private final ShippingMethodRepository methodRepo;
    private final ShippingFeeRuleRepository ruleRepo;
    private final ProductTypeRepository typeRepo;

    @Transactional(readOnly = true)
    public List<Response> findAll() {
        return methodRepo.findAll(Sort.by("shippingMethodId")).stream().map(Response::from).toList();
    }

    public Response create(Request r) {
        if (methodRepo.existsByShippingMethodName(r.shippingMethodName())) throw new BusinessException(DUPLICATE_NAME);
        return Response.from(methodRepo.save(ShippingMethod.builder()
                .shippingMethodName(r.shippingMethodName()).defaultFee(r.defaultFee()).active(r.active()).build()));
    }

    public Response update(Long id, Request r) {
        if (methodRepo.existsByShippingMethodNameAndShippingMethodIdNot(r.shippingMethodName(), id)) {
            throw new BusinessException(DUPLICATE_NAME);
        }
        ShippingMethod method = methodRepo.getOrThrow(id);
        method.update(r.shippingMethodName(), r.defaultFee(), r.active());
        return Response.from(method);
    }

    @Transactional(readOnly = true)
    public List<FeeRuleResponse> findFeeRules(Long methodId) {
        return ruleRepo.findByShippingMethodShippingMethodId(methodId).stream().map(FeeRuleResponse::from).toList();
    }

    // 규칙 목록을 통째로 교체 (화면에서 표 전체를 저장)
    public List<FeeRuleResponse> replaceFeeRules(Long methodId, List<FeeRuleRequest> rules) {
        ShippingMethod method = methodRepo.getOrThrow(methodId);
        ruleRepo.deleteByShippingMethodShippingMethodId(methodId);
        ruleRepo.flush(); // 삭제를 먼저 반영해야 같은 (배송방법, 상품종류) 유니크 제약에 걸리지 않음
        return ruleRepo.saveAll(rules.stream().map(r -> ShippingFeeRule.builder()
                        .shippingMethod(method)
                        .productType(typeRepo.getOrThrow(r.productTypeId()))
                        .fee(r.fee())
                        .allowed(r.allowed())
                        .build()).toList())
                .stream().map(FeeRuleResponse::from).toList();
    }
}
