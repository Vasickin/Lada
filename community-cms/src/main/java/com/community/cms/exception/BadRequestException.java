package com.community.cms.exception;

/**
 * Исключение, выбрасываемое при некорректных запросах (HTTP 400).
 *
 * @author Vasickin
 * @since 1.0
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
