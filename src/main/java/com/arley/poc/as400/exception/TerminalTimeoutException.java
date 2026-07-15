package com.arley.poc.as400.exception;

/**
 * Indica que una condición esperada de la terminal 5250
 * no ocurrió dentro del tiempo establecido.
 */
public class TerminalTimeoutException extends RuntimeException {

    public TerminalTimeoutException(String message) {
        super(message);
    }

    public TerminalTimeoutException(
        String message,
        Throwable cause
    ) {
        super(message, cause);
    }
}