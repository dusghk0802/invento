package com.invento.domain.series.repository;

import com.invento.domain.series.entity.Series;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeriesRepository extends JpaRepository<Series, Long> {

    boolean existsBySeriesName(String seriesName);
}