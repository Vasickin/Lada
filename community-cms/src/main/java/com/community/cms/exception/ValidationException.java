package com.community.cms.exception;

/**
 * Исключение, выбрасываемое при ошибках валидации данных.
 *
 * @author Vasickin
 * @since 1.0
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
