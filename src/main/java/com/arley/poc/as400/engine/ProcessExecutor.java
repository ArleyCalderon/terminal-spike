package com.arley.poc.as400.engine;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import com.arley.poc.as400.model.ProcessDefinition;
import com.arley.poc.as400.model.StepDefinition;
import com.arley.poc.as400.result.ExecutionResult;
import com.arley.poc.as400.result.ExecutionStatus;
import com.arley.poc.as400.result.StepResult;
import com.arley.poc.as400.result.StepStatus;
import com.arley.poc.as400.terminal.ScreenReader;
import com.arley.poc.as400.terminal.TerminalActions;

/**
 * Ejecuta procesos definidos mediante JSON sobre una terminal 5250.
 */
public final class ProcessExecutor {

    private static final int DEFAULT_TIMEOUT_SECONDS = 20;

    private final TerminalActions actions;
    private final ScreenReader screenReader;
    private final VariableResolver variableResolver;
    private final ScreenDataExtractor screenDataExtractor;


    public ProcessExecutor(
        TerminalActions actions,
        ScreenReader screenReader,
        VariableResolver variableResolver
    ) {
        this.actions = Objects.requireNonNull(
            actions,
            "TerminalActions no puede ser nulo."
        );

        this.screenReader = Objects.requireNonNull(
            screenReader,
            "ScreenReader no puede ser nulo."
        );

        this.screenDataExtractor =
        new ScreenDataExtractor(this.screenReader);


        this.variableResolver = Objects.requireNonNull(
            variableResolver,
            "VariableResolver no puede ser nulo."
        );
    }

    /**
     * Ejecuta todos los pasos y devuelve un resultado estructurado.
     */
    public ExecutionResult execute(
        ProcessDefinition process
    ) {
        Objects.requireNonNull(
            process,
            "La definición del proceso no puede ser nula."
        );

        Instant processStartedAt = Instant.now();
        long processStartedNanos = System.nanoTime();

        List<StepResult> stepResults =
            new ArrayList<>();

        Map<String, Map<String, String>> extractedData =
        new LinkedHashMap<>();

        boolean hasIgnoredErrors = false;

        printProcessHeader(process);

        int totalSteps = process.steps().size();

        for (int index = 0; index < totalSteps; index++) {
            StepDefinition step =
                process.steps().get(index);

            StepResult stepResult =
            executeStepWithErrorHandling(
                step,
                index + 1,
                totalSteps,
                extractedData
            );

            stepResults.add(stepResult);

            if (
                stepResult.status()
                    == StepStatus.IGNORED_ERROR
            ) {
                hasIgnoredErrors = true;
            }

            if (
                stepResult.status()
                    == StepStatus.FAILED
            ) {
                ExecutionResult result =
                    buildExecutionResult(
                        process,
                        ExecutionStatus.FAILED,
                        processStartedAt,
                        processStartedNanos,
                        stepResults,
                        extractedData,
                        step.id(),
                        stepResult.errorMessage()
                    );

                printProcessFooter(result.status());

                return result;
            }
        }

        ExecutionStatus executionStatus =
            hasIgnoredErrors
                ? ExecutionStatus.COMPLETED_WITH_WARNINGS
                : ExecutionStatus.COMPLETED;

        ExecutionResult result =
            buildExecutionResult(
                process,
                executionStatus,
                processStartedAt,
                processStartedNanos,
                stepResults,
                extractedData,
                null,
                null
            );

        printProcessFooter(result.status());

        return result;
    }

    private StepResult executeStepWithErrorHandling(
    StepDefinition step,
    int currentStep,
    int totalSteps,
    Map<String, Map<String, String>> extractedData
    ) {
        printStepHeader(
            step,
            currentStep,
            totalSteps
        );

        Instant stepStartedAt = Instant.now();
        long stepStartedNanos = System.nanoTime();

        try {
           executeStep(
                step,
                extractedData
            );

            Instant stepFinishedAt = Instant.now();

            StepResult result = new StepResult(
                step.id(),
                step.action(),
                step.description(),
                StepStatus.COMPLETED,
                stepStartedAt,
                stepFinishedAt,
                elapsedMilliseconds(stepStartedNanos),
                null
            );

            System.out.println("Resultado: OK");

            return result;

        } catch (RuntimeException exception) {
            Instant stepFinishedAt = Instant.now();

            StepStatus stepStatus =
                step.continueOnError()
                    ? StepStatus.IGNORED_ERROR
                    : StepStatus.FAILED;

            StepResult result = new StepResult(
                step.id(),
                step.action(),
                step.description(),
                stepStatus,
                stepStartedAt,
                stepFinishedAt,
                elapsedMilliseconds(stepStartedNanos),
                exception.getMessage()
            );

            if (step.continueOnError()) {
                System.out.println(
                    "Resultado: ERROR IGNORADO"
                );
            } else {
                System.out.println(
                    "Resultado: ERROR"
                );
            }

            System.out.println(
                "Detalle: " + exception.getMessage()
            );

            return result;
        }
    }

private ExecutionResult buildExecutionResult(
    ProcessDefinition process,
    ExecutionStatus status,
    Instant processStartedAt,
    long processStartedNanos,
    List<StepResult> stepResults,
    Map<String, Map<String, String>> extractedData,
    String failedStepId,
    String errorMessage
) {
       return new ExecutionResult(
        process.id(),
        process.name(),
        process.version(),
        status,
        processStartedAt,
        Instant.now(),
        elapsedMilliseconds(
            processStartedNanos
        ),
        stepResults,
        extractedData,
        failedStepId,
        errorMessage
    );
    }

    private void executeStep(
    StepDefinition step,
    Map<String, Map<String, String>> extractedData
    ) {
        Duration timeout = resolveTimeout(step);

        switch (step.action()) {
            case WAIT_FOR_TEXT ->
                actions.waitForText(
                    requireText(step),
                    timeout
                );

            case WAIT_FOR_KEYBOARD_UNLOCKED ->
                actions.waitForKeyboardUnlocked(
                    timeout
                );

            case WAIT_FOR_EDITABLE_FIELDS ->
                waitForEditableFields(
                    step,
                    timeout
                );

            case WRITE_FIELD ->
                writeField(step);

            case PRESS_KEY ->
                pressKey(step);

            case PRINT_SCREEN ->
            screenReader.printScreen();

            case EXTRACT_SCREEN_DATA ->
                extractScreenData(step,extractedData);
        }
    }

    /**
 * Extrae información de la pantalla actual y la guarda
 * bajo el target definido en el paso.
 */
private void extractScreenData(
    StepDefinition step,
    Map<String, Map<String, String>> extractedData
) {
    String target = step.target();

    if (target == null || target.isBlank()) {
        throw invalidStep(
            step,
            "target es obligatorio para EXTRACT_SCREEN_DATA."
        );
    }

    if (step.fields() == null || step.fields().isEmpty()) {
        throw invalidStep(
            step,
            "Debe existir al menos un campo para extraer."
        );
    }

    if (extractedData.containsKey(target)) {
        throw invalidStep(
            step,
            "Ya existen datos extraídos para el target: "
                + target
        );
    }

    Map<String, String> values =
        screenDataExtractor.extract(
            step.fields()
        );

    extractedData.put(
        target,
        values
    );

    /*
     * Mostramos los nombres de los campos, pero no sus valores,
     * porque en procesos reales podrían contener datos sensibles.
     */
    System.out.println(
        "Campos extraídos ["
            + target
            + "]: "
            + values.keySet()
    );
}

    private void waitForEditableFields(
        StepDefinition step,
        Duration timeout
    ) {
        Integer minimumFields =
            step.minimumFields();

        if (
            minimumFields == null
                || minimumFields <= 0
        ) {
            throw invalidStep(
                step,
                "minimumFields debe ser mayor que cero."
            );
        }

        actions.waitUntil(
            () -> screenReader.hasAtLeastEditableFields(
                minimumFields
            ),
            timeout,
            "No aparecieron al menos "
                + minimumFields
                + " campos editables."
        );
    }

    private void writeField(
        StepDefinition step
    ) {
        Integer fieldIndex =
            step.fieldIndex();

        if (
            fieldIndex == null
                || fieldIndex < 0
        ) {
            throw invalidStep(
                step,
                "fieldIndex debe ser cero o positivo."
            );
        }

        if (step.value() == null) {
            throw invalidStep(
                step,
                "value es obligatorio para WRITE_FIELD."
            );
        }

        String resolvedValue =
            variableResolver.resolve(
                step.value()
            );

        /*
         * No imprimimos el valor porque podría contener
         * credenciales u otra información sensible.
         */
        actions.writeEditableField(
            fieldIndex,
            resolvedValue
        );
    }

    private void pressKey(
        StepDefinition step
    ) {
        if (
            step.key() == null
                || step.key().isBlank()
        ) {
            throw invalidStep(
                step,
                "key es obligatorio para PRESS_KEY."
            );
        }

        String normalizedKey =
            step.key()
                .trim()
                .toUpperCase(Locale.ROOT);

        if ("ENTER".equals(normalizedKey)) {
            actions.pressEnter();
            return;
        }

        if (normalizedKey.startsWith("PF")) {
            pressPfKey(
                step,
                normalizedKey
            );

            return;
        }

        throw invalidStep(
            step,
            "Tecla no soportada: " + step.key()
        );
    }

    private void pressPfKey(
        StepDefinition step,
        String normalizedKey
    ) {
        String rawNumber =
            normalizedKey.substring(2);

        if (rawNumber.isBlank()) {
            throw invalidStep(
                step,
                "La tecla PF debe incluir un número."
            );
        }

        int pfNumber;

        try {
            pfNumber =
                Integer.parseInt(rawNumber);

        } catch (
            NumberFormatException exception
        ) {
            throw invalidStep(
                step,
                "Número de tecla PF inválido: "
                    + rawNumber
            );
        }

        actions.pressPf(pfNumber);
    }

    private String requireText(
        StepDefinition step
    ) {
        if (
            step.text() == null
                || step.text().isBlank()
        ) {
            throw invalidStep(
                step,
                "text es obligatorio para WAIT_FOR_TEXT."
            );
        }

        return step.text();
    }

    private Duration resolveTimeout(
        StepDefinition step
    ) {
        Integer timeoutSeconds =
            step.timeoutSeconds();

        if (timeoutSeconds == null) {
            return Duration.ofSeconds(
                DEFAULT_TIMEOUT_SECONDS
            );
        }

        if (timeoutSeconds <= 0) {
            throw invalidStep(
                step,
                "timeoutSeconds debe ser mayor que cero."
            );
        }

        return Duration.ofSeconds(
            timeoutSeconds
        );
    }

    private long elapsedMilliseconds(
        long startedNanos
    ) {
        return Duration.ofNanos(
            System.nanoTime() - startedNanos
        ).toMillis();
    }

    private IllegalArgumentException invalidStep(
        StepDefinition step,
        String message
    ) {
        return new IllegalArgumentException(
            "Paso inválido "
                + step.id()
                + " ["
                + step.action()
                + "]: "
                + message
        );
    }

    private void printProcessHeader(
        ProcessDefinition process
    ) {
        System.out.println();
        System.out.println(
            "========================================"
        );
        System.out.println(
            " PROCESO: " + process.name()
        );
        System.out.println(
            " VERSIÓN: " + process.version()
        );
        System.out.println(
            "========================================"
        );
    }

    private void printStepHeader(
        StepDefinition step,
        int currentStep,
        int totalSteps
    ) {
        System.out.println();

        System.out.printf(
            "[%d/%d] %s%n",
            currentStep,
            totalSteps,
            step.id()
        );

        System.out.println(
            "Acción: " + step.action()
        );

        if (
            step.description() != null
                && !step.description().isBlank()
        ) {
            System.out.println(
                "Descripción: "
                    + step.description()
            );
        }
    }

    private void printProcessFooter(
        ExecutionStatus status
    ) {
        System.out.println();
        System.out.println(
            "========================================"
        );
        System.out.println(
            " PROCESO FINALIZADO: " + status
        );
        System.out.println(
            "========================================"
        );
    }
}