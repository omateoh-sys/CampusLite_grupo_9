package campuslite.domain;
/**
 * Evaluación de tipo examen escrito.
 * La nota es directa: el estudiante obtiene una nota sobre 100.
 * Demuestra: herencia, @Override.
 */
public class WritteExam extends Evaluation {

    private double nota; // 0–100

    public WritteExam(String nombre, double ponderacion) {
        super(nombre, ponderacion);
        this.nota = 0;
    }

    public WritteExam(String nombre, double ponderacion, double nota) {
        super(nombre, ponderacion);
        setNota(nota);
    }

    public double getNota() {
        return nota;
    }

    public void setNota(double nota) {
        if (nota < 0 || nota > 100) {
            throw new IllegalArgumentException("La nota del examen debe estar entre 0 y 100.");
        }
        this.nota = nota;
    }

    /**
     * Para un examen escrito la nota obtenida es directamente la nota ingresada.
     */
    @Override
    public double calcularNota() {
        return nota;
    }

    @Override
    public String toString() {
        return "Examen: " + getNombre() + " | Nota: " + nota + " | Pond: " + getPonderacion() + "%";
    }
}