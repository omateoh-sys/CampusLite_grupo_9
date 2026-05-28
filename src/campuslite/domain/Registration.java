package campuslite.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * Relaciona un Estudiante con un Curso y guarda la nota obtenida
 * en cada Evaluacion de ese curso.
 *
 * Clave del mapa: nombre de la evaluación.
 * Valor: nota ingresada (0–100).
 */
public class Registration {

    private Student estudiante;
    private Course curso;

    // Mapa evaluación → nota   (nombre evaluacion → nota 0-100)
    private Map<String, Double> notas;

    public Registration(Student estudiante, Course curso) {
        if (estudiante == null) throw new IllegalArgumentException("Estudiante no puede ser nulo.");
        if (curso == null)      throw new IllegalArgumentException("Curso no puede ser nulo.");
        this.estudiante = estudiante;
        this.curso      = curso;
        this.notas      = new HashMap<>();
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public Student getEstudiante() { return estudiante; }
    public Course      getCurso()      { return curso; }

    // ── Manejo de notas ──────────────────────────────────────────────────────

    /**
     * Registra o actualiza la nota de una evaluación.
     * @param nombreEvaluacion debe coincidir con Evaluacion.getNombre()
     * @param nota             valor entre 0 y 100
     */
    public void registerGrade(String nombreEvaluacion, double nota) {
        if (nota < 0 || nota > 100) {
            throw new IllegalArgumentException("La nota debe estar entre 0 y 100.");
        }
        notas.put(nombreEvaluacion, nota);
    }

    /**
     * Retorna la nota de una evaluación, o -1 si no ha sido ingresada.
     */
    public double getGrade(String nombreEvaluacion) {
        return notas.getOrDefault(nombreEvaluacion, -1.0);
    }

    public Map<String, Double> getTodasLasNotas() {
        return new HashMap<>(notas);
    }

    /**
     * Promedio considerando todas las evaluaciones del curso
     * (las sin nota cuentan como 0).
     */
    public double calcularPromedioFinal() {
        double total = 0;
        for (Evaluation ev : curso.getEvaluations()) {
            double nota = getGrade(ev.getNombre());
            if (nota < 0) nota = 0;
            total += nota * (ev.getPonderacion() / 100.0);
        }
        return total;
    }

    public boolean isPassing() {
        return calcularPromedioFinal() >= 61.0;
    }

    public boolean tieneTodasLasNotas() {
        for (Evaluation ev : curso.getEvaluations()) {
            if (getGrade(ev.getNombre()) < 0) return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return estudiante.getStudentId() + " en " + curso.getCode();
    }
}