package com.invento.domain.order.controller;

import com.invento.domain.order.dto.StoreSaleDto.*;
import com.invento.domain.order.service.StoreSaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/store-sales")
@RequiredArgsConstructor
public class StoreSaleController {

    private final StoreSaleService service;

    // 예: GET /api/admin/store-sales?from=2026-10-01&to=2026-10-31&locationId=2
    @GetMapping
    public List<Summary> findAll(@ModelAttribute Search search) { return service.findAll(search); }

    @GetMapping("/{id}")
    public Detail findOne(@PathVariable Long id) { return service.findOne(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Detail create(@Valid @RequestBody Request request) { return service.create(request); }
}
