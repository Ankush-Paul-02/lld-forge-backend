package com.devmare.lldforge.data.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Setter
@Getter
public class AppInfoException extends RuntimeException {
    private final HttpStatus status;

    public AppInfoException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
