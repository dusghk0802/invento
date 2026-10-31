package com.invento.domain.location.dto;

import com.invento.domain.location.entity.Location;
import com.invento.domain.location.entity.LocationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class LocationDto {

    public record Request(
            @NotBlank @Size(max = 50) String locationName,
            @NotNull LocationType locationType,
            @NotNull Boolean active) {
    }

    public record Response(Long locationId, String locationName, LocationType locationType, boolean active) {
        public static Response from(Location l) {
            return new Response(l.getLocationId(), l.getLocationName(), l.getLocationType(), l.isActive());
        }
    }
}
