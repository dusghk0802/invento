package com.invento.domain.product.controller;

import com.invento.domain.product.dto.ProductDto.Request;
import com.invento.domain.product.dto.ProductDto.Response;
import com.invento.domain.product.dto.ProductDto.Search;
import com.invento.domain.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    // 예: GET /api/admin/products?keyword=스티커&seriesId=1
    @GetMapping
    public List<Response> findAll(@ModelAttribute Search search) { return service.findAll(search); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Response create(@Valid @RequestBody Request request) { return service.create(request); }

    @PutMapping("/{id}")
    public Response update(@PathVariable Long id, @Valid @RequestBody Request request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
