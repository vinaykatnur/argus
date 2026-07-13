package com.argus.exception;

import org.springframework.http.HttpStatus;

public class AccountNotVerifiedException extends ApiException {

    public AccountNotVerifiedException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
