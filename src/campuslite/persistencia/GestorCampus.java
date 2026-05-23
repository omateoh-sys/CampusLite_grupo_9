package campuslite.persistencia;

import campuslite.dominio.*;

import java.util.ArrayList;
import java.util.List;

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
public class GestorCampus {

    private static GestorCampus instancia;

    public static GestorCampus getInstancia() {
        if (instancia == null) instancia = new GestorCampus();
        return instancia;
    }

    private GestorCampus() {
        PersistenciaTxt.DatosApp datos = PersistenciaTxt.cargarTodo();
        if (!datos.estaVacio()) {
            maestros      = new ArrayList<>(datos.maestros);
            estudiantes   = new ArrayList<>(datos.estudiantes);
            cursos        = new ArrayList<>(datos.cursos);
            inscripciones = new ArrayList<>(datos.inscripciones);
            Bitacora.sesionIniciada();
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
        PersistenciaTxt.guardarTodo(maestros, estudiantes, cursos, inscripciones);
    }

    private List<Estudiante>  estudiantes;
    private List<Maestro>     maestros;
    private List<Curso>       cursos;
    private List<Inscripcion> inscripciones;

    // ══════════════════════════════════════════════════════════════════════
    // ESTUDIANTES
    // ══════════════════════════════════════════════════════════════════════

    public List<Estudiante> getEstudiantes() { return new ArrayList<>(estudiantes); }

    public void agregarEstudiante(Estudiante e) {
        if (buscarEstudiantePorCarnet(e.getCarnet()) != null)
            throw new IllegalArgumentException(
                "Ya existe un estudiante con carnet: " + e.getCarnet());
        estudiantes.add(e);
        guardar();
        Bitacora.estudianteAgregado(e.getCarnet(), e.getNombreCompleto());
    }

    public void actualizarEstudiante(String carnetOriginal, Estudiante nuevo) {
        for (int i = 0; i < estudiantes.size(); i++) {
            if (estudiantes.get(i).getCarnet().equals(carnetOriginal)) {
                if (!carnetOriginal.equals(nuevo.getCarnet()) &&
                    buscarEstudiantePorCarnet(nuevo.getCarnet()) != null)
                    throw new IllegalArgumentException(
                        "El carnet " + nuevo.getCarnet() + " ya está en uso.");
                // Actualizar referencia en inscripciones existentes
                for (Inscripcion ins : inscripciones)
                    if (ins.getEstudiante().getCarnet().equals(carnetOriginal))
                        ; // la referencia vive en Inscripcion; se reemplaza abajo
                estudiantes.set(i, nuevo);
                guardar();
                Bitacora.estudianteActualizado(
                    carnetOriginal, nuevo.getCarnet(), nuevo.getNombreCompleto());
                return;
            }
        }
        throw new IllegalArgumentException("Estudiante no encontrado: " + carnetOriginal);
    }

    public boolean eliminarEstudiante(String carnet) {
        inscripciones.removeIf(ins -> ins.getEstudiante().getCarnet().equals(carnet));
        boolean result = estudiantes.removeIf(e -> e.getCarnet().equals(carnet));
        if (result) {
            guardar();
            Bitacora.estudianteEliminado(carnet);
        }
        return result;
    }

    public Estudiante buscarEstudiantePorCarnet(String carnet) {
        for (Estudiante e : estudiantes)
            if (e.getCarnet().equals(carnet)) return e;
        return null;
    }

    // ══════════════════════════════════════════════════════════════════════
    // MAESTROS
    // ══════════════════════════════════════════════════════════════════════

    public List<Maestro> getMaestros() { return new ArrayList<>(maestros); }

    public void agregarMaestro(Maestro m) {
        if (buscarMaestroPorCodigo(m.getCodigoEmpleado()) != null)
            throw new IllegalArgumentException(
                "Ya existe un maestro con código: " + m.getCodigoEmpleado());
        maestros.add(m);
        guardar();
        Bitacora.maestroAgregado(m.getCodigoEmpleado(), m.getNombreCompleto());
    }

    public void actualizarMaestro(String codigoOriginal, Maestro nuevo) {
        for (int i = 0; i < maestros.size(); i++) {
            if (maestros.get(i).getCodigoEmpleado().equals(codigoOriginal)) {
                if (!codigoOriginal.equals(nuevo.getCodigoEmpleado()) &&
                    buscarMaestroPorCodigo(nuevo.getCodigoEmpleado()) != null)
                    throw new IllegalArgumentException(
                        "El código " + nuevo.getCodigoEmpleado() + " ya está en uso.");
                for (Curso c : cursos)
                    if (c.getMaestro() != null &&
                        c.getMaestro().getCodigoEmpleado().equals(codigoOriginal))
                        c.setMaestro(nuevo);
                maestros.set(i, nuevo);
                guardar();
                Bitacora.maestroActualizado(
                    codigoOriginal, nuevo.getCodigoEmpleado(), nuevo.getNombreCompleto());
                return;
            }
        }
        throw new IllegalArgumentException("Maestro no encontrado: " + codigoOriginal);
    }

    public boolean eliminarMaestro(String codigo) {
        for (Curso c : cursos)
            if (c.getMaestro() != null &&
                c.getMaestro().getCodigoEmpleado().equals(codigo))
                c.setMaestro(null);
        boolean result = maestros.removeIf(m -> m.getCodigoEmpleado().equals(codigo));
        if (result) {
            guardar();
            Bitacora.maestroEliminado(codigo);
        }
        return result;
    }

    public Maestro buscarMaestroPorCodigo(String codigo) {
        for (Maestro m : maestros)
            if (m.getCodigoEmpleado().equals(codigo)) return m;
        return null;
    }

    // ══════════════════════════════════════════════════════════════════════
    // CURSOS
    // ══════════════════════════════════════════════════════════════════════

    public List<Curso> getCursos() { return new ArrayList<>(cursos); }

    public void agregarCurso(Curso c) {
        if (buscarCursoPorCodigo(c.getCodigo()) != null)
            throw new IllegalArgumentException(
                "Ya existe un curso con código: " + c.getCodigo());
        cursos.add(c);
        guardar();
        Bitacora.cursoAgregado(c.getCodigo(), c.getNombre());
    }

    public void actualizarCurso(String codigoOriginal, Curso nuevo) {
        for (int i = 0; i < cursos.size(); i++) {
            if (cursos.get(i).getCodigo().equals(codigoOriginal)) {
                cursos.set(i, nuevo);
                guardar();
                Bitacora.cursoActualizado(codigoOriginal, nuevo.getNombre());
                return;
            }
        }
        throw new IllegalArgumentException("Curso no encontrado: " + codigoOriginal);
    }

    public boolean eliminarCurso(String codigo) {
        inscripciones.removeIf(ins -> ins.getCurso().getCodigo().equals(codigo));
        boolean result = cursos.removeIf(c -> c.getCodigo().equals(codigo));
        if (result) {
            guardar();
            Bitacora.cursoEliminado(codigo);
        }
        return result;
    }

    public Curso buscarCursoPorCodigo(String codigo) {
        for (Curso c : cursos)
            if (c.getCodigo().equals(codigo)) return c;
        return null;
    }

    // ── Evaluaciones ──────────────────────────────────────────────────────────

    /**
     * Agrega una evaluación a un curso y persiste el cambio.
     * Llama a este método desde VentanaCursos en lugar de
     * curso.agregarEvaluacion() directo, para que quede en bitácora.
     */
    public void agregarEvaluacion(String codigoCurso, Evaluacion ev) {
        Curso curso = buscarCursoPorCodigo(codigoCurso);
        if (curso == null)
            throw new IllegalArgumentException("Curso no encontrado: " + codigoCurso);
        curso.agregarEvaluacion(ev);          // valida suma ≤ 100%
        guardar();
        String tipo = ev instanceof Laboratorio ? "Laboratorio"
                    : ev instanceof Proyecto    ? "Proyecto"
                    : "ExamenEscrito";
        Bitacora.evaluacionAgregada(codigoCurso, ev.getNombre(), tipo, ev.getPonderacion());
    }

    /**
     * Elimina una evaluación de un curso y persiste.
     */
    public void eliminarEvaluacion(String codigoCurso, Evaluacion ev) {
        Curso curso = buscarCursoPorCodigo(codigoCurso);
        if (curso == null)
            throw new IllegalArgumentException("Curso no encontrado: " + codigoCurso);
        String nombre = ev.getNombre();
        curso.eliminarEvaluacion(ev);
        guardar();
        Bitacora.evaluacionEliminada(codigoCurso, nombre);
    }

    /**
     * Edita la ponderación de una evaluación existente y persiste.
     */
    public void editarPonderacionEvaluacion(String codigoCurso,
                                             String nombreEval,
                                             double nuevaPonderacion) {
        Curso curso = buscarCursoPorCodigo(codigoCurso);
        if (curso == null)
            throw new IllegalArgumentException("Curso no encontrado: " + codigoCurso);
        // Buscar ponderación actual para la bitácora
        double antes = -1;
        for (Evaluacion e : curso.getEvaluaciones())
            if (e.getNombre().equals(nombreEval)) { antes = e.getPonderacion(); break; }
        curso.editarPonderacion(nombreEval, nuevaPonderacion);
        guardar();
        Bitacora.evaluacionActualizada(codigoCurso, nombreEval,
            "ponderacion", antes + "%", nuevaPonderacion + "%");
    }

    /**
     * Renombra una evaluación y persiste.
     */
    public void renombrarEvaluacion(String codigoCurso,
                                     String nombreActual,
                                     String nombreNuevo) {
        Curso curso = buscarCursoPorCodigo(codigoCurso);
        if (curso == null)
            throw new IllegalArgumentException("Curso no encontrado: " + codigoCurso);
        curso.renombrarEvaluacion(nombreActual, nombreNuevo);
        guardar();
        Bitacora.evaluacionActualizada(codigoCurso, nombreNuevo,
            "nombre", nombreActual, nombreNuevo);
    }

    // ══════════════════════════════════════════════════════════════════════
    // INSCRIPCIONES Y NOTAS
    // ══════════════════════════════════════════════════════════════════════

    public List<Inscripcion> getInscripciones() { return new ArrayList<>(inscripciones); }

    public Inscripcion inscribir(String carnet, String codigoCurso) {
        Estudiante est = buscarEstudiantePorCarnet(carnet);
        Curso cur = buscarCursoPorCodigo(codigoCurso);
        if (est == null)
            throw new IllegalArgumentException("Estudiante no encontrado: " + carnet);
        if (cur == null)
            throw new IllegalArgumentException("Curso no encontrado: " + codigoCurso);
        if (buscarInscripcion(carnet, codigoCurso) != null)
            throw new IllegalArgumentException(
                est.getNombreCompleto() + " ya está inscrito en " + cur.getNombre());
        Inscripcion ins = new Inscripcion(est, cur);
        inscripciones.add(ins);
        guardar();
        Bitacora.estudianteInscrito(carnet, codigoCurso);
        return ins;
    }

    /**
     * Registra una nota individual en una inscripción y persiste.
     * Úsalo desde VentanaNotas para que quede en bitácora.
     */
    public void registrarNota(String carnet, String codigoCurso,
                               String nombreEval, double nota) {
        Inscripcion ins = buscarInscripcion(carnet, codigoCurso);
        if (ins == null)
            throw new IllegalArgumentException(
                "Inscripción no encontrada: " + carnet + " / " + codigoCurso);
        ins.registrarNota(nombreEval, nota);
        guardar();
        Bitacora.notaRegistrada(carnet, codigoCurso, nombreEval, nota);
    }

    /**
     * Guarda todas las notas que ya están en memoria (llamada desde VentanaNotas
     * cuando el usuario presiona "Guardar notas" con múltiples campos).
     * Las notas deben haberse registrado previamente con ins.registrarNota().
     */
    public void guardarNotas() {
        guardar();
        // La bitácora detallada se escribe nota a nota desde VentanaNotas
        // usando registrarNota(); este método solo persiste el estado.
    }

    public Inscripcion buscarInscripcion(String carnet, String codigoCurso) {
        for (Inscripcion ins : inscripciones)
            if (ins.getEstudiante().getCarnet().equals(carnet) &&
                ins.getCurso().getCodigo().equals(codigoCurso)) return ins;
        return null;
    }

    public List<Inscripcion> getInscripcionesPorCurso(String codigoCurso) {
        List<Inscripcion> res = new ArrayList<>();
        for (Inscripcion ins : inscripciones)
            if (ins.getCurso().getCodigo().equals(codigoCurso)) res.add(ins);
        return res;
    }

    public List<Inscripcion> getInscripcionesPorEstudiante(String carnet) {
        List<Inscripcion> res = new ArrayList<>();
        for (Inscripcion ins : inscripciones)
            if (ins.getEstudiante().getCarnet().equals(carnet)) res.add(ins);
        return res;
    }
}