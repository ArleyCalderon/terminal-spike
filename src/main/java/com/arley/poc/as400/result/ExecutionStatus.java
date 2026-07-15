package com.arley.poc.as400.result;

/**
 * Estado general de una ejecución.
 */
public enum ExecutionStatus {

    /**
     * Todos los pasos finalizaron correctamente.
     */
    COMPLETED,

    /**
     * El proceso terminó, pero uno o más errores
     * fueron ignorados mediante continueOnError.
     */
    COMPLETED_WITH_WARNINGS,

    /**
     * Un paso obligatorio falló y el proceso se detuvo.
     */
    FAILED
}