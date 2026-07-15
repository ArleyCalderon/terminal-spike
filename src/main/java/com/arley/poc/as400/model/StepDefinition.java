package com.arley.poc.as400.model;

/**
 * Representa un paso individual de un proceso definido en JSON.
 *
 * No todas las propiedades se utilizan en todas las acciones.
 * La acción determina cuáles son necesarias.
 *
 * Ejemplos:
 *
 * WAIT_FOR_TEXT:
 * - text
 * - timeoutSeconds
 *
 * WRITE_FIELD:
 * - fieldIndex
 * - value
 *
 * PRESS_KEY:
 * - key
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
    boolean continueOnError
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
    }
}

/*
{
  "id": "write-command",
  "action": "WRITE_FIELD",
  "description": "Escribir DSPJOB en el campo de comandos",
  "fieldIndex": 0,
  "value": "DSPJOB",
  "continueOnError": false
}
 */