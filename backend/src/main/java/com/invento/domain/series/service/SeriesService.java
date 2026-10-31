package com.invento.domain.series.service;

import com.invento.domain.series.dto.SeriesRequest;
import com.invento.domain.series.dto.SeriesResponse;
import com.invento.domain.series.entity.Series;
import com.invento.domain.series.repository.SeriesRepository;
import com.invento.global.exception.BusinessException;
import com.invento.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeriesService {

    private final SeriesRepository seriesRepository;

    public List<SeriesResponse> findAll(Boolean active) {
        List<Series> series = (active == null)
                ? seriesRepository.findAll(Sort.by("seriesName"))
                : seriesRepository.findByActiveOrderBySeriesNameAsc(active);
        return series.stream().map(SeriesResponse::from).toList();
    }

    @Transactional
    public SeriesResponse create(SeriesRequest request) {
        String name = request.seriesName().trim();
        validateDuplicateName(name);
        return SeriesResponse.from(seriesRepository.save(new Series(name)));
    }

    @Transactional
    public SeriesResponse update(Long seriesId, SeriesRequest request) {
        Series series = getSeries(seriesId);
        String name = request.seriesName().trim();
        if (!series.getSeriesName().equals(name)) {
            validateDuplicateName(name);
        }
        series.changeName(name);
        return SeriesResponse.from(series);
    }

    @Transactional
    public void changeActive(Long seriesId, boolean active) { getSeries(seriesId).changeActive(active); }

    private Series getSeries(Long seriesId) {
        return seriesRepository.findById(seriesId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SERIES_NOT_FOUND));
    }

    private void validateDuplicateName(String name) {
        if (seriesRepository.existsBySeriesName(name)) {
            throw new BusinessException(ErrorCode.DUPLICATE_SERIES_NAME);
        }
    }
}