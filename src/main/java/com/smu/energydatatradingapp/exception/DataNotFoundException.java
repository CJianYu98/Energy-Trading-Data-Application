package com.smu.energydatatradingapp.exception;

/**
 * This DataNotFoundException extends RuntimeException and is to handle
 * exception when no data is found when querying the database.
 */
public class DataNotFoundException extends RuntimeException {
    public DataNotFoundException(String message) {
        super(message);
    }
}
