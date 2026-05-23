package campuslite.ui;

import campuslite.dominio.*;
import campuslite.persistencia.GestorCampus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Pantalla de registro de notas.
 *
 * Flujo:
 *   1. Seleccionar curso  → se cargan los estudiantes inscritos.
 *   2. Seleccionar estudiante → se genera un campo por cada evaluación.
 *   3. Ingresar notas (0–100) → "Guardar notas" persiste en .txt y bitácora.
 *
 * Cada nota se registra a través de GestorCampus.registrarNota(), que
 * persiste y anota en la bitácora automáticamente.
 */
public class VentanaNotas extends JFrame {

    private final GestorCampus gestor = GestorCampus.getInstancia();

    private JComboBox<String> cbCurso;
    private JComboBox<String> cbEstudiante;
    private JPanel panelNotas;
    private JLabel lblPromedio;
    private JLabel lblEstado;

    // Mapa nombre-evaluación → campo de texto
    private java.util.Map<String, JTextField> camposNota = new java.util.LinkedHashMap<>();

    public VentanaNotas() {
        setTitle("Campus Lite — Registro de Notas");
        setSize(620, 530);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initComponentes();
        refrescarCombos();
    }

    private void initComponentes() {
        JPanel raiz = new JPanel(new BorderLayout(10, 10));
        raiz.setBorder(new EmptyBorder(14, 14, 14, 14));

        // ── Selector curso / estudiante ──────────────────────────────────
        JPanel selPanel = new JPanel(new GridLayout(2, 2, 8, 6));
        selPanel.setBorder(BorderFactory.createTitledBorder("Selección"));

        selPanel.add(new JLabel("Curso:"));
        cbCurso = new JComboBox<>();
        cbCurso.addActionListener(e -> onCursoSeleccionado());
        selPanel.add(cbCurso);

        selPanel.add(new JLabel("Estudiante:"));
        cbEstudiante = new JComboBox<>();
        cbEstudiante.addActionListener(e -> onEstudianteSeleccionado());
        selPanel.add(cbEstudiante);

        // ── Panel central: campos de nota ────────────────────────────────
        panelNotas = new JPanel();
        panelNotas.setLayout(new BoxLayout(panelNotas, BoxLayout.Y_AXIS));
        panelNotas.setBorder(BorderFactory.createTitledBorder("Notas por evaluación"));
        JScrollPane scrollNotas = new JScrollPane(panelNotas);

        // ── Panel inferior: promedio + botones ───────────────────────────
        JPanel bottomPanel = new JPanel(new BorderLayout(8, 0));
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Resultado"));

        JPanel promedioPanel = new JPanel(new GridLayout(2, 2, 6, 4));
        promedioPanel.add(new JLabel("Promedio ponderado:"));
        lblPromedio = new JLabel("—");
        lblPromedio.setFont(lblPromedio.getFont().deriveFont(Font.BOLD, 14f));
        promedioPanel.add(lblPromedio);

        promedioPanel.add(new JLabel("Estado:"));
        lblEstado = new JLabel("—");
        lblEstado.setFont(lblEstado.getFont().deriveFont(Font.BOLD, 14f));
        promedioPanel.add(lblEstado);

        JPanel botonesPanel = new JPanel(new GridLayout(1, 2, 6, 0));
        JButton btnGuardar = new JButton("💾  Guardar notas");
        JButton btnLimpiar = new JButton("✖  Limpiar campos");
        btnGuardar.addActionListener(e -> accionGuardarNotas());
        btnLimpiar.addActionListener(e -> limpiarCampos());
        botonesPanel.add(btnGuardar);
        botonesPanel.add(btnLimpiar);

        bottomPanel.add(promedioPanel, BorderLayout.CENTER);
        bottomPanel.add(botonesPanel,  BorderLayout.EAST);

        raiz.add(selPanel,    BorderLayout.NORTH);
        raiz.add(scrollNotas, BorderLayout.CENTER);
        raiz.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(raiz);
    }

    // ── Eventos de selección ─────────────────────────────────────────────────

    private void onCursoSeleccionado() {
        cbEstudiante.removeAllItems();
        Curso curso = getCursoSeleccionado();
        if (curso == null) return;
        for (Inscripcion ins : gestor.getInscripcionesPorCurso(curso.getCodigo())) {
            Estudiante est = ins.getEstudiante();
            cbEstudiante.addItem(est.getCarnet() + " — " + est.getNombreCompleto());
        }
        reconstruirPanelNotas();
    }

    private void onEstudianteSeleccionado() {
        reconstruirPanelNotas();
    }

    /**
     * Genera dinámicamente un JTextField por evaluación del curso.
     * Si ya hay nota registrada, la pre-rellena.
     */
    private void reconstruirPanelNotas() {
        panelNotas.removeAll();
        camposNota.clear();
        lblPromedio.setText("—");
        lblEstado.setText("—");

        Curso curso = getCursoSeleccionado();
        Inscripcion ins = getInscripcionSeleccionada();

        if (curso == null || ins == null) {
            panelNotas.revalidate(); panelNotas.repaint(); return;
        }

        List<Evaluacion> evals = curso.getEvaluaciones();
        if (evals.isEmpty()) {
            JLabel aviso = new JLabel(
                "  Este curso no tiene evaluaciones. "
                + "Agrégalas en la ventana Cursos → Evaluaciones.");
            aviso.setForeground(Color.GRAY);
            panelNotas.add(aviso);
        }

        for (Evaluacion ev : evals) {
            JPanel fila = new JPanel(new BorderLayout(10, 0));
            fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            fila.setBorder(new EmptyBorder(4, 8, 4, 8));

            String tipo = ev instanceof Laboratorio ? "Lab"
                        : ev instanceof Proyecto    ? "Proy"
                        : "Exam";
            JLabel lbl = new JLabel(String.format("%-30s  [%s  %.0f%%]",
                ev.getNombre(), tipo, ev.getPonderacion()));
            lbl.setFont(lbl.getFont().deriveFont(11f));

            JTextField txt = new JTextField(6);
            double notaActual = ins.getNota(ev.getNombre());
            if (notaActual >= 0)
                txt.setText(String.valueOf((int) notaActual));
            else
                txt.setToolTipText("Sin nota todavía — ingrese un valor 0-100");

            txt.addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusLost(java.awt.event.FocusEvent e) {
                    actualizarPreviewPromedio();
                }
            });

            camposNota.put(ev.getNombre(), txt);
            fila.add(lbl, BorderLayout.CENTER);
            fila.add(txt, BorderLayout.EAST);
            panelNotas.add(fila);
        }

        panelNotas.revalidate(); panelNotas.repaint();
        actualizarPreviewPromedio();
    }

    private void actualizarPreviewPromedio() {
        Inscripcion ins = getInscripcionSeleccionada();
        Curso curso = getCursoSeleccionado();
        if (ins == null || curso == null) return;

        double totalPond = 0, totalNota = 0;
        for (Evaluacion ev : curso.getEvaluaciones()) {
            JTextField txt = camposNota.get(ev.getNombre());
            if (txt == null) continue;
            String val = txt.getText().trim();
            if (!val.isEmpty()) {
                try {
                    double nota = Double.parseDouble(val);
                    totalNota += nota * ev.getPonderacion();
                    totalPond += ev.getPonderacion();
                } catch (NumberFormatException ignored) {}
            }
        }
        if (totalPond == 0) { lblPromedio.setText("—"); lblEstado.setText("—"); return; }

        double prom = totalNota / totalPond;
        lblPromedio.setText(String.format("%.2f", prom));
        if (prom >= 61) {
            lblEstado.setText("APROBADO ✓");
            lblEstado.setForeground(new Color(0, 140, 0));
        } else {
            lblEstado.setText("REPROBADO ✗");
            lblEstado.setForeground(Color.RED);
        }
    }

    // ── Guardar notas ────────────────────────────────────────────────────────

    /**
     * Recorre los campos, valida y guarda cada nota a través del gestor
     * (que persiste en .txt y escribe en bitácora).
     */
    private void accionGuardarNotas() {
        Inscripcion ins = getInscripcionSeleccionada();
        Curso curso     = getCursoSeleccionado();
        if (ins == null || curso == null) {
            JOptionPane.showMessageDialog(this,
                "Seleccione un curso y un estudiante primero.",
                "Sin selección", JOptionPane.WARNING_MESSAGE); return;
        }

        String carnet     = ins.getEstudiante().getCarnet();
        String codCurso   = curso.getCodigo();
        int errores = 0;
        int guardadas = 0;

        for (java.util.Map.Entry<String, JTextField> entry : camposNota.entrySet()) {
            String nomEval = entry.getKey();
            String val     = entry.getValue().getText().trim();
            if (val.isEmpty()) continue;   // campo vacío = no se toca

            try {
                double nota = Double.parseDouble(val);
                if (nota < 0 || nota > 100)
                    throw new IllegalArgumentException(
                        "La nota debe estar entre 0 y 100.");
                // registrarNota persiste + bitácora
                gestor.registrarNota(carnet, codCurso, nomEval, nota);
                guardadas++;
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                    "Valor inválido en \"" + nomEval + "\": debe ser un número.",
                    "Error de formato", JOptionPane.ERROR_MESSAGE);
                errores++;
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                errores++;
            }
        }

        if (errores == 0 && guardadas > 0) {
            actualizarPreviewPromedio();
            JOptionPane.showMessageDialog(this,
                guardadas + " nota(s) guardada(s) correctamente.",
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } else if (guardadas == 0 && errores == 0) {
            JOptionPane.showMessageDialog(this,
                "No hay notas nuevas para guardar.\n"
                + "Ingrese al menos un valor en los campos.",
                "Sin cambios", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /** Limpia todos los campos sin guardar. */
    private void limpiarCampos() {
        for (JTextField txt : camposNota.values()) txt.setText("");
        lblPromedio.setText("—");
        lblEstado.setText("—");
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Curso getCursoSeleccionado() {
        String sel = (String) cbCurso.getSelectedItem();
        if (sel == null) return null;
        return gestor.buscarCursoPorCodigo(sel.split(" — ")[0].trim());
    }

    private Inscripcion getInscripcionSeleccionada() {
        Curso  curso = getCursoSeleccionado();
        String sel   = (String) cbEstudiante.getSelectedItem();
        if (curso == null || sel == null) return null;
        String carnet = sel.split(" — ")[0].trim();
        return gestor.buscarInscripcion(carnet, curso.getCodigo());
    }

    private void refrescarCombos() {
        cbCurso.removeAllItems();
        for (Curso c : gestor.getCursos())
            cbCurso.addItem(c.getCodigo() + " — " + c.getNombre());
    }
}