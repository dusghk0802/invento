package com.invento.domain.packaging.controller;

import com.invento.domain.packaging.dto.PackagingDto.Request;
import com.invento.domain.packaging.dto.PackagingDto.Response;
import com.invento.domain.packaging.service.PackagingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/packaging")
@RequiredArgsConstructor
public class PackagingController {

    private final PackagingService service;

    @GetMapping
    public List<Response> findAll() { return service.findAll(); }

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
