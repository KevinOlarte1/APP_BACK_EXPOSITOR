package com.gestorventas.deposito.config.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PersonalException extends RuntimeException {

    private final ApiErrorCode errorCode;

    public PersonalException(ApiErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public int getStatusCode() {
        return errorCode.getHttpStatus().value();
    }

    public HttpStatus getHttpStatus() {
        return errorCode.getHttpStatus();
    }

}
