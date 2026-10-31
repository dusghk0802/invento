package com.invento.domain.gift.dto;

import com.invento.domain.gift.entity.GiftRule;
import com.invento.domain.producttype.entity.ProductType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.Comparator;
import java.util.List;

public class GiftRuleDto {

    public record Request(
            @NotNull @PositiveOrZero Long minimumAmount,
            @NotNull @PositiveOrZero Integer giftQty,
            @NotNull Boolean freeShipping,
            @NotNull Boolean active,
            @NotNull List<Long> productTypeIds) {
    }

    public record TypeRef(Long productTypeId, String productTypeName) {
    }

    public record Response(Long giftRuleId, long minimumAmount, int giftQty, boolean freeShipping, boolean active,
                           List<TypeRef> productTypes) {
        public static Response from(GiftRule r) {
            return new Response(r.getGiftRuleId(), r.getMinimumAmount(), r.getGiftQty(), r.isFreeShipping(),
                    r.isActive(), r.getProductTypes().stream()
                    .sorted(Comparator.comparing(ProductType::getProductTypeName))
                    .map(t -> new TypeRef(t.getProductTypeId(), t.getProductTypeName())).toList());
        }
    }
}
