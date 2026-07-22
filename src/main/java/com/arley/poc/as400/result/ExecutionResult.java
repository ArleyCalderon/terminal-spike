package com.arley.poc.as400.result;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Resultado estructurado de la ejecución completa.
 *
 * Este objeto es devuelto posteriormente por la API REST.
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
    Map<String, Map<String, String>> extractedData,
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
        extractedData = copyExtractedData(extractedData);
    }

    /**
     * Conserva compatibilidad con código o pruebas anteriores
     * que todavía construyan el resultado sin extractedData.
     */
    public ExecutionResult(
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
        this(
            processId,
            processName,
            processVersion,
            status,
            startedAt,
            finishedAt,
            durationMs,
            steps,
            Map.of(),
            failedStepId,
            errorMessage
        );
    }

    /**
     * Crea una copia profunda e inmutable de los datos extraídos.
     */
    private static Map<String, Map<String, String>> copyExtractedData(
        Map<String, Map<String, String>> source
    ) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }

        Map<String, Map<String, String>> copy =
            new LinkedHashMap<>();

        for (
            Map.Entry<String, Map<String, String>> entry
                : source.entrySet()
        ) {
            String target = entry.getKey();
            Map<String, String> values = entry.getValue();

            if (target == null || target.isBlank()) {
                throw new IllegalArgumentException(
                    "El target de los datos extraídos no puede estar vacío."
                );
            }

            if (values == null) {
                throw new IllegalArgumentException(
                    "Los datos extraídos de "
                        + target
                        + " no pueden ser nulos."
                );
            }

            copy.put(
                target,
                Collections.unmodifiableMap(
                    new LinkedHashMap<>(values)
                )
            );
        }

        return Collections.unmodifiableMap(copy);
    }
}