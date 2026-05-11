package com.alexey.tabgenerator.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class MlServerException extends RuntimeException {

    private final HttpStatus status;

    public MlServerException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}