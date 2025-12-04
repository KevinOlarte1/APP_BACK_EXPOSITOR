package com.gestorventas.deposito.config.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ApiErrorCode{
    TOKEN_EXPIRED(1000, "Token expirado", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID(1001, "Token Invalido", HttpStatus.UNAUTHORIZED);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ApiErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
