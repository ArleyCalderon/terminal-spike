package com.arley.poc.as400.config;

import java.util.Properties;

import org.tn5250j.TN5250jConstants;

/**
 * Configuración necesaria para abrir una conexión TN5250.
 *
 * Las credenciales no se guardan aquí porque pertenecen al proceso
 * de autenticación y no a la conexión de red.
 */
public record As400ConnectionConfig(
    String host,
    int port,
    boolean tlsEnabled
) {

    public As400ConnectionConfig {
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException(
                "El host del AS400 no puede estar vacío."
            );
        }

        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException(
                "El puerto del AS400 no es válido: " + port
            );
        }
    }

    /**
     * Construye la configuración a partir de variables de entorno.
     *
     * Valores predeterminados para la PoC:
     * - host: pub400.com
     * - port: 992
     * - TLS: true
     */
    public static As400ConnectionConfig fromEnvironment() {
        String host = getEnvironmentVariable(
            "AS400_HOST",
            "pub400.com"
        );

        String rawPort = getEnvironmentVariable(
            "AS400_PORT",
            "992"
        );

        String rawTls = getEnvironmentVariable(
            "AS400_TLS",
            "true"
        );

        int port;

        try {
            port = Integer.parseInt(rawPort);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                "AS400_PORT debe ser un número válido: " + rawPort,
                exception
            );
        }

        boolean tlsEnabled = Boolean.parseBoolean(rawTls);

        return new As400ConnectionConfig(
            host,
            port,
            tlsEnabled
        );
    }

    /**
     * Convierte nuestra configuración al formato requerido por TN5250J.
     */
    public Properties toProperties() {
        Properties properties = new Properties();

        properties.setProperty(
            TN5250jConstants.SESSION_HOST,
            host
        );

        properties.setProperty(
            TN5250jConstants.SESSION_HOST_PORT,
            String.valueOf(port)
        );

        properties.setProperty(
            TN5250jConstants.SSL_TYPE,
            tlsEnabled
                ? TN5250jConstants.SSL_TYPE_TLS
                : TN5250jConstants.SSL_TYPE_NONE
        );

        return properties;
    }

    private static String getEnvironmentVariable(
        String variableName,
        String defaultValue
    ) {
        String value = System.getenv(variableName);

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value.trim();
    }
}