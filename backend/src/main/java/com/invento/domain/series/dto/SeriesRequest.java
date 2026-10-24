package com.invento.domain.series.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SeriesRequest(
        @NotBlank(message = "시리즈 이름을 입력해 주세요.")
        @Size(max = 20, message = "시리즈 이름은 20자 이하로 입력해 주세요.")
        String seriesName
) {
}