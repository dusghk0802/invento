package com.invento.domain.location.entity;

import com.invento.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;
import static lombok.AccessLevel.PRIVATE;

// 재고가 있는 장소 (창고, 매장)
@Entity @Table(name = "locations")
@Getter @Builder @NoArgsConstructor(access = PROTECTED) @AllArgsConstructor(access = PRIVATE)
public class Location extends BaseEntity {

    @Id @GeneratedValue(strategy = IDENTITY) private Long locationId;
    @Column(nullable = false, unique = true, length = 50) private String locationName;
    @Enumerated(STRING) @Column(nullable = false) private LocationType locationType;
    @Builder.Default @Column(nullable = false) private boolean active = true;

    public void update(String locationName, LocationType locationType, boolean active) {
        this.locationName = locationName;
        this.locationType = locationType;
        this.active = active;
    }
}
