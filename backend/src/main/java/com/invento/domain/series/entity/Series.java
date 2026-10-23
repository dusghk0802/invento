package com.invento.domain.series.entity;

import com.invento.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "series",
        uniqueConstraints = @UniqueConstraint(name = "uk_series_name", columnNames = "series_name")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Series extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seriesId;

    @Column(nullable = false, length = 100)
    private String seriesName;

    @Column(nullable = false)
    private boolean active = true;

    public Series(String seriesName) {
        this.seriesName = seriesName;
    }

    public void changeName(String seriesName) {
        this.seriesName = seriesName;
    }

    public void changeActive(boolean active) {
        this.active = active;
    }
}