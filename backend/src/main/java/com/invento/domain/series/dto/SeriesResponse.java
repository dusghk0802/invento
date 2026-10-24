package com.invento.domain.series.dto;

import com.invento.domain.series.entity.Series;

public record SeriesResponse(Long seriesId, String seriesName, boolean active) {

    public static SeriesResponse from(Series s) {
        return new SeriesResponse(s.getSeriesId(), s.getSeriesName(), s.isActive());
    }
}