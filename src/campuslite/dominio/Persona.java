package campuslite.dominio;

/**
 * Clase base abstracta que representa cualquier persona del sistema.
 * Demuestra: clase abstracta, encapsulamiento, método abstracto.
 *
 * Herencia:
 *   Persona (abstracta)
 *   └── Estudiante
 */
public abstract class Persona {

    private String nombreCompleto;
    private String correo;

    // ── Constructor ──────────────────────────────────────────────────────────
    public Persona(String nombreCompleto, String correo) {
        setNombreCompleto(nombreCompleto);
        setCorreo(correo);
    }

    // ── Getters y setters ────────────────────────────────────────────────────
    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        this.nombreCompleto = nombreCompleto.trim();
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        if (correo != null && !correo.trim().isEmpty()) {
            if (!correo.trim().contains("@")) {
                throw new IllegalArgumentException("El correo no tiene un formato válido.");
            }
        }
        this.correo = (correo == null) ? "" : correo.trim();
    }

    /**
     * Cada subclase define su identificador único.
     * Estudiante → carnet.
     */
    public abstract String getIdentificador();

    /**
     * Cada subclase define su rol en el sistema.
     */
    public abstract String getRol();

    @Override
    public String toString() {
        return "[" + getRol() + "] " + nombreCompleto + " (" + getIdentificador() + ")";
    }
}