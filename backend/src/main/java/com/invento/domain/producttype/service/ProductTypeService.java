package com.invento.domain.producttype.service;

import com.invento.domain.producttype.dto.ProductTypeDto.Request;
import com.invento.domain.producttype.dto.ProductTypeDto.Response;
import com.invento.domain.producttype.entity.ProductType;
import com.invento.domain.producttype.repository.ProductTypeRepository;
import com.invento.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.invento.global.exception.ErrorCode.DUPLICATE_NAME;
import static com.invento.global.exception.ErrorCode.INVALID_PARENT;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductTypeService {

    private final ProductTypeRepository repo;

    @Transactional(readOnly = true)
    public List<Response> findAll() {
        return repo.findAll(Sort.by("productTypeName")).stream().map(Response::from).toList();
    }

    public Response create(Request r) {
        if (repo.existsByProductTypeName(r.productTypeName())) throw new BusinessException(DUPLICATE_NAME);
        return Response.from(repo.save(ProductType.builder()
                .parent(parentOf(r)).productTypeName(r.productTypeName()).active(r.active()).build()));
    }

    public Response update(Long id, Request r) {
        if (id.equals(r.parentId())) throw new BusinessException(INVALID_PARENT);
        if (repo.existsByProductTypeNameAndProductTypeIdNot(r.productTypeName(), id)) throw new BusinessException(DUPLICATE_NAME);
        ProductType type = repo.getOrThrow(id);
        type.update(parentOf(r), r.productTypeName(), r.active());
        return Response.from(type);
    }

    private ProductType parentOf(Request r) {
        return r.parentId() == null ? null : repo.getOrThrow(r.parentId());
    }
}
