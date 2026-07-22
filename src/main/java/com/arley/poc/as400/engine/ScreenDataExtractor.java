package com.arley.poc.as400.engine;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.arley.poc.as400.model.ExtractionFieldDefinition;
import com.arley.poc.as400.terminal.ScreenReader;

/**
 * Ejecuta las reglas de extracción definidas en el JSON
 * sobre la pantalla 5250 actual.
 */
public final class ScreenDataExtractor {

    private final ScreenReader screenReader;

    public ScreenDataExtractor(
        ScreenReader screenReader
    ) {
        this.screenReader = Objects.requireNonNull(
            screenReader,
            "ScreenReader no puede ser nulo."
        );
    }

    /**
     * Extrae todos los campos definidos y devuelve
     * un mapa con nombre y valor.
     *
     * Ejemplo:
     *
     * {
     *   "jobName": "QPADEV002J",
     *   "status": "ACTIVE"
     * }
     */
    public Map<String, String> extract(
        List<ExtractionFieldDefinition> fields
    ) {
        if (fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException(
                "Debe existir al menos un campo de extracción."
            );
        }

        Map<String, String> extractedValues =
            new LinkedHashMap<>();

        for (ExtractionFieldDefinition field : fields) {
            Objects.requireNonNull(
                field,
                "La definición del campo no puede ser nula."
            );

            if (extractedValues.containsKey(field.name())) {
                throw new IllegalArgumentException(
                    "El campo de extracción está repetido: "
                        + field.name()
                );
            }

            String extractedValue =
                extractField(field);

            extractedValues.put(
                field.name(),
                extractedValue
            );
        }

        /*
         * Conservamos el orden de los campos definido
         * en el JSON, pero evitamos modificaciones externas.
         */
        return Collections.unmodifiableMap(
            new LinkedHashMap<>(extractedValues)
        );
    }

    private String extractField(
        ExtractionFieldDefinition field
    ) {
        return switch (field.mode()) {
            case REGION ->
                screenReader.readRegion(
                    field.row(),
                    field.column(),
                    field.length()
                );

            case AFTER_LABEL ->
                screenReader.readAfterLabel(
                    field.label()
                );
        };
    }
}