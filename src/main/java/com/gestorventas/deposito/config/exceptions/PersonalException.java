package com.gestorventas.deposito.config.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PersonalException extends RuntimeException {

    private final ApiErrorCode errorCode;
    private final String message;

    public PersonalException(ApiErrorCode errorCode, String message) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.message = message;
    }

    public int getStatusCode() {
        return errorCode.getHttpStatus().value();
    }

    public HttpStatus getHttpStatus() {
        return errorCode.getHttpStatus();
    }

}
