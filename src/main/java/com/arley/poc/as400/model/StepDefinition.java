package com.arley.poc.as400.model;

import java.util.List;

/**
 * Representa un paso individual de un proceso definido en JSON.
 *
 * No todas las propiedades se utilizan en todas las acciones.
 * La acción determina cuáles son necesarias.
 */
public record StepDefinition(
    String id,
    StepAction action,
    String description,
    String text,
    Integer fieldIndex,
    String value,
    String key,
    Integer timeoutSeconds,
    Integer minimumFields,
    boolean continueOnError,
    String target,
    List<ExtractionFieldDefinition> fields
) {

    public StepDefinition {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                "El identificador del paso no puede estar vacío."
            );
        }

        if (action == null) {
            throw new IllegalArgumentException(
                "La acción del paso no puede ser nula."
            );
        }

        if (fieldIndex != null && fieldIndex < 0) {
            throw new IllegalArgumentException(
                "El índice del campo no puede ser negativo."
            );
        }

        if (
            timeoutSeconds != null
                && timeoutSeconds <= 0
        ) {
            throw new IllegalArgumentException(
                "El timeout debe ser mayor que cero."
            );
        }

        if (
            minimumFields != null
                && minimumFields < 0
        ) {
            throw new IllegalArgumentException(
                "La cantidad mínima de campos no puede ser negativa."
            );
        }

        /*
         * Jackson enviará null cuando un paso normal no incluya
         * la propiedad fields en el JSON.
         */
        fields = fields == null
            ? List.of()
            : List.copyOf(fields);
    }

    /**
     * Conserva compatibilidad con código y pruebas anteriores
     * que construyan pasos sin configuración de extracción.
     */
    public StepDefinition(
        String id,
        StepAction action,
        String description,
        String text,
        Integer fieldIndex,
        String value,
        String key,
        Integer timeoutSeconds,
        Integer minimumFields,
        boolean continueOnError
    ) {
        this(
            id,
            action,
            description,
            text,
            fieldIndex,
            value,
            key,
            timeoutSeconds,
            minimumFields,
            continueOnError,
            null,
            List.of()
        );
    }
}