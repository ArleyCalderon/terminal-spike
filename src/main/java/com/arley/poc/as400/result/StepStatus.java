package com.arley.poc.as400.result;

/**
 * Estado individual de un paso ejecutado.
 */
public enum StepStatus {

    /**
     * El paso terminó correctamente.
     */
    COMPLETED,

    /**
     * El paso falló, pero estaba configurado
     * con continueOnError.
     */
    IGNORED_ERROR,

    /**
     * El paso falló y detuvo el proceso.
     */
    FAILED
}