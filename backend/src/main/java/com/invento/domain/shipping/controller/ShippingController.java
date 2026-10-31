package com.invento.domain.shipping.controller;

import com.invento.domain.shipping.dto.ShippingDto.FeeRuleRequest;
import com.invento.domain.shipping.dto.ShippingDto.FeeRuleResponse;
import com.invento.domain.shipping.dto.ShippingDto.Request;
import com.invento.domain.shipping.dto.ShippingDto.Response;
import com.invento.domain.shipping.service.ShippingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/shipping-methods")
@RequiredArgsConstructor
public class ShippingController {

    private final ShippingService service;

    @GetMapping
    public List<Response> findAll() {
        return service.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Response create(@Valid @RequestBody Request request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public Response update(@PathVariable Long id, @Valid @RequestBody Request request) {
        return service.update(id, request);
    }

    @GetMapping("/{id}/fee-rules")
    public List<FeeRuleResponse> findFeeRules(@PathVariable Long id) {
        return service.findFeeRules(id);
    }

    @PutMapping("/{id}/fee-rules")
    public List<FeeRuleResponse> replaceFeeRules(@PathVariable Long id, @RequestBody List<@Valid FeeRuleRequest> rules) {
        return service.replaceFeeRules(id, rules);
    }
}
