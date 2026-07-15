package com.arley.poc.as400.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.tn5250j.framework.tn5250.Screen5250;

import com.arley.poc.as400.config.As400ConnectionConfig;
import com.arley.poc.as400.engine.ProcessExecutor;
import com.arley.poc.as400.engine.VariableResolver;
import com.arley.poc.as400.model.ProcessDefinition;
import com.arley.poc.as400.result.ExecutionResult;
import com.arley.poc.as400.terminal.ScreenReader;
import com.arley.poc.as400.terminal.TerminalActions;
import com.arley.poc.as400.terminal.TerminalSession;

/**
 * Servicio encargado de ejecutar un proceso recibido
 * mediante la API REST sobre una sesión TN5250.
 */
@Service
public class ProcessExecutionService {

    private static final Duration CONNECTION_TIMEOUT =
        Duration.ofSeconds(20);

    /**
     * Abre una sesión independiente, construye el motor,
     * ejecuta el proceso y cierra siempre la conexión.
     */
    public ExecutionResult execute(
        ProcessDefinition process
    ) {
        As400ConnectionConfig connectionConfig =
            As400ConnectionConfig.fromEnvironment();

        VariableResolver variableResolver =
            VariableResolver.fromEnvironment();

        /*
         * Cada solicitud recibe un nombre de sesión diferente.
         * Esto evita colisiones cuando en el futuro existan
         * varias ejecuciones.
         */
        String sessionName =
            "API-" + UUID.randomUUID();

        try (
            TerminalSession terminalSession =
                new TerminalSession(
                    connectionConfig,
                    sessionName
                )
        ) {
            terminalSession.connect(
                CONNECTION_TIMEOUT
            );

            Screen5250 screen =
                terminalSession.getScreen();

            ScreenReader screenReader =
                new ScreenReader(screen);

            TerminalActions actions =
                new TerminalActions(
                    screen,
                    screenReader
                );

            ProcessExecutor processExecutor =
                new ProcessExecutor(
                    actions,
                    screenReader,
                    variableResolver
                );

            return processExecutor.execute(process);
        }
    }
}