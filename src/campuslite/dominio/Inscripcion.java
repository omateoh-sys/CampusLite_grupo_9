package campuslite.dominio;

import java.util.HashMap;
import java.util.Map;

/**
 * Relaciona un Estudiante con un Curso y guarda la nota obtenida
 * en cada Evaluacion de ese curso.
 *
 * Clave del mapa: nombre de la evaluación.
 * Valor: nota ingresada (0–100).
 */
public class Inscripcion {

    private Estudiante estudiante;
    private Curso curso;

    // Mapa evaluación → nota   (nombre evaluacion → nota 0-100)
    private Map<String, Double> notas;

    public Inscripcion(Estudiante estudiante, Curso curso) {
        if (estudiante == null) throw new IllegalArgumentException("Estudiante no puede ser nulo.");
        if (curso == null)      throw new IllegalArgumentException("Curso no puede ser nulo.");
        this.estudiante = estudiante;
        this.curso      = curso;
        this.notas      = new HashMap<>();
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public Estudiante getEstudiante() { return estudiante; }
    public Curso      getCurso()      { return curso; }

    // ── Manejo de notas ──────────────────────────────────────────────────────

    /**
     * Registra o actualiza la nota de una evaluación.
     * @param nombreEvaluacion debe coincidir con Evaluacion.getNombre()
     * @param nota             valor entre 0 y 100
     */
    public void registrarNota(String nombreEvaluacion, double nota) {
        if (nota < 0 || nota > 100) {
            throw new IllegalArgumentException("La nota debe estar entre 0 y 100.");
        }
        notas.put(nombreEvaluacion, nota);
    }

    /**
     * Retorna la nota de una evaluación, o -1 si no ha sido ingresada.
     */
    public double getNota(String nombreEvaluacion) {
        return notas.getOrDefault(nombreEvaluacion, -1.0);
    }

    public Map<String, Double> getTodasLasNotas() {
        return new HashMap<>(notas);
    }

    /**
     * Calcula el promedio ponderado usando las notas registradas.
     * Las evaluaciones sin nota se ignoran del cálculo (no cuentan como 0).
     * Retorna 0 si no hay notas.
     */
    public double calcularPromedio() {
        double totalPond  = 0;
        double totalNota  = 0;

        for (Evaluacion ev : curso.getEvaluaciones()) {
            double nota = getNota(ev.getNombre());
            if (nota >= 0) {
                totalNota += nota * ev.getPonderacion();
                totalPond += ev.getPonderacion();
            }
        }
        if (totalPond == 0) return 0;
        return totalNota / totalPond;
    }

    /**
     * Promedio considerando todas las evaluaciones del curso
     * (las sin nota cuentan como 0).
     */
    public double calcularPromedioFinal() {
        double total = 0;
        for (Evaluacion ev : curso.getEvaluaciones()) {
            double nota = getNota(ev.getNombre());
            if (nota < 0) nota = 0;
            total += nota * (ev.getPonderacion() / 100.0);
        }
        return total;
    }

    public boolean estaAprobado() {
        return calcularPromedioFinal() >= 61.0;
    }

    public boolean tieneTodasLasNotas() {
        for (Evaluacion ev : curso.getEvaluaciones()) {
            if (getNota(ev.getNombre()) < 0) return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return estudiante.getCarnet() + " en " + curso.getCodigo();
    }
}