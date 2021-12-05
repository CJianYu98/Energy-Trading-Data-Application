package com.smu.energydatatradingapp.exception;

import lombok.Getter;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * This ExceptionResponse class provides the attributes and constructor to
 * instantiate an exception response body
 */
@Getter
public class ExceptionResponse {
    private final Integer status;
    private final String message;
    private final ZonedDateTime timestamp;
    private final String path;

    public ExceptionResponse(Integer status, String message, String path) {
        this.status = status;
        this.message = message;
        this.timestamp = ZonedDateTime.now(ZoneId.of("Asia/Singapore"));
        this.path = path;
    }
}
