package com.invento.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    NOT_FOUND(HttpStatus.NOT_FOUND, "대상을 찾을 수 없습니다."),
    DUPLICATE_NAME(HttpStatus.CONFLICT, "이미 사용 중인 이름입니다."),
    INVALID_PARENT(HttpStatus.BAD_REQUEST, "상위 분류를 자기 자신으로 지정할 수 없습니다."),
    FORBIDDEN_LOCATION(HttpStatus.FORBIDDEN, "담당 위치의 재고만 처리할 수 있습니다."),

    // 시리즈
    SERIES_NOT_FOUND(HttpStatus.NOT_FOUND, "시리즈를 찾을 수 없습니다."),
    DUPLICATE_SERIES_NAME(HttpStatus.CONFLICT, "이미 존재하는 시리즈 이름입니다."),

    // 상품·재고
    DUPLICATE_PRODUCT_CODE(HttpStatus.CONFLICT, "이미 사용 중인 상품번호입니다."),
    STOCK_NOT_ENOUGH(HttpStatus.CONFLICT, "재고가 부족합니다."),
    STOCK_CONFLICT(HttpStatus.CONFLICT, "다른 작업과 동시에 재고가 변경되었습니다. 다시 시도해 주세요."),
    INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "수량은 0이 될 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}
