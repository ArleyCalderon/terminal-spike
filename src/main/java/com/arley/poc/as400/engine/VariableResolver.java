package com.arley.poc.as400.engine;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resuelve variables declaradas mediante la sintaxis:
 *
 * ${VARIABLE_NAME}
 *
 * Ejemplo:
 *
 * ${AS400_USERNAME}
 */
public final class VariableResolver {

    private static final Pattern VARIABLE_PATTERN =
        Pattern.compile(
            "\\$\\{([A-Za-z_][A-Za-z0-9_]*)}"
        );

    private final Map<String, String> variables;

    /**
     * Permite inyectar las variables.
     *
     * Esto facilita las pruebas sin depender de las variables
     * reales del sistema operativo.
     */
    public VariableResolver(
        Map<String, String> variables
    ) {
        Objects.requireNonNull(
            variables,
            "El mapa de variables no puede ser nulo."
        );

        this.variables = Map.copyOf(variables);
    }

    /**
     * Crea un resolvedor usando las variables de entorno
     * del proceso Java actual.
     */
    public static VariableResolver fromEnvironment() {
        return new VariableResolver(
            System.getenv()
        );
    }

    /**
     * Reemplaza todas las variables encontradas dentro de un texto.
     *
     * Ejemplos:
     *
     * ${AS400_USERNAME}
     *
     * Usuario=${AS400_USERNAME}
     */
    public String resolve(String value) {
        if (value == null) {
            return null;
        }

        Matcher matcher =
            VARIABLE_PATTERN.matcher(value);

        StringBuffer resolvedValue =
            new StringBuffer();

        while (matcher.find()) {
            String variableName =
                matcher.group(1);

            String variableValue =
                variables.get(variableName);

            if (variableValue == null) {
                throw new IllegalStateException(
                    "No existe la variable requerida: "
                        + variableName
                );
            }

            String normalizedValue =
                normalizeValue(
                    variableName,
                    variableValue
                );

            /*
             * quoteReplacement evita que caracteres especiales
             * como $ o \\ dentro de una contraseña sean
             * interpretados por el motor de expresiones regulares.
             */
            matcher.appendReplacement(
                resolvedValue,
                Matcher.quoteReplacement(
                    normalizedValue
                )
            );
        }

        matcher.appendTail(resolvedValue);

        return resolvedValue.toString();
    }

    /**
     * Aplica reglas especiales únicamente a variables conocidas.
     *
     * El usuario de IBM i se normaliza porque los perfiles
     * se manejan en mayúsculas.
     *
     * Las demás variables, especialmente la contraseña,
     * permanecen exactamente como fueron recibidas.
     */
    private String normalizeValue(
        String variableName,
        String variableValue
    ) {
        if ("AS400_USERNAME".equals(variableName)) {
            return variableValue
                .trim()
                .toUpperCase(Locale.ROOT);
        }

        return variableValue;
    }
}
