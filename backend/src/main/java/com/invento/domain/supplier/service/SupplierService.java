package com.invento.domain.supplier.service;

import com.invento.domain.supplier.dto.SupplierDto.Request;
import com.invento.domain.supplier.dto.SupplierDto.Response;
import com.invento.domain.supplier.entity.Supplier;
import com.invento.domain.supplier.repository.SupplierRepository;
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
public class SupplierService {

    private final SupplierRepository repo;

    @Transactional(readOnly = true)
    public List<Response> findAll() {
        return repo.findAll(Sort.by("supplierName")).stream().map(Response::from).toList();
    }

    public Response create(Request r) {
        if (repo.existsBySupplierName(r.supplierName())) throw new BusinessException(DUPLICATE_NAME);
        return Response.from(repo.save(Supplier.builder()
                .supplierName(r.supplierName()).contact(r.contact()).active(r.active()).build()));
    }

    public Response update(Long id, Request r) {
        if (repo.existsBySupplierNameAndSupplierIdNot(r.supplierName(), id)) throw new BusinessException(DUPLICATE_NAME);
        Supplier supplier = repo.getOrThrow(id);
        supplier.update(r.supplierName(), r.contact(), r.active());
        return Response.from(supplier);
    }
}
