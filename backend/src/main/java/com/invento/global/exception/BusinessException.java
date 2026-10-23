package com.invento.global.exception;

import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class BusinessException extends ErrorResponseException {

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getStatus(),
              ProblemDetail.forStatusAndDetail(errorCode.getStatus(), errorCode.getMessage()),
              null);
        getBody().setProperty("code", errorCode.name());
    }
}
