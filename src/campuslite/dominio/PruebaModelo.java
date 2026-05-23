package campuslite.dominio;

/**
 * Clase de prueba rápida para verificar el modelo sin necesidad de Swing.
 * Ejecutar como Java Application desde Eclipse.
 * NO es parte de la entrega final; sirve para detectar errores temprano.
 */
public class PruebaModelo {

    public static void main(String[] args) {

        System.out.println("=== Prueba del modelo Campus Lite ===\n");

        // 1. Crear estudiantes (sobrecarga de constructores)
        Estudiante e1 = new Estudiante("20231045", "María García López", "maria@ej.com");
        Estudiante e2 = new Estudiante("20231046", "Carlos Pérez Morales"); // sin correo
        System.out.println("Estudiantes creados:");
        System.out.println("  " + e1);
        System.out.println("  " + e2);

        // 2. Crear curso
        Curso curso = new Curso("POO-2024-A", "Prog. Orientada a Objetos", 4, 30);
        System.out.println("\nCurso: " + curso);

        // 3. Crear evaluaciones (herencia + polimorfismo)
        ExamenEscrito examen = new ExamenEscrito("Examen parcial", 40.0, 85.0);
        Laboratorio lab = new Laboratorio("Laboratorio", 30.0);
        lab.agregarPractica(90.0);
        lab.agregarPractica(80.0);
        lab.agregarPractica(95.0);
        Proyecto proyecto = new Proyecto("Proyecto final", 30.0, 88.0, 5.0);

        // 4. Agregar evaluaciones al curso
        curso.agregarEvaluacion(examen);
        curso.agregarEvaluacion(lab);
        curso.agregarEvaluacion(proyecto);

        System.out.println("\nEvaluaciones del curso:");
        // Polimorfismo: iteramos List<Evaluacion>, cada toString() es el de la subclase
        for (Evaluacion ev : curso.getEvaluaciones()) {
            System.out.println("  " + ev);
        }

        System.out.println("\nPonderaciones completas: " + curso.ponderacionesCompletas());
        System.out.println("Suma ponderaciones: " + curso.sumaPonderaciones() + "%");

        // 5. Calcular promedio ponderado (polimorfismo en acción)
        double promedio = curso.calcularPromedioPonderado();
        System.out.printf("\nPromedio ponderado de %s: %.2f%n", e1.getNombreCompleto(), promedio);
        System.out.println("Estado: " + (Curso.esAprobado(promedio) ? "APROBADO ✓" : "REPROBADO ✗"));

        // 6. Probar validaciones
        System.out.println("\n=== Prueba de validaciones ===");
        try {
            Estudiante invalido = new Estudiante("123", "Sin carnet válido");
        } catch (IllegalArgumentException ex) {
            System.out.println("Validación carnet: " + ex.getMessage());
        }

        try {
            ExamenEscrito notaMala = new ExamenEscrito("Test", 40.0, 150.0);
        } catch (IllegalArgumentException ex) {
            System.out.println("Validación nota: " + ex.getMessage());
        }

        try {
            // Intentar pasar de 100% de ponderación
            ExamenEscrito extra = new ExamenEscrito("Extra", 20.0);
            curso.agregarEvaluacion(extra);
        } catch (IllegalArgumentException ex) {
            System.out.println("Validación ponderación: " + ex.getMessage());
        }

        System.out.println("\n=== Todas las pruebas completadas ===");
    }
}
