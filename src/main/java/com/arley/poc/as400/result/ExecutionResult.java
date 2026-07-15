package com.arley.poc.as400.result;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Resultado estructurado de la ejecución completa.
 *
 * Este objeto será posteriormente devuelto por la API REST.
 */
public record ExecutionResult(
    String processId,
    String processName,
    String processVersion,
    ExecutionStatus status,
    Instant startedAt,
    Instant finishedAt,
    long durationMs,
    List<StepResult> steps,
    String failedStepId,
    String errorMessage
) {

    public ExecutionResult {
        if (processId == null || processId.isBlank()) {
            throw new IllegalArgumentException(
                "El identificador del proceso no puede estar vacío."
            );
        }

        if (processName == null || processName.isBlank()) {
            throw new IllegalArgumentException(
                "El nombre del proceso no puede estar vacío."
            );
        }

        Objects.requireNonNull(
            status,
            "El estado de ejecución no puede ser nulo."
        );

        Objects.requireNonNull(
            startedAt,
            "La fecha inicial no puede ser nula."
        );

        Objects.requireNonNull(
            finishedAt,
            "La fecha final no puede ser nula."
        );

        if (durationMs < 0) {
            throw new IllegalArgumentException(
                "La duración de la ejecución no puede ser negativa."
            );
        }

        if (steps == null) {
            throw new IllegalArgumentException(
                "La lista de resultados no puede ser nula."
            );
        }

        steps = List.copyOf(steps);
    }
}