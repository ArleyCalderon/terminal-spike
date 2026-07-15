package com.arley.poc.as400;

import java.time.Duration;

import org.tn5250j.framework.tn5250.Screen5250;

import com.arley.poc.as400.config.As400ConnectionConfig;
import com.arley.poc.as400.exception.TerminalTimeoutException;
import com.arley.poc.as400.terminal.ScreenReader;
import com.arley.poc.as400.terminal.TerminalActions;
import com.arley.poc.as400.terminal.TerminalSession;

/**
 * Spike funcional completo:
 *
 * 1. Conexión TLS.
 * 2. Login.
 * 3. Ejecución de DSPJOB.
 * 4. Validación de la pantalla Display Job.
 * 5. Retorno con PF3.
 * 6. SIGNOFF.
 */
public final class TerminalSpike {

    private static final Duration CONNECTION_TIMEOUT =
        Duration.ofSeconds(20);

    private static final Duration SCREEN_TIMEOUT =
        Duration.ofSeconds(20);

    private static final Duration LOGIN_TIMEOUT =
        Duration.ofSeconds(30);

    private static final Duration SIGNOFF_TIMEOUT =
        Duration.ofSeconds(15);

    private TerminalSpike() {
        // Evita crear instancias.
    }

    public static void main(String[] args) {
        String password = null;

        try {
            As400ConnectionConfig connectionConfig =
                As400ConnectionConfig.fromEnvironment();

            String username = requireEnvironmentVariable(
                "AS400_USERNAME"
            ).trim().toUpperCase();

            password = requireEnvironmentVariable(
                "AS400_PASSWORD"
            );

            printHeader(connectionConfig);

            try (
                TerminalSession terminalSession =
                    new TerminalSession(
                        connectionConfig,
                        "PUB400-SPIKE"
                    )
            ) {
                System.out.println("Iniciando conexión...");

                terminalSession.connect(CONNECTION_TIMEOUT);

                System.out.println("Conexión establecida.");
                System.out.println(
                    "TLS configurado: "
                        + terminalSession.isSslConfigured()
                );
                System.out.println(
                    "Socket TLS activo: "
                        + terminalSession.isSslSocket()
                );

                Screen5250 screen = terminalSession.getScreen();

                ScreenReader screenReader =
                    new ScreenReader(screen);

                TerminalActions actions =
                    new TerminalActions(
                        screen,
                        screenReader
                    );

                executeLogin(
                    actions,
                    screenReader,
                    username,
                    password
                );

                executeDisplayJob(
                    actions,
                    screenReader
                );

                returnToMainMenu(
                    actions,
                    screenReader
                );

                executeSignOff(
                    actions,
                    screenReader,
                    terminalSession
                );

                System.out.println();
                System.out.println(
                    "========================================"
                );
                System.out.println(
                    " SPIKE COMPLETO EJECUTADO CORRECTAMENTE"
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
            System.err.println(" ERROR EN EL SPIKE");
            System.err.println(
                "========================================"
            );
            System.err.println(exception.getMessage());
            exception.printStackTrace(System.err);

        } finally {
            /*
             * Evita conservar nuestra referencia a la contraseña
             * más tiempo del necesario.
             *
             * Para producción usaremos un gestor de secretos.
             */
            password = null;
        }
    }

    private static void executeLogin(
        TerminalActions actions,
        ScreenReader screenReader,
        String username,
        String password
    ) {
        actions.waitForLoginScreen(SCREEN_TIMEOUT);

        System.out.println();
        System.out.println("Pantalla de login detectada.");

        screenReader.printEditableFieldsMetadata();

        actions.writeEditableField(0, username);
        actions.writeEditableField(1, password);

        System.out.println(
            "Credenciales cargadas en los campos."
        );
        System.out.println("Enviando Enter...");

        actions.pressEnter();

        try {
            actions.waitForText(
                "IBM i Main Menu",
                LOGIN_TIMEOUT
            );

            actions.waitForKeyboardUnlocked(
                SCREEN_TIMEOUT
            );

        } catch (TerminalTimeoutException exception) {
            System.err.println();
            System.err.println(
                "No se confirmó el login. Pantalla actual:"
            );

            screenReader.printScreen();

            throw exception;
        }

        System.out.println();
        System.out.println(
            "========================================"
        );
        System.out.println(" LOGIN EXITOSO");
        System.out.println(
            "========================================"
        );

        screenReader.printScreen();
    }

    private static void executeDisplayJob(
        TerminalActions actions,
        ScreenReader screenReader
    ) {
        System.out.println();
        System.out.println(
            "Ejecutando comando DSPJOB..."
        );

        /*
         * En IBM i Main Menu existe un único campo editable:
         * Selection or command.
         */
        actions.writeEditableField(
            0,
            "DSPJOB"
        );

        actions.pressEnter();

        try {
            actions.waitForText(
                "Display Job",
                SCREEN_TIMEOUT
            );

            actions.waitForKeyboardUnlocked(
                SCREEN_TIMEOUT
            );

        } catch (TerminalTimeoutException exception) {
            System.err.println();
            System.err.println(
                "No apareció Display Job. Pantalla actual:"
            );

            screenReader.printScreen();

            throw exception;
        }

        System.out.println();
        System.out.println(
            "========================================"
        );
        System.out.println(" DSPJOB EJECUTADO");
        System.out.println(
            "========================================"
        );

        screenReader.printScreen();
    }

    private static void returnToMainMenu(
        TerminalActions actions,
        ScreenReader screenReader
    ) {
        System.out.println();
        System.out.println(
            "Enviando PF3 para regresar..."
        );

        actions.pressPf(3);

        try {
            actions.waitForText(
                "IBM i Main Menu",
                SCREEN_TIMEOUT
            );

            actions.waitForKeyboardUnlocked(
                SCREEN_TIMEOUT
            );

        } catch (TerminalTimeoutException exception) {
            System.err.println();
            System.err.println(
                "No se confirmó el regreso al menú principal."
            );

            screenReader.printScreen();

            throw exception;
        }

        System.out.println(
            "Regreso al menú principal confirmado."
        );
    }

    private static void executeSignOff(
        TerminalActions actions,
        ScreenReader screenReader,
        TerminalSession terminalSession
    ) {
        System.out.println();
        System.out.println("Ejecutando SIGNOFF...");

        actions.writeEditableField(
            0,
            "SIGNOFF"
        );

        actions.pressEnter();

        try {
            actions.waitUntil(
                () -> !terminalSession.isConnected()
                    || screenReader.contains(
                        "Your user name:"
                    ),
                SIGNOFF_TIMEOUT,
                "No se confirmó el cierre funcional de la sesión."
            );

            System.out.println(
                "SIGNOFF confirmado."
            );

        } catch (TerminalTimeoutException exception) {
            /*
             * No hacemos fallar toda la PoC por esto.
             * El bloque try-with-resources desconectará el socket.
             */
            System.out.println(
                "SIGNOFF fue enviado, pero no se confirmó "
                    + "la pantalla final dentro del timeout."
            );
        }
    }

    private static void printHeader(
        As400ConnectionConfig connectionConfig
    ) {
        System.out.println(
            "========================================"
        );
        System.out.println(
            " SPIKE COMPLETO TN5250J"
        );
        System.out.println(
            "========================================"
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

    private static String requireEnvironmentVariable(
        String variableName
    ) {
        String value = System.getenv(variableName);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                "Falta la variable de entorno "
                    + variableName
            );
        }

        return value;
    }
}