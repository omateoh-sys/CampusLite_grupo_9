package campuslite.dominio;

/**
 * Evaluación de tipo proyecto.
 * Tiene una nota base más una bonificación opcional (máx 10 puntos extra),
 * pero el resultado no puede superar 100.
 * Demuestra: herencia, @Override, regla de negocio propia.
 */
public class Proyecto extends Evaluacion {

    private double notaBase;       // 0–100
    private double bonificacion;   // 0–10 puntos extra

    public Proyecto(String nombre, double ponderacion) {
        super(nombre, ponderacion);
        this.notaBase = 0;
        this.bonificacion = 0;
    }

    public Proyecto(String nombre, double ponderacion, double notaBase, double bonificacion) {
        super(nombre, ponderacion);
        setNotaBase(notaBase);
        setBonificacion(bonificacion);
    }

    public double getNotaBase() { return notaBase; }

    public void setNotaBase(double notaBase) {
        if (notaBase < 0 || notaBase > 100) {
            throw new IllegalArgumentException("La nota base debe estar entre 0 y 100.");
        }
        this.notaBase = notaBase;
    }

    public double getBonificacion() { return bonificacion; }

    public void setBonificacion(double bonificacion) {
        if (bonificacion < 0 || bonificacion > 10) {
            throw new IllegalArgumentException("La bonificación debe estar entre 0 y 10.");
        }
        this.bonificacion = bonificacion;
    }

    /**
     * Nota final = notaBase + bonificacion, máximo 100.
     */
    @Override
    public double calcularNota() {
        return Math.min(100, notaBase + bonificacion);
    }

    @Override
    public String toString() {
        return "Proyecto: " + getNombre()
                + " | Base: " + notaBase
                + " | Bonif: +" + bonificacion
                + " | Final: " + calcularNota()
                + " | Pond: " + getPonderacion() + "%";
    }
}

