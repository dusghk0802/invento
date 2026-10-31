package com.invento.domain.supplier.dto;

import com.invento.domain.supplier.entity.Supplier;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SupplierDto {

    public record Request(
            @NotBlank @Size(max = 100) String supplierName,
            @Size(max = 100) String contact,
            @NotNull Boolean active) {
    }

    public record Response(Long supplierId, String supplierName, String contact, boolean active) {
        public static Response from(Supplier s) {
            return new Response(s.getSupplierId(), s.getSupplierName(), s.getContact(), s.isActive());
        }
    }
}
