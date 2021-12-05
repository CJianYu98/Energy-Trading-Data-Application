package com.smu.energydatatradingapp.exception;

/**
 * This CrawlerErrorException extends RuntimeException and is to handle
 * exception when a crawler encounters an error while crawling.
 */
public class CrawlerErrorException extends RuntimeException {
    public CrawlerErrorException(String message) {
        super(message);
    }
}
