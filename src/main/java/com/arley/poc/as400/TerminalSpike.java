package com.arley.poc.as400;

import java.time.Duration;

import org.tn5250j.framework.tn5250.Screen5250;
import com.arley.poc.as400.result.ExecutionResult;
import com.arley.poc.as400.result.ExecutionStatus;
import com.arley.poc.as400.config.As400ConnectionConfig;
import com.arley.poc.as400.engine.ProcessExecutor;
import com.arley.poc.as400.engine.ProcessLoader;
import com.arley.poc.as400.engine.VariableResolver;
import com.arley.poc.as400.model.ProcessDefinition;
import com.arley.poc.as400.terminal.ScreenReader;
import com.arley.poc.as400.terminal.TerminalActions;
import com.arley.poc.as400.terminal.TerminalSession;

/**
 * Punto de entrada del spike.
 *
 * La definición del proceso ya no se encuentra escrita
 * directamente en Java. Se carga desde un archivo JSON
 * y se ejecuta mediante ProcessExecutor.
 */
public final class TerminalSpike {

    private static final Duration CONNECTION_TIMEOUT =
        Duration.ofSeconds(20);

    private static final String PROCESS_RESOURCE =
        "processes/dspjob.json";

    private TerminalSpike() {
        // Evita crear instancias.
    }

    public static void main(String[] args) {
        try {
            As400ConnectionConfig connectionConfig =
                As400ConnectionConfig.fromEnvironment();

            ProcessLoader processLoader =
                new ProcessLoader();

            ProcessDefinition process =
                processLoader.loadFromClasspath(
                    PROCESS_RESOURCE
                );

            VariableResolver variableResolver =
                VariableResolver.fromEnvironment();

            printHeader(
                connectionConfig,
                process
            );

            try (
                TerminalSession terminalSession =
                    new TerminalSession(
                        connectionConfig,
                        "PUB400-JSON-ENGINE"
                    )
            ) {
                System.out.println(
                    "Iniciando conexión..."
                );

                terminalSession.connect(
                    CONNECTION_TIMEOUT
                );

                System.out.println(
                    "Conexión establecida."
                );

                System.out.println(
                    "TLS configurado: "
                        + terminalSession.isSslConfigured()
                );

                System.out.println(
                    "Socket TLS activo: "
                        + terminalSession.isSslSocket()
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

                ExecutionResult executionResult =
                    processExecutor.execute(process);

                printExecutionSummary(executionResult);

                if (
                    executionResult.status()
                        == ExecutionStatus.FAILED
                ) {
                    throw new IllegalStateException(
                        "El proceso falló en el paso "
                            + executionResult.failedStepId()
                            + ": "
                            + executionResult.errorMessage()
                    );
                }

                System.out.println();
                System.out.println(
                    "========================================"
                );
                System.out.println(
                    " MOTOR JSON EJECUTADO CORRECTAMENTE"
                );
                System.out.println(
                    "========================================"
                );
            }

        } catch (Exception exception) {
            System.err.println();
            System.err.println(
                "========================================"
            );
            System.err.println(
                " ERROR EN EL MOTOR JSON"
            );
            System.err.println(
                "========================================"
            );

            System.err.println(
                exception.getMessage()
            );

            exception.printStackTrace(
                System.err
            );
        }
    }

    private static void printHeader(
        As400ConnectionConfig connectionConfig,
        ProcessDefinition process
    ) {
        System.out.println(
            "========================================"
        );
        System.out.println(
            " MOTOR PARAMETRIZABLE TN5250J"
        );
        System.out.println(
            "========================================"
        );

        System.out.println(
            "Proceso: " + process.name()
        );

        System.out.println(
            "Versión: " + process.version()
        );

        System.out.println(
            "Definición: " + PROCESS_RESOURCE
        );

        System.out.println(
            "Host: " + connectionConfig.host()
        );

        System.out.println(
            "Puerto: " + connectionConfig.port()
        );

        System.out.println(
            "TLS solicitado: "
                + connectionConfig.tlsEnabled()
        );

        System.out.println();
    }
    private static void printExecutionSummary(
    ExecutionResult result
) {
    System.out.println();
    System.out.println(
        "========================================"
    );
    System.out.println(
        " RESULTADO ESTRUCTURADO"
    );
    System.out.println(
        "========================================"
    );

    System.out.println(
        "Proceso: " + result.processId()
    );

    System.out.println(
        "Estado: " + result.status()
    );

    System.out.println(
        "Duración: "
            + result.durationMs()
            + " ms"
    );

    System.out.println(
        "Pasos ejecutados: "
            + result.steps().size()
    );

    if (result.failedStepId() != null) {
        System.out.println(
            "Paso fallido: "
                + result.failedStepId()
        );
    }

    if (result.errorMessage() != null) {
        System.out.println(
            "Error: "
                + result.errorMessage()
        );
    }
}
}