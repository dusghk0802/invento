package com.invento.domain.location.service;

import com.invento.domain.location.dto.LocationDto.Request;
import com.invento.domain.location.dto.LocationDto.Response;
import com.invento.domain.location.entity.Location;
import com.invento.domain.location.repository.LocationRepository;
import com.invento.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.invento.global.exception.ErrorCode.DUPLICATE_NAME;

@Service
@RequiredArgsConstructor
@Transactional
public class LocationService {

    private final LocationRepository repo;

    @Transactional(readOnly = true)
    public List<Response> findAll() {
        return repo.findAll(Sort.by("locationId")).stream().map(Response::from).toList();
    }

    public Response create(Request r) {
        if (repo.existsByLocationName(r.locationName())) throw new BusinessException(DUPLICATE_NAME);
        return Response.from(repo.save(Location.builder()
                .locationName(r.locationName()).locationType(r.locationType()).active(r.active()).build()));
    }

    public Response update(Long id, Request r) {
        if (repo.existsByLocationNameAndLocationIdNot(r.locationName(), id)) throw new BusinessException(DUPLICATE_NAME);
        Location location = repo.getOrThrow(id);
        location.update(r.locationName(), r.locationType(), r.active());
        return Response.from(location);
    }
}
