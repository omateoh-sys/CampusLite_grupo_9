package campuslite.persistence;

import java.io.*;
import java.util.*;

import campuslite.domain.*;

/**
 * Guarda y carga todos los datos del sistema en archivos .txt.
 *
 * Archivos generados (en la carpeta "datos/" junto al proyecto):
 *   datos/maestros.txt
 *   datos/estudiantes.txt
 *   datos/cursos.txt        (incluye evaluaciones de cada curso)
 *   datos/inscripciones.txt (incluye notas por evaluación)
 *
 * Formato general: campos separados por "|"
 * Las listas internas usan ";" como separador secundario.
 */
public class PersistenceTxt {

    // Carpeta donde se guardan los archivos (relativa al directorio de ejecución)
    private static final String CARPETA = "datos";

    private static final String ARCHIVO_MAESTROS      = CARPETA + "/maestros.txt";
    private static final String ARCHIVO_ESTUDIANTES   = CARPETA + "/estudiantes.txt";
    private static final String ARCHIVO_CURSOS        = CARPETA + "/cursos.txt";
    private static final String ARCHIVO_INSCRIPCIONES = CARPETA + "/inscripciones.txt";

    // ══════════════════════════════════════════════════════════════════════
    //  GUARDAR
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Guarda todos los datos del gestor en los archivos .txt.
     * Llamar después de cada operación de escritura.
     */
    public static void guardarTodo(List<Teacher>     maestros,
                                   List<Student>  estudiantes,
                                   List<Course>       cursos,
                                   List<Registration> inscripciones) {
        crearCarpetaSiNoExiste();
        guardarMaestros(maestros);
        guardarEstudiantes(estudiantes);
        guardarCursos(cursos);
        guardarInscripciones(inscripciones);
    }

    // ── Maestros ─────────────────────────────────────────────────────────────
    // Formato: codigoEmpleado|nombreCompleto|correo|especialidad
    private static void guardarMaestros(List<Teacher> maestros) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARCHIVO_MAESTROS))) {
            for (Teacher m : maestros) {
                pw.println(
                    esc(m.getEmployeeCode()) + "|" +
                    esc(m.getFullName()) + "|" +
                    esc(m.getEmail())         + "|" +
                    esc(m.getSpecialty())
                );
            }
        } catch (IOException e) {
            System.err.println("[Persistencia] Error guardando maestros: " + e.getMessage());
        }
    }

    // ── Estudiantes ───────────────────────────────────────────────────────────
    // Formato: carnet|nombreCompleto|correo
    private static void guardarEstudiantes(List<Student> estudiantes) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARCHIVO_ESTUDIANTES))) {
            for (Student e : estudiantes) {
                pw.println(
                    esc(e.getStudentId())          + "|" +
                    esc(e.getFullName())  + "|" +
                    esc(e.getEmail())
                );
            }
        } catch (IOException e) {
            System.err.println("[Persistencia] Error guardando estudiantes: " + e.getMessage());
        }
    }

    // ── Cursos ────────────────────────────────────────────────────────────────
    // Formato por línea:
    //   CURSO|codigo|nombre|creditos|cupoMaximo|codigoMaestro(o vacío)
    //   EVAL|tipo|nombre|ponderacion[|camposExtra]
    //   ...
    //   ---   (separador entre cursos)
    //
    // Tipos de evaluación:
    //   EXAMEN|nombre|ponderacion|nota
    //   LAB|nombre|ponderacion|nota1;nota2;nota3
    //   PROYECTO|nombre|ponderacion|notaBase|bonificacion
    private static void guardarCursos(List<Course> cursos) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARCHIVO_CURSOS))) {
            for (Course c : cursos) {
                String codMaestro = (c.getMaestro() != null)
                        ? c.getMaestro().getEmployeeCode() : "";
                pw.println("CURSO|" +
                    esc(c.getCode())   + "|" +
                    esc(c.getName())   + "|" +
                    c.getCredits()      + "|" +
                    c.getMaxCapacity()    + "|" +
                    esc(codMaestro)
                );
                for (Evaluation ev : c.getEvaluations()) {
                    if (ev instanceof WritteExam) {
                        WritteExam ex = (WritteExam) ev;
                        pw.println("EXAMEN|" +
                            esc(ex.getNombre())     + "|" +
                            ex.getPonderacion()     + "|" +
                            ex.getNota()
                        );
                    } else if (ev instanceof Laboratory) {
                        Laboratory lab = (Laboratory) ev;
                        StringBuilder practicas = new StringBuilder();
                        for (Double p : lab.getPracticas()) {
                            if (practicas.length() > 0) practicas.append(";");
                            practicas.append(p);
                        }
                        pw.println("LAB|" +
                            esc(lab.getNombre())  + "|" +
                            lab.getPonderacion()  + "|" +
                            practicas
                        );
                    } else if (ev instanceof Project) {
                        Project proy = (Project) ev;
                        pw.println("PROYECTO|" +
                            esc(proy.getNombre())     + "|" +
                            proy.getPonderacion()     + "|" +
                            proy.getNotaBase()        + "|" +
                            proy.getBonificacion()
                        );
                    }
                }
                pw.println("---");
            }
        } catch (IOException e) {
            System.err.println("[Persistencia] Error guardando cursos: " + e.getMessage());
        }
    }

    // ── Inscripciones ─────────────────────────────────────────────────────────
    // Formato por línea:
    //   INSCRIPCION|carnet|codigoCurso
    //   NOTA|nombreEvaluacion|valor
    //   ...
    //   ---
    private static void guardarInscripciones(List<Registration> inscripciones) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARCHIVO_INSCRIPCIONES))) {
            for (Registration ins : inscripciones) {
                pw.println("INSCRIPCION|" +
                    esc(ins.getEstudiante().getStudentId()) + "|" +
                    esc(ins.getCurso().getCode())
                );
                for (Map.Entry<String, Double> entry : ins.getTodasLasNotas().entrySet()) {
                    pw.println("NOTA|" +
                        esc(entry.getKey()) + "|" +
                        entry.getValue()
                    );
                }
                pw.println("---");
            }
        } catch (IOException e) {
            System.err.println("[Persistencia] Error guardando inscripciones: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  CARGAR
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Carga todos los datos desde los archivos.txt.
     * Retorna un DatosApp con las listas listas para usar.
     * Si algún archivo no existe, esa lista queda vacía (sin errores).
     */
    public static DatosApp cargarTodo() {
        List<Teacher>     maestros      = cargarMaestros();
        List<Student>  estudiantes   = cargarEstudiantes();
        List<Course>       cursos        = cargarCursos(maestros);
        List<Registration> inscripciones = cargarInscripciones(estudiantes, cursos);
        return new DatosApp(maestros, estudiantes, cursos, inscripciones);
    }

    // ── Cargar Maestros ───────────────────────────────────────────────────────
    private static List<Teacher> cargarMaestros() {
        List<Teacher> lista = new ArrayList<>();
        File f = new File(ARCHIVO_MAESTROS);
        if (!f.exists()) return lista;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;
                String[] p = linea.split("\\|", -1);
                if (p.length < 4) continue;
                try {
                    lista.add(new Teacher(p[0], p[1], p[2], p[3]));
                } catch (IllegalArgumentException e) {
                    System.err.println("[Persistencia] Maestro inválido ignorado: " + linea);
                }
            }
        } catch (IOException e) {
            System.err.println("[Persistencia] Error cargando maestros: " + e.getMessage());
        }
        return lista;
    }

    // ── Cargar Estudiantes ────────────────────────────────────────────────────
    private static List<Student> cargarEstudiantes() {
        List<Student> lista = new ArrayList<>();
        File f = new File(ARCHIVO_ESTUDIANTES);
        if (!f.exists()) return lista;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;
                String[] p = linea.split("\\|", -1);
                if (p.length < 3) continue;
                try {
                    lista.add(new Student(p[0], p[1], p[2]));
                } catch (IllegalArgumentException e) {
                    System.err.println("[Persistencia] Estudiante inválido ignorado: " + linea);
                }
            }
        } catch (IOException e) {
            System.err.println("[Persistencia] Error cargando estudiantes: " + e.getMessage());
        }
        return lista;
    }

    // ── Cargar Cursos ─────────────────────────────────────────────────────────
    private static List<Course> cargarCursos(List<Teacher> maestros) {
        List<Course> lista = new ArrayList<>();
        File f = new File(ARCHIVO_CURSOS);
        if (!f.exists()) return lista;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String linea;
            Course cursoActual = null;

            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;

                if (linea.equals("---")) {
                    if (cursoActual != null) lista.add(cursoActual);
                    cursoActual = null;
                    continue;
                }

                String[] p = linea.split("\\|", -1);

                if (p[0].equals("CURSO") && p.length >= 6) {
                    try {
                        int creditos = Integer.parseInt(p[3]);
                        int cupo     = Integer.parseInt(p[4]);
                        cursoActual  = new Course(p[1], p[2], creditos, cupo);
                        // Vincular maestro si existe
                        if (!p[5].isEmpty()) {
                            Teacher m = buscarMaestro(maestros, p[5]);
                            if (m != null) cursoActual.setMaestro(m);
                        }
                    } catch (Exception e) {
                        System.err.println("[Persistencia] Curso inválido ignorado: " + linea);
                        cursoActual = null;
                    }

                } else if (cursoActual != null && p[0].equals("EXAMEN") && p.length >= 4) {
                    try {
                        double ponderacion = Double.parseDouble(p[2]);
                        double nota        = Double.parseDouble(p[3]);
                        cursoActual.addEvaluation(new WritteExam(p[1], ponderacion, nota));
                    } catch (Exception e) {
                        System.err.println("[Persistencia] Examen inválido ignorado: " + linea);
                    }

                } else if (cursoActual != null && p[0].equals("LAB") && p.length >= 3) {
                    try {
                        double ponderacion = Double.parseDouble(p[2]);
                        Laboratory lab = new Laboratory(p[1], ponderacion);
                        if (p.length >= 4 && !p[3].isEmpty()) {
                            for (String nota : p[3].split(";")) {
                                lab.agregarPractica(Double.parseDouble(nota));
                            }
                        }
                        cursoActual.addEvaluation(lab);
                    } catch (Exception e) {
                        System.err.println("[Persistencia] Laboratorio inválido ignorado: " + linea);
                    }

                } else if (cursoActual != null && p[0].equals("PROYECTO") && p.length >= 5) {
                    try {
                        double ponderacion  = Double.parseDouble(p[2]);
                        double notaBase     = Double.parseDouble(p[3]);
                        double bonificacion = Double.parseDouble(p[4]);
                        cursoActual.addEvaluation(new Project(p[1], ponderacion, notaBase, bonificacion));
                    } catch (Exception e) {
                        System.err.println("[Persistencia] Proyecto inválido ignorado: " + linea);
                    }
                }
            }
            // Por si faltó el separador al final
            if (cursoActual != null) lista.add(cursoActual);

        } catch (IOException e) {
            System.err.println("[Persistencia] Error cargando cursos: " + e.getMessage());
        }
        return lista;
    }

    // ── Cargar Inscripciones ──────────────────────────────────────────────────
    private static List<Registration> cargarInscripciones(List<Student> estudiantes,
                                                          List<Course> cursos) {
        List<Registration> lista = new ArrayList<>();
        File f = new File(ARCHIVO_INSCRIPCIONES);
        if (!f.exists()) return lista;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String linea;
            Registration insActual = null;

            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;

                if (linea.equals("---")) {
                    if (insActual != null) lista.add(insActual);
                    insActual = null;
                    continue;
                }

                String[] p = linea.split("\\|", -1);

                if (p[0].equals("INSCRIPCION") && p.length >= 3) {
                    Student est  = buscarEstudiante(estudiantes, p[1]);
                    Course      cur  = buscarCurso(cursos, p[2]);
                    if (est != null && cur != null) {
                        insActual = new Registration(est, cur);
                    } else {
                        System.err.println("[Persistencia] Inscripción con referencias no encontradas: " + linea);
                        insActual = null;
                    }

                } else if (insActual != null && p[0].equals("NOTA") && p.length >= 3) {
                    try {
                        insActual.registerGrade(p[1], Double.parseDouble(p[2]));
                    } catch (Exception e) {
                        System.err.println("[Persistencia] Nota inválida ignorada: " + linea);
                    }
                }
            }
            if (insActual != null) lista.add(insActual);

        } catch (IOException e) {
            System.err.println("[Persistencia] Error cargando inscripciones: " + e.getMessage());
        }
        return lista;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  UTILIDADES PRIVADAS
    // ══════════════════════════════════════════════════════════════════════

    private static void crearCarpetaSiNoExiste() {
        File carpeta = new File(CARPETA);
        if (!carpeta.exists()) carpeta.mkdirs();
    }

    /** Escapa el separador "|" dentro de un valor para no romper el formato. */
    private static String esc(String valor) {
        if (valor == null) return "";
        return valor.replace("|", "\\pipe");
    }

    private static Teacher buscarMaestro(List<Teacher> lista, String codigo) {
        for (Teacher m : lista)
            if (m.getEmployeeCode().equals(codigo)) return m;
        return null;
    }

    private static Student buscarEstudiante(List<Student> lista, String carnet) {
        for (Student e : lista)
            if (e.getStudentId().equals(carnet)) return e;
        return null;
    }

    private static Course buscarCurso(List<Course> lista, String codigo) {
        for (Course c : lista)
            if (c.getCode().equals(codigo)) return c;
        return null;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  CLASE AUXILIAR: contenedor de los datos cargados
    // ══════════════════════════════════════════════════════════════════════

    public static class DatosApp {
        public final List<Teacher>     maestros;
        public final List<Student>  estudiantes;
        public final List<Course>       cursos;
        public final List<Registration> inscripciones;

        public DatosApp(List<Teacher> maestros, List<Student> estudiantes,
                        List<Course> cursos, List<Registration> inscripciones) {
            this.maestros      = maestros;
            this.estudiantes   = estudiantes;
            this.cursos        = cursos;
            this.inscripciones = inscripciones;
        }

        public boolean estaVacio() {
            return maestros.isEmpty() && estudiantes.isEmpty()
                && cursos.isEmpty()   && inscripciones.isEmpty();
        }
    }
}