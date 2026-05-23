package campuslite.ui;

import campuslite.dominio.*;
import campuslite.persistencia.GestorCampus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Ventana de Cursos con 3 pestañas:
 *   1. Cursos        — CRUD + asignación de maestro
 *   2. Evaluaciones  — agregar, editar nombre/ponderación, eliminar
 *   3. Inscripciones — inscribir estudiantes
 *
 * Todas las operaciones de escritura pasan por GestorCampus,
 * lo que garantiza persistencia en .txt y registro en bitácora.
 */
public class VentanaCursos extends JFrame {

    private final GestorCampus gestor = GestorCampus.getInstancia();

    // Tab 1
    private JTextField txtCodigo, txtNombre, txtCreditos, txtCupo;
    private JComboBox<String> cbMaestro;
    private JTable tablaCursos;
    private DefaultTableModel modeloCursos;
    private String codigoSeleccionado = null;

    // Tab 2
    private JComboBox<String> cbCursoEval;
    private JTextField txtNombreEval, txtPonderacion;
    private JComboBox<String> cbTipoEval;
    private JTable tablaEvaluaciones;
    private DefaultTableModel modeloEvaluaciones;
    // Edición de evaluación existente
    private JTextField txtEditNombre, txtEditPonderacion;
    private int filaEvalSeleccionada = -1;

    // Tab 3
    private JComboBox<String> cbCursoInscripcion;
    private JComboBox<String> cbEstudianteInscripcion;
    private JTable tablaInscritos;
    private DefaultTableModel modeloInscritos;

    public VentanaCursos() {
        setTitle("Campus Lite — Cursos y Evaluaciones");
        setSize(920, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initComponentes();
    }

    private void initComponentes() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("📚 Cursos",        crearTabCursos());
        tabs.addTab("📋 Evaluaciones",  crearTabEvaluaciones());
        tabs.addTab("👥 Inscripciones", crearTabInscripciones());
        tabs.addChangeListener(e -> refrescarTodo());
        setContentPane(tabs);
        cargarTablaCursos();
        refrescarComboMaestros();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TAB 1 — CURSOS
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel crearTabCursos() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(255, 0));
        form.setBorder(BorderFactory.createTitledBorder("Datos del curso"));

        form.add(lbl("Código *:"));   txtCodigo   = campo(); form.add(txtCodigo);   form.add(strut());
        form.add(lbl("Nombre *:"));   txtNombre   = campo(); form.add(txtNombre);   form.add(strut());
        form.add(lbl("Créditos:"));   txtCreditos = campo(); form.add(txtCreditos); form.add(strut());
        form.add(lbl("Cupo:"));       txtCupo     = campo(); form.add(txtCupo);     form.add(strut());

        form.add(lbl("Maestro asignado:"));
        cbMaestro = new JComboBox<>();
        cbMaestro.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        form.add(cbMaestro); form.add(strut(16));
        form.add(new JSeparator()); form.add(strut(10));

        JButton btnGuardar  = btn("Guardar curso",   () -> accionGuardarCurso());
        JButton btnLimpiar  = btn("Nuevo / Limpiar", () -> limpiarFormCurso());
        JButton btnEliminar = btn("Eliminar curso",  () -> accionEliminarCurso());
        btnEliminar.setForeground(Color.RED);
        form.add(btnGuardar); form.add(strut(6));
        form.add(btnLimpiar); form.add(strut(6));
        form.add(btnEliminar);

        String[] cols = {"Código", "Nombre", "Créditos", "Cupo", "Maestro", "Evaluaciones"};
        modeloCursos = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaCursos = new JTable(modeloCursos);
        tablaCursos.setRowHeight(22);
        tablaCursos.getColumnModel().getColumn(1).setPreferredWidth(180);
        tablaCursos.getColumnModel().getColumn(4).setPreferredWidth(140);
        tablaCursos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarFilaCurso();
        });

        JPanel right = new JPanel(new BorderLayout());
        right.setBorder(BorderFactory.createTitledBorder("Cursos registrados"));
        right.add(new JScrollPane(tablaCursos));

        panel.add(form,  BorderLayout.WEST);
        panel.add(right, BorderLayout.CENTER);
        return panel;
    }

    private void accionGuardarCurso() {
        String cod    = txtCodigo.getText().trim();
        String nom    = txtNombre.getText().trim();
        String credTx = txtCreditos.getText().trim();
        String cupoTx = txtCupo.getText().trim();

        if (cod.isEmpty() || nom.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Código y nombre son obligatorios.",
                "Campos requeridos", JOptionPane.WARNING_MESSAGE); return;
        }
        int cred = 0, cupo = 0;
        try {
            if (!credTx.isEmpty()) cred = Integer.parseInt(credTx);
            if (!cupoTx.isEmpty()) cupo = Integer.parseInt(cupoTx);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Créditos y cupo deben ser números enteros.",
                "Error de formato", JOptionPane.ERROR_MESSAGE); return;
        }

        Maestro maestroSel = null;
        String selMaestro = (String) cbMaestro.getSelectedItem();
        if (selMaestro != null && !selMaestro.equals("— Sin asignar —")) {
            String codM = selMaestro.split(" — ")[0].trim();
            maestroSel = gestor.buscarMaestroPorCodigo(codM);
        }

        try {
            Curso c = new Curso(cod, nom, cred, cupo);
            c.setMaestro(maestroSel);

            if (codigoSeleccionado == null) {
                gestor.agregarCurso(c);
                JOptionPane.showMessageDialog(this, "Curso agregado.", "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Conservar evaluaciones originales
                Curso original = gestor.buscarCursoPorCodigo(codigoSeleccionado);
                if (original != null)
                    for (Evaluacion ev : original.getEvaluaciones())
                        c.agregarEvaluacion(ev);
                gestor.actualizarCurso(codigoSeleccionado, c);
                JOptionPane.showMessageDialog(this, "Curso actualizado.", "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            }
            cargarTablaCursos(); limpiarFormCurso();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionEliminarCurso() {
        if (codigoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un curso primero.",
                "Sin selección", JOptionPane.WARNING_MESSAGE); return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
            "¿Eliminar curso " + codigoSeleccionado + "?\nSe eliminarán sus inscripciones.",
            "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            gestor.eliminarCurso(codigoSeleccionado);
            cargarTablaCursos(); limpiarFormCurso();
        }
    }

    private void cargarTablaCursos() {
        modeloCursos.setRowCount(0);
        for (Curso c : gestor.getCursos()) {
            String maestro = c.getMaestro() != null
                ? c.getMaestro().getNombreCompleto() : "— Sin asignar —";
            modeloCursos.addRow(new Object[]{
                c.getCodigo(), c.getNombre(), c.getCreditos(), c.getCupoMaximo(),
                maestro, c.getEvaluaciones().size() + " eval(s)"
            });
        }
    }

    private void cargarFilaCurso() {
        int fila = tablaCursos.getSelectedRow();
        if (fila < 0) return;
        codigoSeleccionado = (String) modeloCursos.getValueAt(fila, 0);
        Curso c = gestor.buscarCursoPorCodigo(codigoSeleccionado);
        if (c == null) return;
        txtCodigo  .setText(c.getCodigo());
        txtNombre  .setText(c.getNombre());
        txtCreditos.setText(String.valueOf(c.getCreditos()));
        txtCupo    .setText(String.valueOf(c.getCupoMaximo()));
        if (c.getMaestro() != null) {
            String item = c.getMaestro().getCodigoEmpleado()
                        + " — " + c.getMaestro().getNombreCompleto();
            cbMaestro.setSelectedItem(item);
        } else {
            cbMaestro.setSelectedItem("— Sin asignar —");
        }
    }

    private void limpiarFormCurso() {
        codigoSeleccionado = null;
        txtCodigo.setText(""); txtNombre.setText("");
        txtCreditos.setText(""); txtCupo.setText("");
        cbMaestro.setSelectedIndex(0);
        tablaCursos.clearSelection();
    }

    private void refrescarComboMaestros() {
        cbMaestro.removeAllItems();
        cbMaestro.addItem("— Sin asignar —");
        for (Maestro m : gestor.getMaestros())
            cbMaestro.addItem(m.getCodigoEmpleado() + " — " + m.getNombreCompleto());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TAB 2 — EVALUACIONES
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel crearTabEvaluaciones() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // ── Panel izquierdo: agregar nueva + editar existente ──────────────
        JPanel izq = new JPanel();
        izq.setLayout(new BoxLayout(izq, BoxLayout.Y_AXIS));
        izq.setPreferredSize(new Dimension(270, 0));

        // Selector de curso
        JPanel selCurso = new JPanel(new BorderLayout(4, 0));
        selCurso.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        selCurso.add(new JLabel("Curso: "), BorderLayout.WEST);
        cbCursoEval = new JComboBox<>();
        cbCursoEval.addActionListener(e -> cargarTablaEvaluaciones());
        selCurso.add(cbCursoEval, BorderLayout.CENTER);
        izq.add(selCurso); izq.add(strut(10));

        // ── Sección AGREGAR ────────────────────────────────────────────────
        JPanel panelAgregar = new JPanel();
        panelAgregar.setLayout(new BoxLayout(panelAgregar, BoxLayout.Y_AXIS));
        panelAgregar.setBorder(BorderFactory.createTitledBorder("Agregar evaluación"));

        panelAgregar.add(lbl("Nombre de la evaluación *:"));
        txtNombreEval = new JTextField();
        txtNombreEval.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        panelAgregar.add(txtNombreEval); panelAgregar.add(strut());

        panelAgregar.add(lbl("Ponderación (%) *:"));
        txtPonderacion = new JTextField();
        txtPonderacion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        panelAgregar.add(txtPonderacion); panelAgregar.add(strut());

        panelAgregar.add(lbl("Tipo:"));
        cbTipoEval = new JComboBox<>(new String[]{"Examen Escrito", "Laboratorio", "Proyecto"});
        cbTipoEval.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        panelAgregar.add(cbTipoEval); panelAgregar.add(strut(10));

        panelAgregar.add(btn("➕ Agregar evaluación", () -> accionAgregarEvaluacion()));
        izq.add(panelAgregar); izq.add(strut(10));

        // ── Sección EDITAR ─────────────────────────────────────────────────
        JPanel panelEditar = new JPanel();
        panelEditar.setLayout(new BoxLayout(panelEditar, BoxLayout.Y_AXIS));
        panelEditar.setBorder(BorderFactory.createTitledBorder(
            "Editar evaluación seleccionada"));

        panelEditar.add(lbl("Nuevo nombre:"));
        txtEditNombre = new JTextField();
        txtEditNombre.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        panelEditar.add(txtEditNombre); panelEditar.add(strut());

        panelEditar.add(lbl("Nueva ponderación (%):"));
        txtEditPonderacion = new JTextField();
        txtEditPonderacion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        panelEditar.add(txtEditPonderacion); panelEditar.add(strut(10));

        JButton btnActualizar = btn("💾 Guardar cambios", () -> accionActualizarEvaluacion());
        JButton btnEliminarEv = btn("🗑 Eliminar evaluación", () -> accionEliminarEvaluacion());
        btnEliminarEv.setForeground(Color.RED);
        panelEditar.add(btnActualizar); panelEditar.add(strut(6));
        panelEditar.add(btnEliminarEv);
        izq.add(panelEditar);

        // ── Tabla de evaluaciones ──────────────────────────────────────────
        String[] cols = {"#", "Nombre", "Tipo", "Ponderación", "Acumulado"};
        modeloEvaluaciones = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaEvaluaciones = new JTable(modeloEvaluaciones);
        tablaEvaluaciones.setRowHeight(22);
        tablaEvaluaciones.getColumnModel().getColumn(0).setPreferredWidth(30);
        tablaEvaluaciones.getColumnModel().getColumn(1).setPreferredWidth(160);
        tablaEvaluaciones.getColumnModel().getColumn(2).setPreferredWidth(110);
        tablaEvaluaciones.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarFilaEvaluacion();
        });

        JPanel right = new JPanel(new BorderLayout());
        right.setBorder(BorderFactory.createTitledBorder("Evaluaciones del curso"));
        right.add(new JScrollPane(tablaEvaluaciones));

        // Nota pie: suma actual
        JLabel lblSuma = new JLabel(" Suma: —");
        lblSuma.setFont(lblSuma.getFont().deriveFont(Font.BOLD, 11f));
        right.add(lblSuma, BorderLayout.SOUTH);

        // Actualizar etiqueta de suma al cambiar curso
        cbCursoEval.addActionListener(e -> {
            Curso c = getCursoDeCombo(cbCursoEval);
            lblSuma.setText(c == null ? " Suma: —"
                : " Suma ponderaciones: " + c.sumaPonderaciones() + "%"
                + (c.ponderacionesCompletas() ? "  ✓ Completo" : "  (pendiente)"));
        });

        panel.add(izq,   BorderLayout.WEST);
        panel.add(right, BorderLayout.CENTER);
        return panel;
    }

    private void accionAgregarEvaluacion() {
        Curso curso = getCursoDeCombo(cbCursoEval);
        if (curso == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un curso primero.",
                "Sin selección", JOptionPane.WARNING_MESSAGE); return;
        }
        String nombre = txtNombreEval.getText().trim();
        String pondTx = txtPonderacion.getText().trim();
        if (nombre.isEmpty() || pondTx.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nombre y ponderación son obligatorios.",
                "Campos requeridos", JOptionPane.WARNING_MESSAGE); return;
        }
        try {
            double pond = Double.parseDouble(pondTx);
            String tipo = (String) cbTipoEval.getSelectedItem();
            Evaluacion ev;
            if ("Laboratorio".equals(tipo))      ev = new Laboratorio(nombre, pond);
            else if ("Proyecto".equals(tipo))    ev = new Proyecto(nombre, pond);
            else                                 ev = new ExamenEscrito(nombre, pond);

            // Usa el gestor para persistir + bitácora
            gestor.agregarEvaluacion(curso.getCodigo(), ev);

            cargarTablaEvaluaciones(); cargarTablaCursos();
            txtNombreEval.setText(""); txtPonderacion.setText("");
            JOptionPane.showMessageDialog(this, "Evaluación agregada correctamente.",
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "La ponderación debe ser un número.",
                "Error de formato", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionActualizarEvaluacion() {
        if (filaEvalSeleccionada < 0) {
            JOptionPane.showMessageDialog(this,
                "Seleccione una evaluación en la tabla primero.",
                "Sin selección", JOptionPane.WARNING_MESSAGE); return;
        }
        Curso curso = getCursoDeCombo(cbCursoEval);
        if (curso == null) return;

        List<Evaluacion> evals = curso.getEvaluaciones();
        if (filaEvalSeleccionada >= evals.size()) return;
        Evaluacion ev = evals.get(filaEvalSeleccionada);

        String nuevoNombre = txtEditNombre.getText().trim();
        String pondTx      = txtEditPonderacion.getText().trim();

        try {
            // Cambiar nombre si se escribió uno diferente
            if (!nuevoNombre.isEmpty() && !nuevoNombre.equals(ev.getNombre())) {
                gestor.renombrarEvaluacion(curso.getCodigo(), ev.getNombre(), nuevoNombre);
                // Actualizamos la referencia local para el siguiente paso
                ev = curso.getEvaluaciones().get(filaEvalSeleccionada);
            }
            // Cambiar ponderación si se escribió algo
            if (!pondTx.isEmpty()) {
                double nuevaPond = Double.parseDouble(pondTx);
                if (nuevaPond != ev.getPonderacion()) {
                    gestor.editarPonderacionEvaluacion(
                        curso.getCodigo(), ev.getNombre(), nuevaPond);
                }
            }
            cargarTablaEvaluaciones(); cargarTablaCursos();
            txtEditNombre.setText(""); txtEditPonderacion.setText("");
            filaEvalSeleccionada = -1;
            tablaEvaluaciones.clearSelection();
            JOptionPane.showMessageDialog(this, "Evaluación actualizada correctamente.",
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "La ponderación debe ser un número.",
                "Error de formato", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionEliminarEvaluacion() {
        if (filaEvalSeleccionada < 0) {
            JOptionPane.showMessageDialog(this,
                "Seleccione una evaluación en la tabla primero.",
                "Sin selección", JOptionPane.WARNING_MESSAGE); return;
        }
        Curso curso = getCursoDeCombo(cbCursoEval);
        if (curso == null) return;
        List<Evaluacion> evals = curso.getEvaluaciones();
        if (filaEvalSeleccionada >= evals.size()) return;

        Evaluacion ev = evals.get(filaEvalSeleccionada);
        int ok = JOptionPane.showConfirmDialog(this,
            "¿Eliminar la evaluación \"" + ev.getNombre() + "\"?\n"
            + "Se perderán las notas asociadas en las inscripciones.",
            "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            gestor.eliminarEvaluacion(curso.getCodigo(), ev);
            cargarTablaEvaluaciones(); cargarTablaCursos();
            txtEditNombre.setText(""); txtEditPonderacion.setText("");
            filaEvalSeleccionada = -1;
        }
    }

    private void cargarFilaEvaluacion() {
        int fila = tablaEvaluaciones.getSelectedRow();
        filaEvalSeleccionada = fila;
        if (fila < 0) return;
        txtEditNombre.setText((String) modeloEvaluaciones.getValueAt(fila, 1));
        String pond = modeloEvaluaciones.getValueAt(fila, 3)
                        .toString().replace("%", "").trim();
        txtEditPonderacion.setText(pond);
    }

    private void cargarTablaEvaluaciones() {
        modeloEvaluaciones.setRowCount(0);
        Curso curso = getCursoDeCombo(cbCursoEval);
        if (curso == null) return;
        double acum = 0;
        int i = 1;
        for (Evaluacion ev : curso.getEvaluaciones()) {
            acum += ev.getPonderacion();
            String tipo = ev instanceof Laboratorio ? "Laboratorio"
                        : ev instanceof Proyecto    ? "Proyecto"
                        : "Examen Escrito";
            modeloEvaluaciones.addRow(new Object[]{
                i++, ev.getNombre(), tipo,
                ev.getPonderacion() + "%", String.format("%.1f%%", acum)
            });
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TAB 3 — INSCRIPCIONES
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel crearTabInscripciones() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(260, 0));
        form.setBorder(BorderFactory.createTitledBorder("Inscribir estudiante"));

        form.add(lbl("Curso:"));
        cbCursoInscripcion = new JComboBox<>();
        cbCursoInscripcion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        cbCursoInscripcion.addActionListener(e -> cargarTablaInscritos());
        form.add(cbCursoInscripcion); form.add(strut());

        form.add(lbl("Estudiante:"));
        cbEstudianteInscripcion = new JComboBox<>();
        cbEstudianteInscripcion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        form.add(cbEstudianteInscripcion); form.add(strut(16));

        form.add(new JSeparator()); form.add(strut(10));
        form.add(btn("Inscribir", () -> accionInscribir()));

        String[] cols = {"Carnet", "Nombre", "Notas ingresadas"};
        modeloInscritos = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaInscritos = new JTable(modeloInscritos);
        tablaInscritos.setRowHeight(22);
        tablaInscritos.getColumnModel().getColumn(1).setPreferredWidth(180);

        JPanel right = new JPanel(new BorderLayout());
        right.setBorder(BorderFactory.createTitledBorder("Estudiantes inscritos en el curso"));
        right.add(new JScrollPane(tablaInscritos));

        panel.add(form,  BorderLayout.WEST);
        panel.add(right, BorderLayout.CENTER);
        return panel;
    }

    private void accionInscribir() {
        String cursoSel = (String) cbCursoInscripcion.getSelectedItem();
        String estSel   = (String) cbEstudianteInscripcion.getSelectedItem();
        if (cursoSel == null || estSel == null) {
            JOptionPane.showMessageDialog(this, "Seleccione curso y estudiante."); return;
        }
        String codCurso = cursoSel.split(" — ")[0].trim();
        String carnet   = estSel.split(" — ")[0].trim();
        try {
            gestor.inscribir(carnet, codCurso);
            cargarTablaInscritos();
            JOptionPane.showMessageDialog(this, "Estudiante inscrito correctamente.",
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarTablaInscritos() {
        modeloInscritos.setRowCount(0);
        Curso curso = getCursoDeCombo(cbCursoInscripcion);
        if (curso == null) return;
        int totalEval = curso.getEvaluaciones().size();
        for (Inscripcion ins : gestor.getInscripcionesPorCurso(curso.getCodigo())) {
            int conNotas = 0;
            for (Evaluacion ev : curso.getEvaluaciones())
                if (ins.getNota(ev.getNombre()) >= 0) conNotas++;
            modeloInscritos.addRow(new Object[]{
                ins.getEstudiante().getCarnet(),
                ins.getEstudiante().getNombreCompleto(),
                conNotas + " / " + totalEval
            });
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private void refrescarTodo() {
        refrescarComboMaestros();
        refrescarCombo(cbCursoEval);
        refrescarCombo(cbCursoInscripcion);
        cbEstudianteInscripcion.removeAllItems();
        for (Estudiante e : gestor.getEstudiantes())
            cbEstudianteInscripcion.addItem(
                e.getCarnet() + " — " + e.getNombreCompleto());
        cargarTablaEvaluaciones();
        cargarTablaInscritos();
    }

    private void refrescarCombo(JComboBox<String> cb) {
        if (cb == null) return;
        String sel = (String) cb.getSelectedItem();
        cb.removeAllItems();
        for (Curso c : gestor.getCursos())
            cb.addItem(c.getCodigo() + " — " + c.getNombre());
        if (sel != null) cb.setSelectedItem(sel);
    }

    private Curso getCursoDeCombo(JComboBox<String> cb) {
        String sel = (String) cb.getSelectedItem();
        if (sel == null) return null;
        return gestor.buscarCursoPorCodigo(sel.split(" — ")[0].trim());
    }

    private JLabel     lbl(String t)  { JLabel l = new JLabel(t); l.setAlignmentX(Component.LEFT_ALIGNMENT); return l; }
    private JTextField campo()        { JTextField tf = new JTextField(); tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28)); return tf; }
    private Component  strut()        { return Box.createVerticalStrut(8); }
    private Component  strut(int n)   { return Box.createVerticalStrut(n); }
    private JButton    btn(String t, Runnable r) {
        JButton b = new JButton(t);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.addActionListener(e -> r.run());
        return b;
    }
}