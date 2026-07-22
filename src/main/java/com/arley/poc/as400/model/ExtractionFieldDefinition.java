package com.arley.poc.as400.model;

import java.util.Objects;

/**
 * Define un valor que debe extraerse de una pantalla 5250.
 *
 * REGION utiliza:
 * - row
 * - column
 * - length
 *
 * AFTER_LABEL utiliza:
 * - label
 */
public record ExtractionFieldDefinition(
    String name,
    ExtractionMode mode,
    String label,
    Integer row,
    Integer column,
    Integer length
) {

    public ExtractionFieldDefinition {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                "El nombre del campo de extracción no puede estar vacío."
            );
        }

        Objects.requireNonNull(
            mode,
            "El modo de extracción no puede ser nulo."
        );

        if (mode == ExtractionMode.REGION) {
            validateRegion(
                name,
                row,
                column,
                length
            );
        }

        if (
            mode == ExtractionMode.AFTER_LABEL
                && (label == null || label.isBlank())
        ) {
            throw new IllegalArgumentException(
                "El campo "
                    + name
                    + " debe incluir label para usar AFTER_LABEL."
            );
        }
    }

    private static void validateRegion(
        String name,
        Integer row,
        Integer column,
        Integer length
    ) {
        if (row == null || row <= 0) {
            throw new IllegalArgumentException(
                "El campo "
                    + name
                    + " debe incluir una fila mayor que cero."
            );
        }

        if (column == null || column <= 0) {
            throw new IllegalArgumentException(
                "El campo "
                    + name
                    + " debe incluir una columna mayor que cero."
            );
        }

        if (length == null || length <= 0) {
            throw new IllegalArgumentException(
                "El campo "
                    + name
                    + " debe incluir una longitud mayor que cero."
            );
        }
    }
}