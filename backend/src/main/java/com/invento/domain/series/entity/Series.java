package com.invento.domain.series.entity;

import com.invento.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity @Table(name = "series", uniqueConstraints = @UniqueConstraint(name = "uk_series_name", columnNames = "series_name"))
@Getter @NoArgsConstructor(access = PROTECTED)
public class Series extends BaseEntity {

    @Id @GeneratedValue(strategy = IDENTITY) private Long seriesId;
    @Column(nullable = false, length = 20) private String seriesName;
    @Column(nullable = false) private boolean active = true;

    public Series(String seriesName) { this.seriesName = seriesName; }

    public void changeName(String seriesName) { this.seriesName = seriesName; }

    public void changeActive(boolean active) { this.active = active; }
}