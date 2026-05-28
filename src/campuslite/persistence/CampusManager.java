package campuslite.persistence;

import java.util.ArrayList;
import java.util.List;

import campuslite.domain.*;

/**
 * Gestor central con persistencia en archivos .txt.
 * Singleton compartido por todas las ventanas.
 *
 * Comportamiento al iniciar:
 *   - Si existen datos en disco  → los carga.
 *   - Si NO existen datos        → arranca vacío (sin datos de demo).
 *
 * Cada operación de escritura:
 *   1. Modifica la lista en memoria.
 *   2. Persiste TODO en los archivos .txt.
 *   3. Registra el cambio en datos/bitacora.txt.
 */
public class CampusManager {

    private static CampusManager instancia;

    public static CampusManager getInstancia() {
        if (instancia == null) instancia = new CampusManager();
        return instancia;
    }

    private CampusManager() {
        PersistenceTxt.DatosApp datos = PersistenceTxt.cargarTodo();
        if (!datos.estaVacio()) {
            maestros      = new ArrayList<>(datos.maestros);
            estudiantes   = new ArrayList<>(datos.estudiantes);
            cursos        = new ArrayList<>(datos.cursos);
            inscripciones = new ArrayList<>(datos.inscripciones);
            Logbook.sesionIniciada();
        } else {
            // Primera vez o archivos completamente vacíos → listas vacías
            maestros      = new ArrayList<>();
            estudiantes   = new ArrayList<>();
            cursos        = new ArrayList<>();
            inscripciones = new ArrayList<>();
            // Guarda archivos vacíos para que existan en disco
            guardar();
        }
    }

    /** Persiste el estado actual en disco. */
    private void guardar() {
        PersistenceTxt.guardarTodo(maestros, estudiantes, cursos, inscripciones);
    }

    private List<Student>  estudiantes;
    private List<Teacher>     maestros;
    private List<Course>       cursos;
    private List<Registration> inscripciones;

    // ══════════════════════════════════════════════════════════════════════
    // ESTUDIANTES
    // ══════════════════════════════════════════════════════════════════════

    public List<Student> getStudents() { return new ArrayList<>(estudiantes); }

    public void addStudent(Student e) {
        if (buscarEstudiantePorCarnet(e.getStudentId()) != null)
            throw new IllegalArgumentException(
                "Ya existe un estudiante con carnet: " + e.getStudentId());
        estudiantes.add(e);
        guardar();
        Logbook.estudianteAgregado(e.getStudentId(), e.getFullName());
    }

    public void actualizarEstudiante(String carnetOriginal, Student nuevo) {
        for (int i = 0; i < estudiantes.size(); i++) {
            if (estudiantes.get(i).getStudentId().equals(carnetOriginal)) {
                if (!carnetOriginal.equals(nuevo.getStudentId()) &&
                    buscarEstudiantePorCarnet(nuevo.getStudentId()) != null)
                    throw new IllegalArgumentException(
                        "El carnet " + nuevo.getStudentId() + " ya está en uso.");
                // Actualizar referencia en inscripciones existentes
                for (Registration ins : inscripciones)
                    if (ins.getEstudiante().getStudentId().equals(carnetOriginal))
                        ; // la referencia vive en Inscripcion; se reemplaza abajo
                estudiantes.set(i, nuevo);
                guardar();
                Logbook.estudianteActualizado(
                    carnetOriginal, nuevo.getStudentId(), nuevo.getFullName());
                return;
            }
        }
        throw new IllegalArgumentException("Estudiante no encontrado: " + carnetOriginal);
    }

    public boolean removeStudent(String carnet) {
        inscripciones.removeIf(ins -> ins.getEstudiante().getStudentId().equals(carnet));
        boolean result = estudiantes.removeIf(e -> e.getStudentId().equals(carnet));
        if (result) {
            guardar();
            Logbook.estudianteEliminado(carnet);
        }
        return result;
    }

    public Student buscarEstudiantePorCarnet(String carnet) {
        for (Student e : estudiantes)
            if (e.getStudentId().equals(carnet)) return e;
        return null;
    }

    // ══════════════════════════════════════════════════════════════════════
    // MAESTROS
    // ══════════════════════════════════════════════════════════════════════

    public List<Teacher> getTeachers() { return new ArrayList<>(maestros); }

    public void agregarMaestro(Teacher m) {
        if (buscarMaestroPorCodigo(m.getEmployeeCode()) != null)
            throw new IllegalArgumentException(
                "Ya existe un maestro con código: " + m.getEmployeeCode());
        maestros.add(m);
        guardar();
        Logbook.maestroAgregado(m.getEmployeeCode(), m.getFullName());
    }

    public void actualizarMaestro(String codigoOriginal, Teacher nuevo) {
        for (int i = 0; i < maestros.size(); i++) {
            if (maestros.get(i).getEmployeeCode().equals(codigoOriginal)) {
                if (!codigoOriginal.equals(nuevo.getEmployeeCode()) &&
                    buscarMaestroPorCodigo(nuevo.getEmployeeCode()) != null)
                    throw new IllegalArgumentException(
                        "El código " + nuevo.getEmployeeCode() + " ya está en uso.");
                for (Course c : cursos)
                    if (c.getMaestro() != null &&
                        c.getMaestro().getEmployeeCode().equals(codigoOriginal))
                        c.setMaestro(nuevo);
                maestros.set(i, nuevo);
                guardar();
                Logbook.maestroActualizado(
                    codigoOriginal, nuevo.getEmployeeCode(), nuevo.getFullName());
                return;
            }
        }
        throw new IllegalArgumentException("Maestro no encontrado: " + codigoOriginal);
    }

    public boolean eliminarMaestro(String codigo) {
        for (Course c : cursos)
            if (c.getMaestro() != null &&
                c.getMaestro().getEmployeeCode().equals(codigo))
                c.setMaestro(null);
        boolean result = maestros.removeIf(m -> m.getEmployeeCode().equals(codigo));
        if (result) {
            guardar();
            Logbook.maestroEliminado(codigo);
        }
        return result;
    }

    public Teacher buscarMaestroPorCodigo(String codigo) {
        for (Teacher m : maestros)
            if (m.getEmployeeCode().equals(codigo)) return m;
        return null;
    }

    // ══════════════════════════════════════════════════════════════════════
    // CURSOS
    // ══════════════════════════════════════════════════════════════════════

    public List<Course> getCourses() { return new ArrayList<>(cursos); }

    public void agregarCurso(Course c) {
        if (buscarCursoPorCodigo(c.getCode()) != null)
            throw new IllegalArgumentException(
                "Ya existe un curso con código: " + c.getCode());
        cursos.add(c);
        guardar();
        Logbook.cursoAgregado(c.getCode(), c.getName());
    }

    public void actualizarCurso(String codigoOriginal, Course nuevo) {
        for (int i = 0; i < cursos.size(); i++) {
            if (cursos.get(i).getCode().equals(codigoOriginal)) {
                cursos.set(i, nuevo);
                guardar();
                Logbook.cursoActualizado(codigoOriginal, nuevo.getName());
                return;
            }
        }
        throw new IllegalArgumentException("Curso no encontrado: " + codigoOriginal);
    }

    public boolean eliminarCurso(String codigo) {
        inscripciones.removeIf(ins -> ins.getCurso().getCode().equals(codigo));
        boolean result = cursos.removeIf(c -> c.getCode().equals(codigo));
        if (result) {
            guardar();
            Logbook.cursoEliminado(codigo);
        }
        return result;
    }

    public Course buscarCursoPorCodigo(String codigo) {
        for (Course c : cursos)
            if (c.getCode().equals(codigo)) return c;
        return null;
    }

    // ── Evaluaciones ──────────────────────────────────────────────────────────

    /**
     * Agrega una evaluación a un curso y persiste el cambio.
     * Llama a este método desde VentanaCursos en lugar de
     * curso.agregarEvaluacion() directo, para que quede en bitácora.
     */
    public void agregarEvaluacion(String codigoCurso, Evaluation ev) {
        Course curso = buscarCursoPorCodigo(codigoCurso);
        if (curso == null)
            throw new IllegalArgumentException("Curso no encontrado: " + codigoCurso);
        curso.addEvaluation(ev);          // valida suma ≤ 100%
        guardar();
        String tipo = ev instanceof Laboratory ? "Laboratorio"
                    : ev instanceof Project    ? "Proyecto"
                    : "ExamenEscrito";
        Logbook.evaluacionAgregada(codigoCurso, ev.getNombre(), tipo, ev.getPonderacion());
    }

    /**
     * Elimina una evaluación de un curso y persiste.
     */
    public void eliminarEvaluacion(String codigoCurso, Evaluation ev) {
        Course curso = buscarCursoPorCodigo(codigoCurso);
        if (curso == null)
            throw new IllegalArgumentException("Curso no encontrado: " + codigoCurso);
        String nombre = ev.getNombre();
        curso.removeEvaluation(ev);
        guardar();
        Logbook.evaluacionEliminada(codigoCurso, nombre);
    }

    /**
     * Edita la ponderación de una evaluación existente y persiste.
     */
    public void editarPonderacionEvaluacion(String codigoCurso,
                                             String nombreEval,
                                             double nuevaPonderacion) {
        Course curso = buscarCursoPorCodigo(codigoCurso);
        if (curso == null)
            throw new IllegalArgumentException("Curso no encontrado: " + codigoCurso);
        // Buscar ponderación actual para la bitácora
        double antes = -1;
        for (Evaluation e : curso.getEvaluations())
            if (e.getNombre().equals(nombreEval)) { antes = e.getPonderacion(); break; }
        curso.editarPonderacion(nombreEval, nuevaPonderacion);
        guardar();
        Logbook.evaluacionActualizada(codigoCurso, nombreEval,
            "ponderacion", antes + "%", nuevaPonderacion + "%");
    }

    /**
     * Renombra una evaluación y persiste.
     */
    public void renombrarEvaluacion(String codigoCurso,
                                     String nombreActual,
                                     String nombreNuevo) {
        Course curso = buscarCursoPorCodigo(codigoCurso);
        if (curso == null)
            throw new IllegalArgumentException("Curso no encontrado: " + codigoCurso);
        curso.renombrarEvaluacion(nombreActual, nombreNuevo);
        guardar();
        Logbook.evaluacionActualizada(codigoCurso, nombreNuevo,
            "nombre", nombreActual, nombreNuevo);
    }

    // ══════════════════════════════════════════════════════════════════════
    // INSCRIPCIONES Y NOTAS
    // ══════════════════════════════════════════════════════════════════════

    public List<Registration> getInscripciones() { return new ArrayList<>(inscripciones); }

    public Registration inscribir(String carnet, String codigoCurso) {
        Student est = buscarEstudiantePorCarnet(carnet);
        Course cur = buscarCursoPorCodigo(codigoCurso);
        if (est == null)
            throw new IllegalArgumentException("Estudiante no encontrado: " + carnet);
        if (cur == null)
            throw new IllegalArgumentException("Curso no encontrado: " + codigoCurso);
        if (buscarInscripcion(carnet, codigoCurso) != null)
            throw new IllegalArgumentException(
                est.getFullName() + " ya está inscrito en " + cur.getName());
        Registration ins = new Registration(est, cur);
        inscripciones.add(ins);
        guardar();
        Logbook.estudianteInscrito(carnet, codigoCurso);
        return ins;
    }

    /**
     * Registra una nota individual en una inscripción y persiste.
     * Úsalo desde VentanaNotas para que quede en bitácora.
     */
    public void registerGrade(String carnet, String codigoCurso,
                               String nombreEval, double nota) {
        Registration ins = buscarInscripcion(carnet, codigoCurso);
        if (ins == null)
            throw new IllegalArgumentException(
                "Inscripción no encontrada: " + carnet + " / " + codigoCurso);
        ins.registerGrade(nombreEval, nota);
        guardar();
        Logbook.notaRegistrada(carnet, codigoCurso, nombreEval, nota);
    }

    public Registration buscarInscripcion(String carnet, String codigoCurso) {
        for (Registration ins : inscripciones)
            if (ins.getEstudiante().getStudentId().equals(carnet) &&
                ins.getCurso().getCode().equals(codigoCurso)) return ins;
        return null;
    }

    public List<Registration> getInscripcionesPorCurso(String codigoCurso) {
        List<Registration> res = new ArrayList<>();
        for (Registration ins : inscripciones)
            if (ins.getCurso().getCode().equals(codigoCurso)) res.add(ins);
        return res;
    }

    public List<Registration> getInscripcionesPorEstudiante(String carnet) {
        List<Registration> res = new ArrayList<>();
        for (Registration ins : inscripciones)
            if (ins.getEstudiante().getStudentId().equals(carnet)) res.add(ins);
        return res;
    }
}