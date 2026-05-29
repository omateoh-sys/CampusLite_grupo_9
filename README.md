# Campus Lite — Sistema de Gestión Académica

**Proyecto Integrador — Opción A | Programación Orientada a Objetos con Java Swing**

| | |
|---|---|
| **Integrantes** | Karen Lizzeth Jarquin Salguero · Alexis Mateo |
| **JDK** | Java 11 |
| **IDE** | Eclipse IDE |
| **Paquete raíz** | `campuslite` |

---

## Tabla de contenidos

1. [¿Qué hace el sistema?](#1-qué-hace-el-sistema)
2. [Estructura del proyecto](#2-estructura-del-proyecto)
3. [Orden de creación de clases](#3-orden-de-creación-de-clases)
4. [Descripción de cada clase](#4-descripción-de-cada-clase)
5. [Diseño POO](#5-diseño-poo)
6. [Cómo ejecutar el proyecto](#6-cómo-ejecutar-el-proyecto)
7. [Persistencia de datos](#7-persistencia-de-datos)
8. [Validaciones importantes](#8-validaciones-importantes)

---

## 1. ¿Qué hace el sistema?

Campus Lite permite gestionar estudiantes, maestros, cursos, evaluaciones, inscripciones y notas desde una interfaz gráfica de escritorio hecha con Java Swing. Los datos se guardan automáticamente en archivos `.txt` dentro de la carpeta `datos/` al cerrar, y se cargan al volver a abrir la aplicación.

**Módulos disponibles:**

- **Estudiantes** — Crear, editar y eliminar estudiantes.
- **Maestros** — Gestión de docentes con especialidad.
- **Cursos y Evaluaciones** — Crear cursos, asignarles maestro y agregar evaluaciones (Examen, Laboratorio, Proyecto).
- **Notas** — Registrar notas por estudiante e inscripción.
- **Reportes** — Ver resultados por curso, por estudiante o resumen general del salón.
- **Bitácora** — Historial completo de todos los cambios realizados en el sistema.

---

## 2. Estructura del proyecto

```
campuslite/
├── domain/              ← Modelo de negocio (clases POO puras, sin Swing)
│   ├── Person.java          Clase abstracta base
│   ├── Student.java         Extiende Person
│   ├── Teacher.java         Extiende Person
│   ├── Evaluation.java      Clase abstracta base de evaluaciones
│   ├── WritteExam.java      Extiende Evaluation
│   ├── Laboratory.java      Extiende Evaluation
│   ├── Project.java         Extiende Evaluation
│   ├── Course.java          Gestiona evaluaciones y cálculos
│   ├── Registration.java    Une estudiante con curso y guarda notas
│   └── TestModel.java       Prueba rápida del modelo sin Swing
│
├── persistence/         ← Capa de guardado y bitácora
│   ├── CampusManager.java   Singleton gestor central (CRUD + persistencia)
│   ├── PersistenceTxt.java  Lee y escribe los archivos .txt
│   └── Logbook.java         Registra cada operación en bitacora.txt
│
└── ui/                  ← Interfaz gráfica Swing
    ├── Main.java            Punto de entrada (main)
    ├── MainWindow.java      Ventana principal con estadísticas y navegación
    ├── StudentWindow.java   CRUD de estudiantes
    ├── TeachersWindow.java  CRUD de maestros
    ├── CourseWindow.java    CRUD de cursos, evaluaciones e inscripciones
    ├── NotesWindow.java     Registro de notas por estudiante
    ├── ReportWindow.java    Reportes (por curso, por estudiante, por salón)
    └── LogWindow.java       Visualizador de bitácora con filtros
```

---

## 3. Orden de creación de clases

Seguir este orden evita errores de compilación porque cada clase depende de las anteriores.

```
Paso 1 — Dominio base (sin dependencias)
  Person.java
  Evaluation.java

Paso 2 — Subclases de dominio
  Student.java          (extiende Person)
  Teacher.java          (extiende Person)
  WritteExam.java       (extiende Evaluation)
  Laboratory.java       (extiende Evaluation)
  Project.java          (extiende Evaluation)

Paso 3 — Clases que usan las anteriores
  Course.java           (usa Evaluation, Teacher)
  Registration.java     (usa Student, Course, Evaluation)

Paso 4 — Prueba sin interfaz gráfica
  TestModel.java        (usa todas las de dominio)

Paso 5 — Persistencia
  Logbook.java          (independiente, solo escribe archivos)
  PersistenceTxt.java   (usa todas las clases de dominio)
  CampusManager.java    (usa PersistenceTxt y Logbook)

Paso 6 — Interfaz gráfica
  Main.java
  MainWindow.java
  StudentWindow.java
  TeachersWindow.java
  CourseWindow.java
  NotesWindow.java
  ReportWindow.java
  LogWindow.java
```

---

## 4. Descripción de cada clase

### Paquete `campuslite.domain`

---

#### `Person.java` — Clase abstracta base de personas

**Lo más importante:** es abstracta, obliga a que cada subclase defina `getIdentificador()` y `getRol()`. Tiene encapsulamiento con validaciones en los setters.

**Atributos:**
- `nombreCompleto` (String) — no puede estar vacío.
- `correo` (String) — opcional; si se ingresa debe contener `@`.

**Métodos abstractos que las subclases deben implementar:**
- `getIdentificador()` → en Student devuelve el carnet; en Teacher devuelve el código de empleado.
- `getRol()` → devuelve `"Estudiante"` o `"Maestro"`.

**Cómo crear:**
```java
// No se puede instanciar directamente (es abstracta)
// Se instancia a través de sus subclases: Student o Teacher
```

---

#### `Student.java` — Representa un estudiante

**Lo más importante:** extiende `Person`, implementa los métodos abstractos heredados y demuestra sobrecarga de constructores.

**Atributos:**
- `carnet` (String) — exactamente 8 dígitos numéricos.

**Constructores (sobrecarga):**
```java
// Con correo
new Student("20231045", "María García López", "maria@ej.com");

// Sin correo (correo queda vacío)
new Student("20231046", "Carlos Pérez Morales");
```

**Validación clave:** el carnet debe tener exactamente 8 dígitos (`\d{8}`). Si no, lanza `IllegalArgumentException`.

---

#### `Teacher.java` — Representa un maestro/docente

**Lo más importante:** segunda subclase de `Person`, demuestra que la herencia funciona para más de un tipo de persona.

**Atributos:**
- `codigoEmpleado` (String) — no puede estar vacío.
- `especialidad` (String) — opcional.

**Constructores (sobrecarga):**
```java
// Con especialidad
new Teacher("DOC001", "Ana López", "ana@ej.com", "Programación");

// Sin especialidad
new Teacher("DOC002", "Luis Ramos", "luis@ej.com");
```

---

#### `Evaluation.java` — Clase abstracta base de evaluaciones

**Lo más importante:** es la segunda clase abstracta del proyecto. Define el método abstracto `calcularNota()` que cada tipo de evaluación implementa de forma diferente. También tiene el método concreto `calcularContribucion()` que usa polimorfismo internamente.

**Atributos:**
- `nombre` (String) — nombre de la evaluación.
- `ponderacion` (double) — porcentaje que representa en el curso (1–100).

**Método clave:**
```java
// calcularContribucion() usa polimorfismo: llama a calcularNota()
// del objeto real (Examen, Laboratorio o Proyecto)
public double calcularContribucion() {
    return (calcularNota() * ponderacion) / 100.0;
}
```

**No se puede instanciar directamente.** Se instancia a través de `WritteExam`, `Laboratory` o `Project`.

---

#### `WritteExam.java` — Evaluación tipo examen escrito

**Lo más importante:** subclase más simple de `Evaluation`. La nota es directa (0–100).

```java
// Sin nota inicial (nota = 0)
new WritteExam("Examen parcial", 40.0);

// Con nota
new WritteExam("Examen parcial", 40.0, 85.0);
```

`calcularNota()` simplemente devuelve la nota ingresada.

---

#### `Laboratory.java` — Evaluación tipo laboratorio

**Lo más importante:** la nota se calcula como el **promedio de varias prácticas individuales**.

```java
Laboratory lab = new Laboratory("Laboratorio", 30.0);
lab.agregarPractica(90.0);
lab.agregarPractica(80.0);
lab.agregarPractica(95.0);
// calcularNota() devuelve (90+80+95)/3 = 88.33
```

Si no hay prácticas registradas, `calcularNota()` devuelve 0.

---

#### `Project.java` — Evaluación tipo proyecto

**Lo más importante:** tiene una **nota base** más una **bonificación** (0–10 puntos extra), pero el resultado no puede superar 100.

```java
// nota base 88, bonificación 5 → calcularNota() devuelve 93
new Project("Proyecto final", 30.0, 88.0, 5.0);
```

Regla de negocio: `Math.min(100, notaBase + bonificacion)`.

---

#### `Course.java` — Representa un curso académico

**Lo más importante:** gestiona la lista de evaluaciones y usa **polimorfismo** para calcular el promedio ponderado.

**Atributos:**
- `codigo` — código único del curso.
- `nombre` — nombre del curso.
- `creditos` — créditos (puede ser 0).
- `cupoMaximo` — cupo máximo (puede ser 0).
- `maestro` — referencia a `Teacher`, puede ser null.
- `evaluaciones` — `List<Evaluation>` (polimorfismo).

**Método más importante:**
```java
public double calcularPromedioPonderado() {
    // Llama a calcularContribucion() de cada evaluación
    // → polimorfismo: se ejecuta el método del tipo real
    for (Evaluation e : evaluaciones)
        total += e.calcularContribucion();
}
```

**Validación clave:** la suma de ponderaciones de todas las evaluaciones no puede superar 100%.

**Constructores (sobrecarga):**
```java
new Course("POO-2024-A", "Prog. OO", 4, 30); // con créditos y cupo
new Course("POO-2024-A", "Prog. OO");         // sin créditos ni cupo
```

---

#### `Registration.java` — Inscripción de estudiante en curso

**Lo más importante:** une un `Student` con un `Course` y almacena las notas de cada evaluación en un `HashMap`.

**Atributos:**
- `estudiante` — referencia al Student.
- `curso` — referencia al Course.
- `notas` — `Map<String, Double>` donde la clave es el nombre de la evaluación.

**Métodos clave:**
```java
ins.registerGrade("Examen parcial", 85.0); // registra una nota
ins.getGrade("Examen parcial");            // devuelve la nota, o -1 si no existe
ins.calcularPromedioFinal();               // promedio ponderado usando notas guardadas
ins.isPassing();                           // true si promedio >= 61.0
```

---

#### `TestModel.java` — Prueba rápida del modelo

Clase utilitaria para verificar que el dominio funciona **sin necesidad de abrir la interfaz gráfica**. Ejecutar como Java Application desde Eclipse antes de trabajar en Swing.

```
Clic derecho en TestModel.java → Run As → Java Application
```

Muestra en consola: creación de estudiantes, cursos, evaluaciones, cálculo de promedios y prueba de todas las validaciones.

---

### Paquete `campuslite.persistence`

---

#### `Logbook.java` — Bitácora de operaciones

**Lo más importante:** registra **cada cambio** en el archivo `datos/bitacora.txt` usando `append`, por lo que nunca sobreescribe el historial.

**Formato de cada línea:**
```
[2024-05-15 10:32:45] | AGREGAR    | ESTUDIANTE   | carnet=20231045 | nombre=María García
```

**No requiere instanciación**, todos sus métodos son `static`:
```java
Logbook.estudianteAgregado("20231045", "María García");
Logbook.notaRegistrada("20231045", "POO-2024-A", "Examen parcial", 85.0);
```

La carpeta `datos/` se crea automáticamente si no existe.

---

#### `PersistenceTxt.java` — Lectura y escritura de archivos

**Lo más importante:** traduce objetos Java ↔ líneas de texto en archivos `.txt` dentro de `datos/`. Es la única clase que toca el disco para leer y guardar entidades.

**Archivos que maneja:**
```
datos/maestros.txt
datos/estudiantes.txt
datos/cursos.txt
datos/inscripciones.txt
datos/bitacora.txt     ← manejado por Logbook
```

Todos los métodos son `static`. `CampusManager` es quien lo llama; las ventanas Swing nunca llaman a esta clase directamente.

---

#### `CampusManager.java` — Gestor central (Singleton)

**Lo más importante:** es el **único punto de acceso** al modelo desde la interfaz gráfica. Implementa el patrón Singleton para que todas las ventanas compartan la misma instancia y los mismos datos en memoria.

**Cómo obtener la instancia:**
```java
CampusManager gestor = CampusManager.getInstancia();
```

**Lo que hace automáticamente al iniciar:**
- Si existen archivos en `datos/` → carga todo en memoria.
- Si no existen → inicia con listas vacías y crea los archivos.

**Operaciones disponibles:**

| Método | Descripción |
|--------|-------------|
| `addStudent(student)` | Agrega estudiante, valida carnet único, guarda, registra en bitácora |
| `actualizarEstudiante(carnetOriginal, nuevo)` | Edita estudiante, guarda, registra |
| `removeStudent(carnet)` | Elimina estudiante y sus inscripciones, guarda |
| `agregarMaestro(teacher)` | Agrega maestro, valida código único |
| `actualizarMaestro(codigo, nuevo)` | Actualiza maestro y referencias en cursos |
| `eliminarMaestro(codigo)` | Elimina maestro; los cursos asignados quedan sin maestro |
| `agregarCurso(course)` | Agrega curso, valida código único |
| `agregarEvaluacion(codigoCurso, ev)` | Agrega evaluación al curso indicado |
| `inscribir(carnet, codigoCurso)` | Crea inscripción, valida que no sea duplicada |
| `registerGrade(carnet, codigoCurso, nombreEval, nota)` | Registra nota, persiste y anota en bitácora |
| `getInscripcionesPorCurso(codigo)` | Lista inscripciones de un curso |
| `getInscripcionesPorEstudiante(carnet)` | Lista inscripciones de un estudiante |

---

### Paquete `campuslite.ui`

---

#### `Main.java` — Punto de entrada

Clase mínima con el `main`. Usa `SwingUtilities.invokeLater` para iniciar la UI correctamente en el hilo de Swing (EDT).

```java
// Para ejecutar desde Eclipse:
// Clic derecho en Main.java → Run As → Java Application
```

---

#### `MainWindow.java` — Ventana principal

**Lo más importante:** muestra tres tarjetas con contadores en tiempo real (estudiantes, cursos activos, maestros) y seis botones de navegación a cada módulo. Los contadores se actualizan automáticamente cada vez que se cierra una ventana secundaria.

**Botones disponibles:**
- Estudiantes → abre `StudentWindow`
- Maestros → abre `TeachersWindow`
- Cursos y Evaluaciones → abre `CourseWindow`
- Notas → abre `NotesWindow`
- Reportes → abre `ReportWindow`
- Bitácora → abre `LogWindow`

---

#### `StudentWindow.java` — Gestión de estudiantes

**Funcionalidad:** crear, editar y eliminar estudiantes.

**Flujo de uso:**
1. Llenar los campos Carnet, Nombre y Correo (correo es opcional).
2. Clic en **Guardar** para crear un nuevo estudiante.
3. Para editar: hacer clic en una fila de la tabla → los datos se cargan en el formulario → modificar y clic en **Guardar**.
4. Para eliminar: seleccionar una fila → clic en **Eliminar** → confirmar en el diálogo.
5. **Nuevo / Limpiar** limpia el formulario para ingresar otro estudiante.

**Validaciones que muestra al usuario:**
- Carnet vacío o con formato incorrecto (debe tener 8 dígitos).
- Nombre vacío.
- Carnet duplicado.

---

#### `TeachersWindow.java` — Gestión de maestros

**Funcionalidad:** crear, editar y eliminar maestros.

**Campos:** Código empleado (obligatorio), Nombre (obligatorio), Correo (opcional), Especialidad (opcional).

La tabla muestra también cuántos cursos tiene asignados cada maestro. Al eliminar un maestro, los cursos que tenía asignados quedan sin maestro (no se eliminan).

---

#### `CourseWindow.java` — Cursos, evaluaciones e inscripciones

**Lo más importante:** es la ventana más completa. Usa `JTabbedPane` con tres pestañas.

**Pestaña 1 — Cursos:**
- CRUD de cursos con código, nombre, créditos, cupo y maestro asignado.

**Pestaña 2 — Evaluaciones:**
- Seleccionar un curso y agregar evaluaciones de tres tipos: Examen Escrito, Laboratorio o Proyecto.
- Muestra la suma de ponderaciones en tiempo real para no superar 100%.
- Permite editar ponderación, renombrar o eliminar evaluaciones.

**Pestaña 3 — Inscripciones:**
- Seleccionar estudiante y curso para inscribir.
- Lista las inscripciones existentes con opción de eliminar.

---

#### `NotesWindow.java` — Registro de notas

**Flujo de uso:**
1. Seleccionar el curso en el primer combo.
2. Seleccionar el estudiante inscrito en ese curso en el segundo combo.
3. Se generan automáticamente campos de texto, uno por cada evaluación del curso.
4. Ingresar las notas (0–100) y clic en **Guardar notas**.
5. El promedio ponderado y estado (Aprobado/Reprobado) se actualizan en tiempo real al salir de cada campo.

Si una evaluación ya tiene nota registrada, el campo aparece pre-rellenado. Los campos vacíos se omiten al guardar.

---

#### `ReportWindow.java` — Reportes

Tiene tres pestañas:

**Tab "Por curso":** seleccionar un curso y ver todos los estudiantes inscritos con sus notas por evaluación, promedio final y estado. Muestra el total de aprobados y reprobados al pie.

**Tab "Por estudiante":** seleccionar un estudiante y ver todas sus inscripciones con el detalle de cada evaluación y el aporte al promedio. Muestra el promedio general al pie.

**Tab "Por salón":** vista global de todas las inscripciones del sistema con filtro (Todos / Solo aprobados / Solo reprobados) y estadísticas generales.

---

#### `LogWindow.java` — Visualizador de bitácora

**Lo más importante:** lee el archivo `datos/bitacora.txt` y lo muestra en una tabla con colores por tipo de acción.

**Filtros disponibles:**
- Por tipo de acción: AGREGAR, ACTUALIZAR, ELIMINAR, INSCRIBIR, NOTA, INICIO, RESET.
- Búsqueda libre en todos los campos.

**Colores:**
- Verde → AGREGAR
- Rojo → ELIMINAR
- Naranja → ACTUALIZAR
- Azul → NOTA
- Violeta → INSCRIBIR

El botón **Exportar copia** permite guardar una copia del archivo en otra ubicación del sistema.

---

## 5. Diseño POO

### Jerarquías de herencia

```
Person (abstracta)
├── Student
└── Teacher

Evaluation (abstracta)
├── WritteExam
├── Laboratory
└── Project
```

### Clases abstractas y métodos abstractos

| Clase abstracta | Método abstracto | Lo implementan |
|---|---|---|
| `Person` | `getIdentificador()` | Student → carnet · Teacher → código empleado |
| `Person` | `getRol()` | Student → "Estudiante" · Teacher → "Maestro" |
| `Evaluation` | `calcularNota()` | WritteExam, Laboratory, Project (cada uno con lógica distinta) |

### Dónde ocurre el polimorfismo

En `Course.calcularPromedioPonderado()`:
```java
for (Evaluation e : evaluaciones)       // List<Evaluation> → polimorfismo
    total += e.calcularContribucion();  // llama calcularNota() del tipo real
```
La misma lista puede contener objetos `WritteExam`, `Laboratory` y `Project`, y cada uno calcula su nota de forma diferente.

### Sobrecarga de constructores

| Clase | Firmas |
|---|---|
| `Student` | `(carnet, nombre, correo)` y `(carnet, nombre)` |
| `Teacher` | `(codigo, nombre, correo, especialidad)` y `(codigo, nombre, correo)` |
| `WritteExam` | `(nombre, ponderacion)` y `(nombre, ponderacion, nota)` |
| `Project` | `(nombre, ponderacion)` y `(nombre, ponderacion, notaBase, bonificacion)` |
| `Course` | `(codigo, nombre, creditos, cupo)` y `(codigo, nombre)` |

---

## 6. Cómo ejecutar el proyecto

### Requisitos

- Java JDK 11 instalado.
- Eclipse IDE.
- Ambos trabajando con el **mismo JDK 11** (verificar en Eclipse: *Window → Preferences → Java → Installed JREs*).

### Pasos

**1. Clonar el repositorio**
```bash
git clone <URL-del-repositorio>
```

**2. Importar en Eclipse**
- *File → Import → Existing Projects into Workspace*
- Seleccionar la carpeta clonada → Finish.

**3. Verificar estructura de paquetes**

Confirmar que Eclipse muestra estos tres paquetes:
```
campuslite.domain
campuslite.persistence
campuslite.ui
```

**4. Ejecutar**
- Clic derecho en `Main.java` → *Run As → Java Application*.
- La ventana principal abre automáticamente.

**5. Verificar que los datos persisten**
- Agregar un estudiante y cerrar la aplicación.
- Volver a ejecutar: el estudiante debe seguir apareciendo.
- Los archivos se guardan en la carpeta `datos/` dentro del directorio del proyecto.

### Prueba del modelo sin Swing

Para verificar solo el dominio (sin abrir ventanas):
```
Clic derecho en TestModel.java → Run As → Java Application
```
Muestra en consola que herencia, polimorfismo y validaciones funcionan correctamente.

---

## 7. Persistencia de datos

Los datos se guardan automáticamente en `datos/` cada vez que se realiza una operación de escritura (no solo al cerrar).

```
datos/
├── maestros.txt
├── estudiantes.txt
├── cursos.txt
├── inscripciones.txt
└── bitacora.txt       ← historial acumulado de todas las sesiones
```

`PersistenceTxt` traduce objetos ↔ líneas de texto. `CampusManager` es quien lo llama internamente; las ventanas Swing nunca acceden a los archivos directamente.

La carpeta `datos/` se crea automáticamente en la primera ejecución.

---

## 8. Validaciones importantes

| Regla | Clase que la aplica | Mensaje al usuario |
|---|---|---|
| Carnet debe tener exactamente 8 dígitos | `Student.setStudentId()` | "El carnet debe tener exactamente 8 dígitos." |
| Nombre no puede estar vacío | `Person.setFullName()` | "El nombre no puede estar vacío." |
| Correo debe contener `@` si se ingresa | `Person.setEmail()` | "El correo no tiene un formato válido." |
| Carnet duplicado | `CampusManager.addStudent()` | "Ya existe un estudiante con carnet: ..." |
| Suma de ponderaciones no puede superar 100% | `Course.addEvaluation()` | "Agregar esta evaluación superaría el 100%..." |
| Nota fuera de rango (0–100) | Setters de cada Evaluation / `Registration` | "La nota debe estar entre 0 y 100." |
| Bonificación en Proyecto (0–10) | `Project.setBonificacion()` | "La bonificación debe estar entre 0 y 10." |
| Inscripción duplicada | `CampusManager.inscribir()` | "... ya está inscrito en ..." |
| Estudiante aprobado si promedio >= 61 | `Registration.isPassing()` | Se muestra "APROBADO ✓" o "REPROBADO ✗" |

Todas las validaciones del dominio lanzan `IllegalArgumentException`, que las ventanas Swing capturan y muestran en un `JOptionPane` con mensaje claro al usuario.  
