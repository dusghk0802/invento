package com.invento.domain.product.repository;

import com.invento.domain.product.dto.ProductDto.Search;
import com.invento.domain.product.entity.Product;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.invento.domain.product.entity.QProduct.product;
import static com.invento.domain.producttype.entity.QProductType.productType;
import static com.invento.domain.series.entity.QSeries.series;

// 상품 검색: 조건이 비어 있으면 그 조건은 무시 (QueryDSL 동적 쿼리)
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory query;

    @Override
    public List<Product> search(Search s) {
        BooleanBuilder where = new BooleanBuilder(product.deletedAt.isNull());
        if (s.keyword() != null && !s.keyword().isBlank()) {
            String k = s.keyword().trim();
            where.and(product.productName.contains(k).or(product.productCode.contains(k)));
        }
        if (s.seriesId() != null) where.and(product.series.seriesId.eq(s.seriesId()));
        if (s.productTypeId() != null) where.and(product.productType.productTypeId.eq(s.productTypeId()));
        if (s.itemKind() != null) where.and(product.itemKind.eq(s.itemKind()));

        return query.selectFrom(product)
                .leftJoin(product.series, series).fetchJoin()
                .join(product.productType, productType).fetchJoin()
                .where(where)
                .orderBy(product.productId.desc())
                .fetch();
    }
}
