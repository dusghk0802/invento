package com.invento.domain.product.service;

import com.invento.domain.product.dto.ProductDto.Request;
import com.invento.domain.product.dto.ProductDto.Response;
import com.invento.domain.product.dto.ProductDto.Search;
import com.invento.domain.product.entity.Product;
import com.invento.domain.product.repository.ProductRepository;
import com.invento.domain.producttype.repository.ProductTypeRepository;
import com.invento.domain.series.entity.Series;
import com.invento.domain.series.repository.SeriesRepository;
import com.invento.domain.stock.entity.ProductStock;
import com.invento.domain.stock.repository.ProductStockRepository;
import com.invento.domain.stock.service.StockService;
import com.invento.global.common.AuthUtils;
import com.invento.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.invento.global.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository repo;
    private final ProductStockRepository stockRepo;
    private final SeriesRepository seriesRepo;
    private final ProductTypeRepository productTypeRepo;
    private final StockService stockService;

    @Transactional(readOnly = true)
    public List<Response> findAll(Search search) {
        List<Product> products = repo.search(search);
        Map<Long, List<ProductStock>> stocks = stockService.groupVisible(
                stockRepo.findByProductProductIdIn(products.stream().map(Product::getProductId).toList()),
                s -> s.getProduct().getProductId());
        boolean owner = AuthUtils.isOwner();
        return products.stream().map(p -> Response.of(p, stocks.getOrDefault(p.getProductId(), List.of()), owner)).toList();
    }

    public Response create(Request r) {
        if (repo.existsByProductCode(r.productCode())) throw new BusinessException(DUPLICATE_PRODUCT_CODE);
        return Response.of(repo.save(fill(Product.builder().build(), r)), List.of(), true);
    }

    public Response update(Long id, Request r) {
        if (repo.existsByProductCodeAndProductIdNot(r.productCode(), id)) throw new BusinessException(DUPLICATE_PRODUCT_CODE);
        return Response.of(fill(getActive(id), r), stockRepo.findByProductProductIdIn(List.of(id)), true);
    }

    // 주문·원장에서 참조하므로 실제 삭제하지 않고 deleted_at만 기록
    public void delete(Long id) { getActive(id).delete(); }

    private Product getActive(Long id) {
        Product product = repo.getOrThrow(id);
        if (product.getDeletedAt() != null) throw new BusinessException(NOT_FOUND);
        return product;
    }

    // 등록·수정 공통: 요청 값을 엔티티에 채움
    private Product fill(Product p, Request r) {
        Series series = r.seriesId() == null ? null
                : seriesRepo.findById(r.seriesId()).orElseThrow(() -> new BusinessException(SERIES_NOT_FOUND));
        p.update(r.itemKind(), r.productCode(), series, productTypeRepo.getOrThrow(r.productTypeId()), r.productName(),
                r.imageUrl(), r.description(), r.shopVisible(), r.costPrice(), r.sellingPrice(), r.releaseDate(), r.boxQty());
        return p;
    }
}
