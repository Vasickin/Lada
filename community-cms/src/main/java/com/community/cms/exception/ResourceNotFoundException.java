package com.community.cms.exception;

/**
 * Исключение, выбрасываемое когда запрашиваемый ресурс не найден.
 *
 * @author Vasickin
 * @since 1.0
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
