package com.invento.domain.transfer.controller;

import com.invento.domain.transfer.dto.TransferDto.*;
import com.invento.domain.transfer.entity.TransferStatus;
import com.invento.domain.transfer.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService service;

    @GetMapping
    public List<Summary> findAll(@RequestParam(required = false) TransferStatus status) {
        return service.findAll(status);
    }

    @GetMapping("/{id}")
    public Detail findOne(@PathVariable Long id) { return service.findOne(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Detail request(@Valid @RequestBody Request request) { return service.request(request); }

    @PostMapping("/{id}/approve")
    public Detail approve(@PathVariable Long id, @Valid @RequestBody(required = false) ShipRequest request) {
        return service.approve(id, request);
    }

    @PostMapping("/{id}/reject")
    public Detail reject(@PathVariable Long id, @Valid @RequestBody RejectRequest request) { return service.reject(id, request); }

    @PostMapping("/{id}/ship")
    public Detail ship(@PathVariable Long id, @Valid @RequestBody(required = false) ShipRequest request) {
        return service.ship(id, request);
    }

    @PostMapping("/{id}/receive")
    public Detail receive(@PathVariable Long id) { return service.receive(id); }

    @PostMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id) { service.cancel(id); }
}
