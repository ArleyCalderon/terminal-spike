package com.arley.poc.as400.result;

import java.time.Instant;
import java.util.Objects;

import com.arley.poc.as400.model.StepAction;

/**
 * Resultado estructurado de un paso individual.
 */
public record StepResult(
    String stepId,
    StepAction action,
    String description,
    StepStatus status,
    Instant startedAt,
    Instant finishedAt,
    long durationMs,
    String errorMessage
) {

    public StepResult {
        if (stepId == null || stepId.isBlank()) {
            throw new IllegalArgumentException(
                "El identificador del paso no puede estar vacío."
            );
        }

        Objects.requireNonNull(
            action,
            "La acción del paso no puede ser nula."
        );

        Objects.requireNonNull(
            status,
            "El estado del paso no puede ser nulo."
        );

        Objects.requireNonNull(
            startedAt,
            "La fecha inicial del paso no puede ser nula."
        );

        Objects.requireNonNull(
            finishedAt,
            "La fecha final del paso no puede ser nula."
        );

        if (durationMs < 0) {
            throw new IllegalArgumentException(
                "La duración del paso no puede ser negativa."
            );
        }
    }
}