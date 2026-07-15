package com.arley.poc.as400.terminal;

import java.time.Duration;
import java.util.function.BooleanSupplier;

import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.framework.tn5250.ScreenField;

import com.arley.poc.as400.exception.TerminalTimeoutException;

/**
 * Operaciones que modifican o controlan una pantalla 5250.
 */
public final class TerminalActions {

    private static final long POLL_INTERVAL_MS = 200;

    private final Screen5250 screen;
    private final ScreenReader screenReader;

    public TerminalActions(
        Screen5250 screen,
        ScreenReader screenReader
    ) {
        if (screen == null) {
            throw new IllegalArgumentException(
                "La pantalla 5250 no puede ser nula."
            );
        }

        if (screenReader == null) {
            throw new IllegalArgumentException(
                "ScreenReader no puede ser nulo."
            );
        }

        this.screen = screen;
        this.screenReader = screenReader;
    }

    /**
     * Espera a que aparezca un texto en la pantalla.
     */
    public void waitForText(
        String expectedText,
        Duration timeout
    ) {
        waitUntil(
            () -> screenReader.contains(expectedText),
            timeout,
            "No apareció el texto esperado: " + expectedText
        );
    }

    /**
     * Espera la pantalla utilizada por PUB400 para autenticación.
     */
    public void waitForLoginScreen(Duration timeout) {
        waitUntil(
            () -> screenReader.contains("Your user name:")
                && screenReader.hasAtLeastEditableFields(2)
                && screenReader.isKeyboardUnlocked(),
            timeout,
            "No apareció una pantalla de login utilizable."
        );
    }

    /**
     * Espera hasta que el host permita enviar teclas.
     */
    public void waitForKeyboardUnlocked(Duration timeout) {
        waitUntil(
            screenReader::isKeyboardUnlocked,
            timeout,
            "El teclado de la terminal permaneció bloqueado."
        );
    }

    /**
     * Escribe sobre un campo editable usando su índice.
     */
    public void writeEditableField(
        int fieldIndex,
        String value
    ) {
        if (value == null) {
            throw new IllegalArgumentException(
                "El valor del campo no puede ser nulo."
            );
        }

        if (!screenReader.isKeyboardUnlocked()) {
            throw new IllegalStateException(
                "No se puede escribir porque el teclado está bloqueado."
            );
        }

        ScreenField field = screenReader.getEditableField(fieldIndex);

        if (value.length() > field.getLength()) {
            throw new IllegalArgumentException(
                "El valor supera la longitud máxima del campo "
                    + fieldIndex
                    + ". Máximo: "
                    + field.getLength()
                    + ", recibido: "
                    + value.length()
            );
        }

        field.setString(value);
    }

    public void pressEnter() {
        sendKey("[enter]");
    }

    public void pressPf(int pfNumber) {
        if (pfNumber < 1 || pfNumber > 24) {
            throw new IllegalArgumentException(
                "La tecla PF debe estar entre PF1 y PF24."
            );
        }

        sendKey("[pf" + pfNumber + "]");
    }

    /**
     * Método genérico de espera basada en una condición real.
     */
    public void waitUntil(
        BooleanSupplier condition,
        Duration timeout,
        String timeoutMessage
    ) {
        long deadline = System.nanoTime() + timeout.toNanos();

        while (System.nanoTime() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }

            try {
                /*
                 * Es el intervalo de polling.
                 * No representa una espera fija del proceso.
                 */
                Thread.sleep(POLL_INTERVAL_MS);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();

                throw new TerminalTimeoutException(
                    "La espera fue interrumpida.",
                    exception
                );
            }
        }

        throw new TerminalTimeoutException(timeoutMessage);
    }

    private void sendKey(String keyMnemonic) {
        if (!screenReader.isKeyboardUnlocked()) {
            throw new IllegalStateException(
                "No se puede enviar "
                    + keyMnemonic
                    + " porque el teclado está bloqueado."
            );
        }

        screen.sendKeys(keyMnemonic);
    }
}

//Escribir en campos
//Enter
//PF1–PF24
//Esperas por texto
//Esperas por desbloqueo
//Timeouts