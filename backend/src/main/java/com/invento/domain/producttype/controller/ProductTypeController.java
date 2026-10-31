package com.invento.domain.producttype.controller;

import com.invento.domain.producttype.dto.ProductTypeDto.Request;
import com.invento.domain.producttype.dto.ProductTypeDto.Response;
import com.invento.domain.producttype.service.ProductTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/product-types")
@RequiredArgsConstructor
public class ProductTypeController {

    private final ProductTypeService service;

    @GetMapping
    public List<Response> findAll() { return service.findAll(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Response create(@Valid @RequestBody Request request) { return service.create(request); }

    @PutMapping("/{id}")
    public Response update(@PathVariable Long id, @Valid @RequestBody Request request) {
        return service.update(id, request);
    }
}
