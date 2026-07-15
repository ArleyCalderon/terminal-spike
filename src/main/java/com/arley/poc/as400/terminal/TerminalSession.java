package com.arley.poc.as400.terminal;

import java.time.Duration;

import org.tn5250j.Session5250;
import org.tn5250j.framework.common.SessionManager;
import org.tn5250j.framework.tn5250.Screen5250;

import com.arley.poc.as400.config.As400ConnectionConfig;
import com.arley.poc.as400.exception.TerminalTimeoutException;

/**
 * Administra el ciclo de vida de una sesión TN5250.
 */
public final class TerminalSession implements AutoCloseable {

    private static final long POLL_INTERVAL_MS = 200;

    private final Session5250 session;

    private boolean connectionStarted;

    public TerminalSession(
        As400ConnectionConfig connectionConfig,
        String sessionName
    ) {
        if (connectionConfig == null) {
            throw new IllegalArgumentException(
                "La configuración de conexión no puede ser nula."
            );
        }

        if (sessionName == null || sessionName.isBlank()) {
            throw new IllegalArgumentException(
                "El nombre de sesión no puede estar vacío."
            );
        }

        this.session = SessionManager.instance().openSession(
            connectionConfig.toProperties(),
            "",
            sessionName
        );
    }

    /**
     * Inicia la conexión y espera a que TN5250J confirme
     * que el socket está conectado.
     */
    public void connect(Duration timeout) {
        if (connectionStarted) {
            throw new IllegalStateException(
                "La conexión ya fue iniciada."
            );
        }

        session.connect();
        connectionStarted = true;

        long deadline = System.nanoTime() + timeout.toNanos();

        while (System.nanoTime() < deadline) {
            if (session.isConnected()) {
                return;
            }

            try {
                Thread.sleep(POLL_INTERVAL_MS);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();

                throw new TerminalTimeoutException(
                    "La conexión fue interrumpida.",
                    exception
                );
            }
        }

        throw new TerminalTimeoutException(
            "No fue posible establecer la conexión dentro del tiempo esperado."
        );
    }

    public boolean isConnected() {
        return session.isConnected();
    }

    public boolean isSslConfigured() {
        return session.isSslConfigured();
    }

    public boolean isSslSocket() {
        return session.isSslSocket();
    }

    public Screen5250 getScreen() {
        return session.getScreen();
    }

    /**
     * Cierra la conexión aunque el flujo haya fallado.
     */
    @Override
    public void close() {
        if (!connectionStarted) {
            return;
        }

        try {
            session.disconnect();
        } finally {
            connectionStarted = false;
        }
    }
}
/*
Crear sesión
Conectar
Esperar conexión
Consultar TLS
Entregar la pantalla
Desconectar*/