package campuslite.dominio;

/**
 * Representa un estudiante del sistema.
 * Demuestra: herencia de Persona, encapsulamiento, sobrecarga de constructores,
 *            @Override de métodos abstractos.
 */
public class Estudiante extends Persona {

    private String carnet;

    // ── Constructor completo (sobrecarga 1) ──────────────────────────────────
    public Estudiante(String carnet, String nombreCompleto, String correo) {
        super(nombreCompleto, correo);
        setCarnet(carnet);
    }

    // ── Constructor sin correo (sobrecarga 2) ────────────────────────────────
    public Estudiante(String carnet, String nombreCompleto) {
        this(carnet, nombreCompleto, "");
    }

    // ── Getter/setter del carnet ─────────────────────────────────────────────
    public String getCarnet() {
        return carnet;
    }

    public void setCarnet(String carnet) {
        if (carnet == null || carnet.trim().isEmpty()) {
            throw new IllegalArgumentException("El carnet no puede estar vacío.");
        }
        if (!carnet.trim().matches("\\d{8}")) {
            throw new IllegalArgumentException("El carnet debe tener exactamente 8 dígitos.");
        }
        this.carnet = carnet.trim();
    }

    // ── Implementación de métodos abstractos de Persona ──────────────────────
    @Override
    public String getIdentificador() {
        return carnet;
    }

    @Override
    public String getRol() {
        return "Estudiante";
    }

    @Override
    public String toString() {
        return carnet + " — " + getNombreCompleto();
    }
}