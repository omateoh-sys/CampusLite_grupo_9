package campuslite.domain;

/**
 * Clase base abstracta que representa cualquier persona del sistema.
 * Demuestra: clase abstracta, encapsulamiento, método abstracto.
 *
 * Herencia:
 *   Persona (abstracta)
 *   └── Estudiante
 */
public abstract class Person {

    private String nombreCompleto;
    private String correo;

    // ── Constructor ──────────────────────────────────────────────────────────
    public Person(String nombreCompleto, String correo) {
        setFullName(nombreCompleto);
        setEmail(correo);
    }

    // ── Getters y setters ────────────────────────────────────────────────────
    public String getFullName() {
        return nombreCompleto;
    }

    public void setFullName(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        this.nombreCompleto = nombreCompleto.trim();
    }

    public String getEmail() {
        return correo;
    }

    public void setEmail(String correo) {
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