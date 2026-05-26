package campuslite.domain;

/**
 * Representa un maestro/docente del sistema.
 * Demuestra: herencia de Persona, @Override de métodos abstractos.
 *
 * Jerarquía completa:
 *   Persona (abstracta)
 *   ├── Estudiante
 *   └── Maestro
 */
public class Teacher extends Person {

    private String codigoEmpleado;
    private String especialidad;

    // Constructor completo (sobrecarga 1)
    public Teacher(String codigoEmpleado, String nombreCompleto,
                   String correo, String especialidad) {
        super(nombreCompleto, correo);
        setCodigoEmpleado(codigoEmpleado);
        setEspecialidad(especialidad);
    }

    // Constructor sin especialidad (sobrecarga 2)
    public Teacher(String codigoEmpleado, String nombreCompleto, String correo) {
        this(codigoEmpleado, nombreCompleto, correo, "");
    }

    public String getEmployeeCode() { return codigoEmpleado; }

    public void setCodigoEmpleado(String codigo) {
        if (codigo == null || codigo.trim().isEmpty())
            throw new IllegalArgumentException("El código de empleado no puede estar vacío.");
        this.codigoEmpleado = codigo.trim();
    }

    public String getSpecialty() { return especialidad; }

    public void setEspecialidad(String especialidad) {
        this.especialidad = (especialidad == null) ? "" : especialidad.trim();
    }

    @Override public String getIdentificador() { return codigoEmpleado; }
    @Override public String getRol()           { return "Maestro"; }

    @Override
    public String toString() {
        return codigoEmpleado + " — " + getFullName()
            + (especialidad.isEmpty() ? "" : " [" + especialidad + "]");
    }

	public Object getCarnet() {
		// TODO Auto-generated method stub
		return null;
	}
}