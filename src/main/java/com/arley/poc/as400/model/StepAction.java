package com.arley.poc.as400.model;

/**
 * Acciones que el motor puede ejecutar sobre una terminal 5250.
 *
 * Cada valor podrá ser utilizado desde la propiedad "action"
 * de un paso definido en JSON.
 */
public enum StepAction {

    /**
     * Espera hasta que un texto aparezca en la pantalla.
     */
    WAIT_FOR_TEXT,

    /**
     * Espera hasta que el teclado de la terminal quede desbloqueado.
     */
    WAIT_FOR_KEYBOARD_UNLOCKED,

    /**
     * Espera hasta que exista una cantidad mínima de campos editables.
     */
    WAIT_FOR_EDITABLE_FIELDS,

    /**
     * Escribe un valor en un campo editable identificado por índice.
     */
    WRITE_FIELD,

    /**
     * Envía una tecla como ENTER, PF3 o PF12.
     */
    PRESS_KEY,

    /**
     * Imprime el contenido actual de la pantalla.
     */
    PRINT_SCREEN,

    /**
     * Extrae valores de la pantalla actual y los agrega
     * al resultado estructurado del proceso.
     */
    EXTRACT_SCREEN_DATA
}