package campuslite.dominio;

/**
 * Clase abstracta que representa una evaluación dentro de un curso.
 * Demuestra: clase abstracta, encapsulamiento, método abstracto.
 *
 * Cada subclase define su propia lógica para calcular la contribución
 * al promedio final del curso.
 */
public abstract class Evaluacion {

    private String nombre;
    private double ponderacion; // Porcentaje del curso, ej. 40.0 para 40%

    // ── Constructor ──────────────────────────────────────────────────────────
    public Evaluacion(String nombre, double ponderacion) {
        setNombre(nombre);
        setPonderacion(ponderacion);
    }

    // ── Getters y setters ────────────────────────────────────────────────────
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la evaluación no puede estar vacío.");
        }
        this.nombre = nombre.trim();
    }

    public double getPonderacion() {
        return ponderacion;
    }

    public void setPonderacion(double ponderacion) {
        if (ponderacion <= 0 || ponderacion > 100) {
            throw new IllegalArgumentException("La ponderación debe estar entre 1 y 100.");
        }
        this.ponderacion = ponderacion;
    }

    /**
     * Calcula la nota obtenida en esta evaluación (0–100).
     * Cada subclase implementa su propia regla de calificación.
     */
    public abstract double calcularNota();

    /**
     * Calcula cuánto aporta esta evaluación al promedio final.
     * Usa polimorfismo: llama a calcularNota() del objeto real.
     */
    public double calcularContribucion() {
        return (calcularNota() * ponderacion) / 100.0;
    }

    @Override
    public String toString() {
        return nombre + " (" + ponderacion + "%)";
    }
}