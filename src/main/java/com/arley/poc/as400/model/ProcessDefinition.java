package com.arley.poc.as400.model;

import java.util.List;

/**
 * Representa la definición completa de un proceso ejecutable.
 *
 * Posteriormente esta clase será construida por Jackson
 * a partir de un archivo JSON.
 */
public record ProcessDefinition(
    String id,
    String name,
    String version,
    String description,
    List<StepDefinition> steps
) {

    public ProcessDefinition {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                "El identificador del proceso no puede estar vacío."
            );
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                "El nombre del proceso no puede estar vacío."
            );
        }

        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException(
                "La versión del proceso no puede estar vacía."
            );
        }

        if (steps == null || steps.isEmpty()) {
            throw new IllegalArgumentException(
                "El proceso debe contener al menos un paso."
            );
        }

        for (StepDefinition step : steps) {
            if (step == null) {
                throw new IllegalArgumentException(
                    "La lista de pasos no puede contener valores nulos."
                );
            }
        }

        /*
         * Creamos una copia inmutable para evitar que alguien
         * modifique los pasos después de construir el proceso.
         */
        steps = List.copyOf(steps);
    }
}

/*{
  "id": "dspjob",
  "name": "Consulta de trabajo AS400",
  "version": "1.0",
  "description": "Login, ejecución de DSPJOB y cierre de sesión",
  "steps": [
    {
      "id": "wait-login",
      "action": "WAIT_FOR_TEXT",
      "text": "Your user name:",
      "timeoutSeconds": 20,
      "continueOnError": false
    }
  ]
}*/