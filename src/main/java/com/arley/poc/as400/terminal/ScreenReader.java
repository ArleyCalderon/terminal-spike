package com.arley.poc.as400.terminal;

import java.util.ArrayList;
import java.util.List;

import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.framework.tn5250.ScreenField;
import org.tn5250j.framework.tn5250.ScreenFields;

/**
 * Encapsula las operaciones de lectura sobre una pantalla 5250.
 */
public final class ScreenReader {

    private final Screen5250 screen;

    public ScreenReader(Screen5250 screen) {
        if (screen == null) {
            throw new IllegalArgumentException(
                "La pantalla 5250 no puede ser nula."
            );
        }

        this.screen = screen;
    }

    /**
     * Devuelve todo el presentation space como texto.
     */
    public String getText() {
        char[] characters = screen.getScreenAsChars();

        if (characters == null) {
            return "";
        }

        return new String(characters)
            .replace('\0', ' ');
    }

    /**
     * Devuelve una fila completa de la pantalla.
     *
     * La fila recibida utiliza coordenadas visibles:
     * la primera fila es 1.
     */
    public String readLine(int row) {
        int rows = screen.getRows();
        int columns = screen.getColumns();

        if (row <= 0 || row > rows) {
            throw new IllegalArgumentException(
                "La fila debe estar entre 1 y "
                    + rows
                    + ". Valor recibido: "
                    + row
            );
        }

        char[] characters = screen.getScreenAsChars();

        if (characters == null) {
            return "";
        }

        int start = (row - 1) * columns;

        if (start >= characters.length) {
            return "";
        }

        int length = Math.min(
            columns,
            characters.length - start
        );

        return new String(
            characters,
            start,
            length
        )
            .replace('\0', ' ');
    }

    /**
     * Comprueba si la pantalla contiene un texto determinado.
     */
    public boolean contains(String expectedText) {
        if (expectedText == null || expectedText.isEmpty()) {
            return false;
        }

        return getText().contains(expectedText);
    }

    /**
     * Obtiene únicamente los campos editables.
     *
     * Los campos bypass son textos protegidos que el usuario
     * no puede modificar.
     */
    public List<ScreenField> getEditableFields() {
        ScreenFields screenFields = screen.getScreenFields();

        if (screenFields == null) {
            return List.of();
        }

        ScreenField[] allFields = screenFields.getFields();

        if (allFields == null || allFields.length == 0) {
            return List.of();
        }

        List<ScreenField> editableFields = new ArrayList<>();

        for (ScreenField field : allFields) {
            if (field != null && !field.isBypassField()) {
                editableFields.add(field);
            }
        }

        return List.copyOf(editableFields);
    }

    /**
     * Extrae una región exacta de una fila.
     *
     * Las coordenadas son visibles y comienzan desde 1.
     */
    public String readRegion(
        int row,
        int column,
        int length
    ) {
        int rows = screen.getRows();
        int columns = screen.getColumns();

        if (row <= 0 || row > rows) {
            throw new IllegalArgumentException(
                "La fila debe estar entre 1 y "
                    + rows
                    + ". Valor recibido: "
                    + row
            );
        }

        if (column <= 0 || column > columns) {
            throw new IllegalArgumentException(
                "La columna debe estar entre 1 y "
                    + columns
                    + ". Valor recibido: "
                    + column
            );
        }

        if (length <= 0) {
            throw new IllegalArgumentException(
                "La longitud debe ser mayor que cero."
            );
        }

        int finalColumn = column + length - 1;

        if (finalColumn > columns) {
            throw new IllegalArgumentException(
                "La región supera el ancho de la pantalla. "
                    + "Columna inicial: "
                    + column
                    + ", longitud: "
                    + length
                    + ", columnas disponibles: "
                    + columns
            );
        }

        String line = readLine(row);

        int startIndex = column - 1;
        int endIndex = startIndex + length;

        if (startIndex >= line.length()) {
            return "";
        }

        endIndex = Math.min(
            endIndex,
            line.length()
        );

        return line.substring(
            startIndex,
            endIndex
        ).strip();
    }

        /**
         * Busca una etiqueta en la pantalla y devuelve el valor
         * que aparece después del carácter ":" en esa misma fila.
         */
        public String readAfterLabel(
            String label
        ) {
            if (label == null || label.isBlank()) {
                throw new IllegalArgumentException(
                    "La etiqueta de extracción no puede estar vacía."
                );
            }

            int rows = screen.getRows();

            for (int row = 1; row <= rows; row++) {
                String line = readLine(row);

                int labelIndex = line.indexOf(label);

                if (labelIndex < 0) {
                    continue;
                }

                int colonIndex = line.indexOf(
                    ':',
                    labelIndex + label.length()
                );

                if (colonIndex < 0) {
                    continue;
                }

                return line.substring(
                    colonIndex + 1
                ).strip();
            }

            throw new IllegalArgumentException(
                "No se encontró la etiqueta en la pantalla: "
                    + label
            );
        }
    /**
     * Obtiene un campo editable por índice.
     */
    public ScreenField getEditableField(int fieldIndex) {
        List<ScreenField> editableFields = getEditableFields();

        if (fieldIndex < 0 || fieldIndex >= editableFields.size()) {
            throw new IllegalArgumentException(
                "No existe el campo editable con índice "
                    + fieldIndex
                    + ". Campos disponibles: "
                    + editableFields.size()
            );
        }

        return editableFields.get(fieldIndex);
    }

    public boolean hasAtLeastEditableFields(int minimumFields) {
        return getEditableFields().size() >= minimumFields;
    }

    /**
     * Comprueba si el host permite enviar información.
     */
    public boolean isKeyboardUnlocked() {
        return screen.getOIA() != null
            && !screen.getOIA().isKeyBoardLocked();
    }

    /**
     * Imprime todos los campos editables sin mostrar su contenido.
     */
    public void printEditableFieldsMetadata() {
        List<ScreenField> editableFields = getEditableFields();

        System.out.println(
            "Campos editables detectados: " + editableFields.size()
        );

        for (int index = 0; index < editableFields.size(); index++) {
            ScreenField field = editableFields.get(index);

            /*
             * TN5250J usa posiciones desde cero.
             * Se suma uno para mostrarlas como aparecen en el emulador.
             */
            int visibleRow = field.startRow() + 1;
            int visibleColumn = field.startCol() + 1;

            System.out.printf(
                "  [%d] fila=%d, columna=%d, longitud=%d%n",
                index,
                visibleRow,
                visibleColumn,
                field.getLength()
            );
        }
    }

    /**
     * Imprime la pantalla respetando sus filas y columnas.
     */
    public void printScreen() {
        int rows = screen.getRows();
        int columns = screen.getColumns();
        char[] characters = screen.getScreenAsChars();

        if (characters == null) {
            System.out.println("[Pantalla sin contenido]");
            return;
        }

        System.out.println(
            "Dimensiones: "
                + rows
                + " filas x "
                + columns
                + " columnas"
        );

        System.out.println();

        for (int row = 0; row < rows; row++) {
            int start = row * columns;

            if (start >= characters.length) {
                break;
            }

            int length = Math.min(
                columns,
                characters.length - start
            );

            String line = new String(
                characters,
                start,
                length
            )
                .replace('\0', ' ')
                .stripTrailing();

            System.out.printf(
                "%02d | %s%n",
                row + 1,
                line
            );
        }
    }
}