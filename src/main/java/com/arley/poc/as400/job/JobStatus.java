package com.arley.poc.as400.job;

/**
 * Estado general de un trabajo enviado a la cola.
 */
public enum JobStatus {

    /**
     * El trabajo fue recibido y espera su turno.
     */
    PENDING,

    /**
     * El motor está ejecutando actualmente el proceso.
     */
    RUNNING,

    /**
     * El trabajo terminó.
     *
     * El resultado interno puede ser COMPLETED
     * o COMPLETED_WITH_WARNINGS.
     */
    COMPLETED,

    /**
     * La ejecución falló.
     */
    FAILED
}