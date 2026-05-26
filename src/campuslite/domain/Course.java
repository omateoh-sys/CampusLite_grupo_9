package campuslite.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un curso académico.
 * Demuestra: encapsulamiento, polimorfismo (List<Evaluacion>), sobrecarga.
 */
public class Course {

    private String codigo;
    private String nombre;
    private int    creditos;
    private int    cupoMaximo;
    private Teacher maestro;  // puede ser null si no tiene asignado

    private List<Evaluation> evaluaciones;

    // ── Constructores (sobrecarga) ───────────────────────────────────────────
    public Course(String codigo, String nombre, int creditos, int cupoMaximo) {
        setCodigo(codigo); setNombre(nombre);
        setCreditos(creditos); setCupoMaximo(cupoMaximo);
        this.evaluaciones = new ArrayList<>();
    }

    public Course(String codigo, String nombre) {
        this(codigo, nombre, 0, 0);
    }

    // ── Maestro ──────────────────────────────────────────────────────────────
    public Teacher getMaestro() { return maestro; }

    public void setMaestro(Teacher maestroSel) {
        this.maestro = maestroSel; // null = sin asignar, es válido
    }

    // ── Gestión de evaluaciones ──────────────────────────────────────────────

    public void addEvaluation(Evaluation evaluacion) {
        if (evaluacion == null)
            throw new IllegalArgumentException("La evaluación no puede ser nula.");
        if (sumaPonderaciones() + evaluacion.getPonderacion() > 100.0 + 1e-9)
            throw new IllegalArgumentException(
                "Agregar esta evaluación superaría el 100% de ponderación. "
                + "Suma actual: " + sumaPonderaciones() + "%");
        evaluaciones.add(evaluacion);
    }

    public boolean removeEvaluation(Evaluation evaluacion) {
        return evaluaciones.remove(evaluacion);
    }

    public List<Evaluation> getEvaluations() {
        return new ArrayList<>(evaluaciones); // copia defensiva
    }

    /**
     * Edita la ponderación de una evaluación existente.
     * Valida que la nueva suma no supere 100%.
     */
    public void editarPonderacion(String nombreEvaluacion, double nuevaPonderacion) {
        Evaluation objetivo = null;
        for (Evaluation ev : evaluaciones)
            if (ev.getNombre().equals(nombreEvaluacion)) { objetivo = ev; break; }
        if (objetivo == null)
            throw new IllegalArgumentException("Evaluación no encontrada: " + nombreEvaluacion);
        if (nuevaPonderacion <= 0 || nuevaPonderacion > 100)
            throw new IllegalArgumentException("La ponderación debe estar entre 1 y 100.");
        double sumaActual = sumaPonderaciones() - objetivo.getPonderacion();
        if (sumaActual + nuevaPonderacion > 100.0 + 1e-9)
            throw new IllegalArgumentException(
                "Con ese valor la suma total sería " + (sumaActual + nuevaPonderacion)
                + "%. Máximo permitido: 100%.");
        objetivo.setPonderacion(nuevaPonderacion);
    }

    /**
     * Renombra una evaluación existente.
     */
    public void renombrarEvaluacion(String nombreActual, String nombreNuevo) {
        if (nombreNuevo == null || nombreNuevo.trim().isEmpty())
            throw new IllegalArgumentException("El nuevo nombre no puede estar vacío.");
        for (Evaluation ev : evaluaciones) {
            if (ev.getNombre().equals(nombreActual)) {
                ev.setNombre(nombreNuevo.trim());
                return;
            }
        }
        throw new IllegalArgumentException("Evaluación no encontrada: " + nombreActual);
    }

    // ── Cálculos ─────────────────────────────────────────────────────────────

    public double sumaPonderaciones() {
        double suma = 0;
        for (Evaluation e : evaluaciones) suma += e.getPonderacion();
        return suma;
    }

    public boolean ponderacionesCompletas() {
        return Math.abs(sumaPonderaciones() - 100.0) < 1e-9;
    }

    /** Calcula el promedio ponderado usando polimorfismo. */
    public double calcularPromedioPonderado() {
        double total = 0;
        for (Evaluation e : evaluaciones)
            total += e.calcularContribucion(); // polimorfismo
        return total;
    }

    public static boolean esAprobado(double promedio) {
        return promedio >= 61.0;
    }

    // ── Getters y setters ────────────────────────────────────────────────────

    public String getCode() { return codigo; }

    public void setCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty())
            throw new IllegalArgumentException("El código del curso no puede estar vacío.");
        this.codigo = codigo.trim();
    }

    public String getName() { return nombre; }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty())
            throw new IllegalArgumentException("El nombre del curso no puede estar vacío.");
        this.nombre = nombre.trim();
    }

    public int getCredits() { return creditos; }

    public void setCreditos(int creditos) {
        if (creditos < 0)
            throw new IllegalArgumentException("Los créditos no pueden ser negativos.");
        this.creditos = creditos;
    }

    public int getMaxCapacity() { return cupoMaximo; }

    public void setCupoMaximo(int cupoMaximo) {
        if (cupoMaximo < 0)
            throw new IllegalArgumentException("El cupo no puede ser negativo.");
        this.cupoMaximo = cupoMaximo;
    }

    @Override
    public String toString() {
        return codigo + " — " + nombre;
    }
}