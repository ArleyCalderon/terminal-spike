package com.arley.poc.as400.api;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.arley.poc.as400.exception.TerminalTimeoutException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Convierte errores no estructurados en respuestas HTTP
 * fáciles de interpretar por clientes como n8n.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(
            ApiExceptionHandler.class
        );

    @ExceptionHandler(
        HttpMessageNotReadableException.class
    )
    public ResponseEntity<ApiError> handleInvalidJson(
        HttpMessageNotReadableException exception,
        HttpServletRequest request
    ) {
        return buildResponse(
            HttpStatus.BAD_REQUEST,
            "El JSON enviado no es válido o contiene propiedades incompatibles.",
            request.getRequestURI()
        );
    }

    @ExceptionHandler(
        TerminalTimeoutException.class
    )
    public ResponseEntity<ApiError> handleTerminalTimeout(
        TerminalTimeoutException exception,
        HttpServletRequest request
    ) {
        LOGGER.warn(
            "Timeout conectando con la terminal AS400.",
            exception
        );

        return buildResponse(
            HttpStatus.GATEWAY_TIMEOUT,
            exception.getMessage(),
            request.getRequestURI()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedError(
        Exception exception,
        HttpServletRequest request
    ) {
        LOGGER.error(
            "Error no controlado ejecutando el proceso.",
            exception
        );

        return buildResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Ocurrió un error interno al ejecutar el proceso.",
            request.getRequestURI()
        );
    }

    private ResponseEntity<ApiError> buildResponse(
        HttpStatus status,
        String message,
        String path
    ) {
        ApiError error = new ApiError(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            message,
            path
        );

        return ResponseEntity
            .status(status)
            .body(error);
    }

    public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
    ) {
    }
}