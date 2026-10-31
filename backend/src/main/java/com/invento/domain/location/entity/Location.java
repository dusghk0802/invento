package com.invento.domain.location.entity;

import com.invento.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

// 재고가 있는 장소 (창고, 매장)
@Getter
@Entity
@Table(name = "locations")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Location extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long locationId;

    @Column(nullable = false, unique = true, length = 50)
    private String locationName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LocationType locationType;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
