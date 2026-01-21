package com.examplo.pagoamigos;

/**
 * Estado posible para productos/solicitudes.
 * Provee código entero y etiqueta legible.
 */
public enum Estatus_Products {
    PENDIENTE(1, "PENDIENTE"),
    APROBADO(2, "APROBADO");

    private final int code;
    private final String label;

    Estatus_Products(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static Estatus_Products fromCode(int code) {
        for (Estatus_Products e : values()) {
            if (e.code == code) return e;
        }
        throw new IllegalArgumentException("Código de estatus inválido: " + code);
    }

    @Override
    public String toString() {
        return label;
    }
}
