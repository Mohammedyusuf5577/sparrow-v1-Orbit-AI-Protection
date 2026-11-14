package com.orbit.exception;

import com.orbit.util.AppUtils.Status;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AppException extends RuntimeException {

    private final HttpStatus status;

    public AppException(String message) {
        super(message);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public AppException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public AppException(Status status, Object... args) {
        super(status.getMessage(args));
        this.status = status.getHttpStatus();
    }
}
