package campuslite.domain;

/**
 * Clase de prueba rápida para verificar el modelo sin necesidad de Swing.
 * Ejecutar como Java Application desde Eclipse.
 * NO es parte de la entrega final; sirve para detectar errores temprano.
 */
public class TestModel {

    public static void main(String[] args) {

        System.out.println("=== Prueba del modelo Campus Lite ===\n");

        // 1. Crear estudiantes (sobrecarga de constructores)
        Student e1 = new Student("20231045", "María García López", "maria@ej.com");
        Student e2 = new Student("20231046", "Carlos Pérez Morales"); // sin correo
        System.out.println("Estudiantes creados:");
        System.out.println("  " + e1);
        System.out.println("  " + e2);

        // 2. Crear curso
        Course curso = new Course("POO-2024-A", "Prog. Orientada a Objetos", 4, 30);
        System.out.println("\nCurso: " + curso);

        // 3. Crear evaluaciones (herencia + polimorfismo)
        WritteExam examen = new WritteExam("Examen parcial", 40.0, 85.0);
        Laboratory lab = new Laboratory("Laboratorio", 30.0);
        lab.agregarPractica(90.0);
        lab.agregarPractica(80.0);
        lab.agregarPractica(95.0);
        Project proyecto = new Project("Proyecto final", 30.0, 88.0, 5.0);

        // 4. Agregar evaluaciones al curso
        curso.addEvaluation(examen);
        curso.addEvaluation(lab);
        curso.addEvaluation(proyecto);

        System.out.println("\nEvaluaciones del curso:");
        // Polimorfismo: iteramos List<Evaluacion>, cada toString() es el de la subclase
        for (Evaluation ev : curso.getEvaluations()) {
            System.out.println("  " + ev);
        }

        System.out.println("\nPonderaciones completas: " + curso.ponderacionesCompletas());
        System.out.println("Suma ponderaciones: " + curso.sumaPonderaciones() + "%");

        // 5. Calcular promedio ponderado (polimorfismo en acción)
        double promedio = curso.calcularPromedioPonderado();
        System.out.printf("\nPromedio ponderado de %s: %.2f%n", e1.getFullName(), promedio);
        System.out.println("Estado: " + (Course.esAprobado(promedio) ? "APROBADO ✓" : "REPROBADO ✗"));

        // 6. Probar validaciones
        System.out.println("\n=== Prueba de validaciones ===");
        try {
            Student invalido = new Student("123", "Sin carnet válido");
        } catch (IllegalArgumentException ex) {
            System.out.println("Validación carnet: " + ex.getMessage());
        }

        try {
            WritteExam notaMala = new WritteExam("Test", 40.0, 150.0);
        } catch (IllegalArgumentException ex) {
            System.out.println("Validación nota: " + ex.getMessage());
        }

        try {
            // Intentar pasar de 100% de ponderación
            WritteExam extra = new WritteExam("Extra", 20.0);
            curso.addEvaluation(extra);
        } catch (IllegalArgumentException ex) {
            System.out.println("Validación ponderación: " + ex.getMessage());
        }

        System.out.println("\n=== Todas las pruebas completadas ===");
    }
}
