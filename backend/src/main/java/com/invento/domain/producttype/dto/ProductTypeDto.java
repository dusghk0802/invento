package com.invento.domain.producttype.dto;

import com.invento.domain.producttype.entity.ProductType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProductTypeDto {

    public record Request(
            Long parentId, // 없으면 최상위
            @NotBlank @Size(max = 100) String productTypeName,
            @NotNull Boolean active) {
    }

    public record Response(Long productTypeId, Long parentId, String parentName, String productTypeName, boolean active) {
        public static Response from(ProductType t) {
            ProductType p = t.getParent();
            return new Response(t.getProductTypeId(), p == null ? null : p.getProductTypeId(),
                    p == null ? null : p.getProductTypeName(), t.getProductTypeName(), t.isActive());
        }
    }
}
