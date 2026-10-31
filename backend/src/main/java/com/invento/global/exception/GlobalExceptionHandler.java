package com.invento.global.exception;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 같은 재고를 동시에 수정해서 @Version 충돌이 난 경우 500 대신 409
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail optimisticLock() {
        return new BusinessException(ErrorCode.STOCK_CONFLICT).getBody();
    }
}
