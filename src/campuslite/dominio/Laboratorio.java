package campuslite.dominio;

import java.util.ArrayList;
import java.util.List;

/**
 * Evaluación de tipo laboratorio.
 * La nota final es el promedio de varias prácticas individuales.
 * Demuestra: herencia, @Override, uso de colecciones en el dominio.
 */
public class Laboratorio extends Evaluacion {

    private List<Double> practicas; // Notas de cada práctica (0–100)

    public Laboratorio(String nombre, double ponderacion) {
        super(nombre, ponderacion);
        this.practicas = new ArrayList<>();
    }

    /**
     * Agrega la nota de una práctica individual.
     */
    public void agregarPractica(double nota) {
        if (nota < 0 || nota > 100) {
            throw new IllegalArgumentException("La nota de práctica debe estar entre 0 y 100.");
        }
        practicas.add(nota);
    }

    public List<Double> getPracticas() {
        return new ArrayList<>(practicas); // copia defensiva
    }

    public void limpiarPracticas() {
        practicas.clear();
    }

    /**
     * La nota del laboratorio es el promedio de todas las prácticas.
     * Si no hay prácticas registradas, retorna 0.
     */
    @Override
    public double calcularNota() {
        if (practicas.isEmpty()) return 0;
        double suma = 0;
        for (double p : practicas) {
            suma += p;
        }
        return suma / practicas.size();
    }

    @Override
    public String toString() {
        return "Laboratorio: " + getNombre()
                + " | Prácticas: " + practicas.size()
                + " | Promedio: " + String.format("%.1f", calcularNota())
                + " | Pond: " + getPonderacion() + "%";
    }
}
