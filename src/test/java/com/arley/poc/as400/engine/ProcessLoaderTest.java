package com.arley.poc.as400.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.arley.poc.as400.model.ProcessDefinition;
import com.arley.poc.as400.model.StepAction;

class ProcessLoaderTest {

    @Test
    void shouldLoadDspJobProcessFromClasspath() {
        ProcessLoader loader = new ProcessLoader();

        ProcessDefinition process = loader.loadFromClasspath(
            "processes/dspjob.json"
        );

        assertNotNull(process);
        assertEquals("dspjob", process.id());
        assertEquals("Consulta DSPJOB", process.name());
        assertFalse(process.steps().isEmpty());

        assertEquals(
            StepAction.WAIT_FOR_TEXT,
            process.steps().get(0).action()
        );

        var usernameStep = process.steps()
            .stream()
            .filter(step -> "write-username".equals(step.id()))
            .findFirst()
            .orElseThrow();

        assertEquals(
            "${AS400_USERNAME}",
            usernameStep.value()
        );
    }
}