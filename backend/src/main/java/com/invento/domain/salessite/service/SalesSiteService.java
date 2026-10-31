package com.invento.domain.salessite.service;

import com.invento.domain.salessite.dto.SalesSiteDto.Request;
import com.invento.domain.salessite.dto.SalesSiteDto.Response;
import com.invento.domain.salessite.entity.SalesSite;
import com.invento.domain.salessite.repository.SalesSiteRepository;
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
public class SalesSiteService {

    private final SalesSiteRepository repo;

    @Transactional(readOnly = true)
    public List<Response> findAll() {
        return repo.findAll(Sort.by("salesSiteName")).stream().map(Response::from).toList();
    }

    public Response create(Request r) {
        if (repo.existsBySalesSiteName(r.salesSiteName())) throw new BusinessException(DUPLICATE_NAME);
        return Response.from(repo.save(SalesSite.builder().salesSiteName(r.salesSiteName()).active(r.active()).build()));
    }

    public Response update(Long id, Request r) {
        if (repo.existsBySalesSiteNameAndSalesSiteIdNot(r.salesSiteName(), id)) throw new BusinessException(DUPLICATE_NAME);
        SalesSite site = repo.getOrThrow(id);
        site.update(r.salesSiteName(), r.active());
        return Response.from(site);
    }
}
