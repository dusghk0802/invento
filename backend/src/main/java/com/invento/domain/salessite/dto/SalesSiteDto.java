package com.invento.domain.salessite.dto;

import com.invento.domain.salessite.entity.SalesSite;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SalesSiteDto {

    public record Request(@NotBlank @Size(max = 100) String salesSiteName, @NotNull Boolean active) {
    }

    public record Response(Long salesSiteId, String salesSiteName, boolean active) {
        public static Response from(SalesSite s) {
            return new Response(s.getSalesSiteId(), s.getSalesSiteName(), s.isActive());
        }
    }
}
