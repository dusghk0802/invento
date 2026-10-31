package com.invento.domain.production.controller;

import com.invento.domain.production.dto.ProductionDto.*;
import com.invento.domain.production.service.ProductionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/admin/production-orders")
@RequiredArgsConstructor
public class ProductionController {

    private final ProductionService service;

    // 예: GET /api/admin/production-orders?status=PARTIAL&supplierId=1
    @GetMapping
    public List<Summary> findAll(@ModelAttribute Search search) { return service.findAll(search); }

    @GetMapping("/{id}")
    public Detail findOne(@PathVariable Long id) { return service.findOne(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Detail create(@Valid @RequestBody Request request) { return service.create(request); }

    @PutMapping("/{id}")
    public Detail update(@PathVariable Long id, @Valid @RequestBody Request request) {
        return service.update(id, request);
    }

    @PostMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id) { service.cancel(id); }

    // 발주서 엑셀 다운로드 (공장에 메일·카톡으로 보냄)
    @GetMapping("/{id}/excel")
    public ResponseEntity<byte[]> excel(@PathVariable Long id) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("발주서_%03d.xlsx".formatted(id), StandardCharsets.UTF_8).build().toString())
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(service.excel(id));
    }

    // 공장 확인: 공장이 회신한 확정 납기일 기록
    @PostMapping("/{id}/confirm")
    public Detail confirm(@PathVariable Long id, @Valid @RequestBody ConfirmRequest request) { return service.confirm(id, request); }

    @PostMapping("/{id}/receipts")
    public Detail receive(@PathVariable Long id, @Valid @RequestBody ReceiveRequest request) {
        return service.receive(id, request);
    }

    @PutMapping("/{id}/receipts/{receiptId}")
    public Detail correctReceipt(@PathVariable Long id, @PathVariable Long receiptId,
                                 @Valid @RequestBody CorrectRequest request) {
        return service.correctReceipt(id, receiptId, request);
    }
}
