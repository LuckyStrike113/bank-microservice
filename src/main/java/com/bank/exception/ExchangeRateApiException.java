package com.bank.exception;

import lombok.Getter;

/**
 * Exception thrown when the external exchange rate API fails.
 */
@Getter
public class ExchangeRateApiException extends RuntimeException {

    /**
     * Constructs a new exception with the specified message.
     *
     * @param message the detail message
     */
    public ExchangeRateApiException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public ExchangeRateApiException(String message, Throwable cause) {
        super(message, cause);
    }
}