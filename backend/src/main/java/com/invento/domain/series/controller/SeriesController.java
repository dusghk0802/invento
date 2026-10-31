package com.invento.domain.series.controller;

import com.invento.domain.series.dto.SeriesActiveRequest;
import com.invento.domain.series.dto.SeriesRequest;
import com.invento.domain.series.dto.SeriesResponse;
import com.invento.domain.series.service.SeriesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/series")
@RequiredArgsConstructor
public class SeriesController {

    private final SeriesService seriesService;

    @GetMapping
    public List<SeriesResponse> findAll(@RequestParam(required = false) Boolean active) {
        return seriesService.findAll(active);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SeriesResponse create(@Valid @RequestBody SeriesRequest request) { return seriesService.create(request); }

    @PutMapping("/{seriesId}")
    public SeriesResponse update(@PathVariable Long seriesId, @Valid @RequestBody SeriesRequest request) {
        return seriesService.update(seriesId, request);
    }

    @PatchMapping("/{seriesId}/active")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeActive(@PathVariable Long seriesId, @Valid @RequestBody SeriesActiveRequest request) {
        seriesService.changeActive(seriesId, request.active());
    }
}