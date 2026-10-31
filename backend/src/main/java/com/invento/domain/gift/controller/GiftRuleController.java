package com.invento.domain.gift.controller;

import com.invento.domain.gift.dto.GiftRuleDto.Request;
import com.invento.domain.gift.dto.GiftRuleDto.Response;
import com.invento.domain.gift.service.GiftRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 조회는 전 직원(주문 처리 시 필요), 등록·수정은 사장만 (SecurityConfig OWNER_WRITE)
@RestController
@RequestMapping("/api/admin/gift-rules")
@RequiredArgsConstructor
public class GiftRuleController {

    private final GiftRuleService service;

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
}
