package com.invento.domain.salessite.controller;

import com.invento.domain.salessite.dto.SalesSiteDto.Request;
import com.invento.domain.salessite.dto.SalesSiteDto.Response;
import com.invento.domain.salessite.service.SalesSiteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/sales-sites")
@RequiredArgsConstructor
public class SalesSiteController {

    private final SalesSiteService service;

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
