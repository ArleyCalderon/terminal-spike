package com.arley.poc.as400.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

class VariableResolverTest {

    @Test
    void shouldResolveAs400UsernameInUppercase() {
        VariableResolver resolver =
            new VariableResolver(
                Map.of(
                    "AS400_USERNAME",
                    " Arley "
                )
            );

        String result = resolver.resolve(
            "${AS400_USERNAME}"
        );

        assertEquals(
            "ARLEY",
            result
        );
    }

    @Test
    void shouldPreservePasswordExactly() {
        String password =
            "  Clave$2026!!  ";

        VariableResolver resolver =
            new VariableResolver(
                Map.of(
                    "AS400_PASSWORD",
                    password
                )
            );

        String result = resolver.resolve(
            "${AS400_PASSWORD}"
        );

        assertEquals(
            password,
            result
        );
    }

    @Test
    void shouldResolveVariablesInsideLargerText() {
        VariableResolver resolver =
            new VariableResolver(
                Map.of(
                    "AS400_USERNAME",
                    "arley",
                    "COMMAND",
                    "DSPJOB"
                )
            );

        String result = resolver.resolve(
            "Usuario=${AS400_USERNAME};"
                + "Comando=${COMMAND}"
        );

        assertEquals(
            "Usuario=ARLEY;Comando=DSPJOB",
            result
        );
    }

    @Test
    void shouldReturnTextWithoutVariablesUnchanged() {
        VariableResolver resolver =
            new VariableResolver(
                Map.of()
            );

        String result = resolver.resolve(
            "DSPJOB"
        );

        assertEquals(
            "DSPJOB",
            result
        );
    }

    @Test
    void shouldReturnNullWhenInputIsNull() {
        VariableResolver resolver =
            new VariableResolver(
                Map.of()
            );

        String result = resolver.resolve(null);

        assertEquals(
            null,
            result
        );
    }

    @Test
    void shouldFailWhenVariableDoesNotExist() {
        VariableResolver resolver =
            new VariableResolver(
                Map.of()
            );

        IllegalStateException exception =
            assertThrows(
                IllegalStateException.class,
                () -> resolver.resolve(
                    "${AS400_PASSWORD}"
                )
            );

        assertTrue(
            exception
                .getMessage()
                .contains("AS400_PASSWORD")
        );
    }
}