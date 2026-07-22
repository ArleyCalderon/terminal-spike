package com.arley.poc.as400.model;

/**
 * Formas soportadas para extraer información de una pantalla 5250.
 */
public enum ExtractionMode {

    /**
     * Lee una posición exacta de la pantalla utilizando
     * fila, columna y longitud.
     */
    REGION,

    /**
     * Busca una etiqueta en una fila y extrae el valor
     * ubicado después del carácter ":".
     */
    AFTER_LABEL
}