package campuslite.persistence;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Registra cada operación de escritura en datos/bitacora.txt.
 *
 * Formato de cada línea:
 *   [YYYY-MM-DD HH:mm:ss] | ACCION | ENTIDAD | DETALLE
 *
 * Acciones posibles:
 *   AGREGAR, ACTUALIZAR, ELIMINAR, INSCRIBIR, NOTA, RESET
 *
 * El archivo NUNCA se sobreescribe; siempre se hace append,
 * por lo que acumula el historial completo de todas las sesiones.
 */
public class Logbook {

    private static final String CARPETA  = "datos";
    private static final String ARCHIVO  = CARPETA + "/bitacora.txt";
    private static final SimpleDateFormat FMT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // ── Métodos públicos de registro ─────────────────────────────────────────

    /** Registra agregar un maestro. */
    public static void maestroAgregado(String codigo, String nombre) {
        escribir("AGREGAR", "MAESTRO",
                "codigo=" + codigo + " | nombre=" + nombre);
    }

    /** Registra actualizar un maestro. */
    public static void maestroActualizado(String codigoAntes, String codigoDespues, String nombre) {
        escribir("ACTUALIZAR", "MAESTRO",
                "antes=" + codigoAntes + " | despues=" + codigoDespues + " | nombre=" + nombre);
    }

    /** Registra eliminar un maestro. */
    public static void maestroEliminado(String codigo) {
        escribir("ELIMINAR", "MAESTRO", "codigo=" + codigo);
    }

    /** Registra agregar un estudiante. */
    public static void estudianteAgregado(String carnet, String nombre) {
        escribir("AGREGAR", "ESTUDIANTE",
                "carnet=" + carnet + " | nombre=" + nombre);
    }

    /** Registra actualizar un estudiante. */
    public static void estudianteActualizado(String carnetAntes, String carnetDespues, String nombre) {
        escribir("ACTUALIZAR", "ESTUDIANTE",
                "antes=" + carnetAntes + " | despues=" + carnetDespues + " | nombre=" + nombre);
    }

    /** Registra eliminar un estudiante. */
    public static void estudianteEliminado(String carnet) {
        escribir("ELIMINAR", "ESTUDIANTE", "carnet=" + carnet);
    }

    /** Registra agregar un curso. */
    public static void cursoAgregado(String codigo, String nombre) {
        escribir("AGREGAR", "CURSO",
                "codigo=" + codigo + " | nombre=" + nombre);
    }

    /** Registra actualizar un curso. */
    public static void cursoActualizado(String codigoAntes, String nombre) {
        escribir("ACTUALIZAR", "CURSO",
                "codigo=" + codigoAntes + " | nombre=" + nombre);
    }

    /** Registra eliminar un curso. */
    public static void cursoEliminado(String codigo) {
        escribir("ELIMINAR", "CURSO", "codigo=" + codigo);
    }

    /** Registra agregar una evaluación a un curso. */
    public static void evaluacionAgregada(String codigoCurso, String nombreEval,
                                           String tipo, double ponderacion) {
        escribir("AGREGAR", "EVALUACION",
                "curso=" + codigoCurso + " | eval=" + nombreEval
                + " | tipo=" + tipo + " | pond=" + ponderacion + "%");
    }

    /** Registra editar ponderación o nombre de una evaluación. */
    public static void evaluacionActualizada(String codigoCurso, String nombreEval,
                                              String campo, String valorAntes, String valorDespues) {
        escribir("ACTUALIZAR", "EVALUACION",
                "curso=" + codigoCurso + " | eval=" + nombreEval
                + " | " + campo + ": [" + valorAntes + "] -> [" + valorDespues + "]");
    }

    /** Registra eliminar una evaluación. */
    public static void evaluacionEliminada(String codigoCurso, String nombreEval) {
        escribir("ELIMINAR", "EVALUACION",
                "curso=" + codigoCurso + " | eval=" + nombreEval);
    }

    /** Registra inscribir un estudiante en un curso. */
    public static void estudianteInscrito(String carnet, String codigoCurso) {
        escribir("INSCRIBIR", "INSCRIPCION",
                "carnet=" + carnet + " | curso=" + codigoCurso);
    }

    /** Registra guardar (crear o actualizar) una nota. */
    public static void notaRegistrada(String carnet, String codigoCurso,
                                       String nombreEval, double nota) {
        escribir("NOTA", "INSCRIPCION",
                "carnet=" + carnet + " | curso=" + codigoCurso
                + " | eval=" + nombreEval + " | nota=" + nota);
    }

    /**
     * Registra que se inició la aplicación sin datos previos
     * y se cargaron datos de demostración.
     */
    public static void inicioConDemo() {
        escribir("RESET", "SISTEMA", "Primera ejecución: se cargaron datos de demostración");
    }

    /**
     * Registra el inicio de una sesión (siempre).
     */
    public static void sesionIniciada() {
        escribir("INICIO", "SISTEMA",
                "Sesión iniciada — datos cargados desde disco");
    }

    // ── Motor interno ────────────────────────────────────────────────────────

    private static void escribir(String accion, String entidad, String detalle) {
        crearCarpetaSiNoExiste();
        String linea = "[" + FMT.format(new Date()) + "] | "
                + pad(accion, 10) + " | "
                + pad(entidad, 12) + " | "
                + detalle;
        try (PrintWriter pw = new PrintWriter(
                new BufferedWriter(new FileWriter(ARCHIVO, true)))) {
            pw.println(linea);
        } catch (IOException e) {
            System.err.println("[Bitacora] Error al escribir: " + e.getMessage());
        }
    }

    private static void crearCarpetaSiNoExiste() {
        File carpeta = new File(CARPETA);
        if (!carpeta.exists()) carpeta.mkdirs();
    }

    /** Rellena con espacios a la derecha para alinear columnas. */
    private static String pad(String s, int ancho) {
        if (s == null) s = "";
        return s.length() >= ancho ? s : s + " ".repeat(ancho - s.length());
    }
}