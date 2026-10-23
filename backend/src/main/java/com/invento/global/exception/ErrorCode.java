package com.invento.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 시리즈
    SERIES_NOT_FOUND(HttpStatus.NOT_FOUND, "시리즈를 찾을 수 없습니다."),
    DUPLICATE_SERIES_NAME(HttpStatus.CONFLICT, "이미 존재하는 시리즈 이름입니다.");

    private final HttpStatus status;
    private final String message;
}