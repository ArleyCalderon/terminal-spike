package com.arley.poc.as400.engine;

import java.io.IOException;
import java.io.InputStream;

import com.arley.poc.as400.model.ProcessDefinition;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 * Carga definiciones de procesos almacenadas como recursos JSON.
 */
public final class ProcessLoader {

    private final ObjectMapper objectMapper;

    public ProcessLoader() {
        this.objectMapper = JsonMapper.builder()
            /*
             * Hace fallar la carga si el JSON contiene propiedades
             * que no existen en nuestros modelos.
             */
            .enable(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
            )

            /*
             * Evita aceptar contenido adicional después de finalizar
             * el objeto JSON principal.
             */
            .enable(
                DeserializationFeature.FAIL_ON_TRAILING_TOKENS
            )
            .build();
    }

    /**
     * Carga un proceso desde src/main/resources.
     *
     * Ejemplo:
     * processes/dspjob.json
     */
    public ProcessDefinition loadFromClasspath(
        String resourcePath
    ) {
        if (resourcePath == null || resourcePath.isBlank()) {
            throw new IllegalArgumentException(
                "La ruta del proceso no puede estar vacía."
            );
        }

        String normalizedPath = resourcePath.startsWith("/")
            ? resourcePath.substring(1)
            : resourcePath;

        ClassLoader classLoader =
            ProcessLoader.class.getClassLoader();

        try (
            InputStream inputStream =
                classLoader.getResourceAsStream(normalizedPath)
        ) {
            if (inputStream == null) {
                throw new IllegalArgumentException(
                    "No existe el recurso: " + normalizedPath
                );
            }

            return objectMapper.readValue(
                inputStream,
                ProcessDefinition.class
            );

        } catch (IOException exception) {
            throw new IllegalStateException(
                "No fue posible cargar el proceso: "
                    + normalizedPath,
                exception
            );
        }
    }
}