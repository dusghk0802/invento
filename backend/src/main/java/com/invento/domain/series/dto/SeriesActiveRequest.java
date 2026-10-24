package com.invento.domain.series.dto;

import jakarta.validation.constraints.NotNull;

public record SeriesActiveRequest(
        @NotNull(message = "사용 여부를 입력해 주세요.")
        Boolean active
) {
}