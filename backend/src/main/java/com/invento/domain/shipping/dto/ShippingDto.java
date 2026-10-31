package com.invento.domain.shipping.dto;

import com.invento.domain.shipping.entity.ShippingFeeRule;
import com.invento.domain.shipping.entity.ShippingMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class ShippingDto {

    public record Request(
            @NotBlank @Size(max = 100) String shippingMethodName,
            @NotNull @PositiveOrZero Long defaultFee,
            @NotNull Boolean active) {
    }

    public record Response(Long shippingMethodId, String shippingMethodName, long defaultFee, boolean active) {
        public static Response from(ShippingMethod m) {
            return new Response(m.getShippingMethodId(), m.getShippingMethodName(), m.getDefaultFee(), m.isActive());
        }
    }

    public record FeeRuleRequest(@NotNull Long productTypeId, @PositiveOrZero Long fee, @NotNull Boolean allowed) {
    }

    public record FeeRuleResponse(Long productTypeId, String productTypeName, Long fee, boolean allowed) {
        public static FeeRuleResponse from(ShippingFeeRule r) {
            return new FeeRuleResponse(r.getProductType().getProductTypeId(), r.getProductType().getProductTypeName(),
                    r.getFee(), r.isAllowed());
        }
    }
}
