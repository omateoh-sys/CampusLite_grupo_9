package campuslite.ui;

import campuslite.dominio.*;
import campuslite.persistencia.GestorCampus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

/**
 * Pantalla de reportes con 3 vistas:
 *   Tab 1 — Por curso: todos los estudiantes y sus promedios en un curso.
 *   Tab 2 — Por estudiante: todos los cursos y notas de un estudiante.
 *   Tab 3 — Por salón (resumen): ranking general con aprobados/reprobados.
 */
public class VentanaReporte extends JFrame {

    private final GestorCampus gestor = GestorCampus.getInstancia();

    public VentanaReporte() {
        setTitle("Campus Lite — Reportes");
        setSize(900, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("📊 Por curso",       crearTabPorCurso());
        tabs.addTab("👤 Por estudiante",  crearTabPorEstudiante());
        tabs.addTab("🏫 Por salón",       crearTabPorSalon());

        setContentPane(tabs);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  TAB 1 — REPORTE POR CURSO
    // ══════════════════════════════════════════════════════════════════════

    private JPanel crearTabPorCurso() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.add(new JLabel("Curso:"));
        JComboBox<String> cbCurso = new JComboBox<>();
        for (Curso c : gestor.getCursos())
            cbCurso.addItem(c.getCodigo() + " — " + c.getNombre());
        toolbar.add(cbCurso);

        JButton btnGenerar = new JButton("Generar reporte");
        toolbar.add(btnGenerar);

        // Tabla dinámica (columnas dependen del curso)
        DefaultTableModel modelo = new DefaultTableModel() {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(22);
        aplicarRendererEstado(tabla);

        btnGenerar.addActionListener(e -> {
            String sel = (String) cbCurso.getSelectedItem();
            if (sel == null) return;
            String codigo = sel.split(" — ")[0].trim();
            generarReportePorCurso(codigo, modelo, tabla);
        });

        // Resumen al pie
        JLabel lblResumen = new JLabel(" ");
        lblResumen.setFont(lblResumen.getFont().deriveFont(Font.BOLD, 11f));

        btnGenerar.addActionListener(e -> {
            // Calcular stats después de recargar
            SwingUtilities.invokeLater(() -> {
                String sel = (String) cbCurso.getSelectedItem();
                if (sel == null) return;
                String codigo = sel.split(" — ")[0].trim();
                List<Inscripcion> ins = gestor.getInscripcionesPorCurso(codigo);
                long aprobados = ins.stream().filter(Inscripcion::estaAprobado).count();
                lblResumen.setText("  Total: " + ins.size() +
                    "  |  Aprobados: " + aprobados +
                    "  |  Reprobados: " + (ins.size() - aprobados));
            });
        });

        panel.add(toolbar,                 BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla),  BorderLayout.CENTER);
        panel.add(lblResumen,              BorderLayout.SOUTH);
        return panel;
    }

    private void generarReportePorCurso(String codigoCurso,
                                         DefaultTableModel modelo, JTable tabla) {
        Curso curso = gestor.buscarCursoPorCodigo(codigoCurso);
        if (curso == null) return;

        // Construir columnas dinámicas según evaluaciones
        modelo.setRowCount(0);
        modelo.setColumnCount(0);
        modelo.addColumn("Carnet");
        modelo.addColumn("Nombre");
        for (Evaluacion ev : curso.getEvaluaciones())
            modelo.addColumn(ev.getNombre() + "\n(" + (int)ev.getPonderacion() + "%)");
        modelo.addColumn("Promedio");
        modelo.addColumn("Estado");

        for (Inscripcion ins : gestor.getInscripcionesPorCurso(codigoCurso)) {
            Object[] fila = new Object[2 + curso.getEvaluaciones().size() + 2];
            fila[0] = ins.getEstudiante().getCarnet();
            fila[1] = ins.getEstudiante().getNombreCompleto();
            int col = 2;
            for (Evaluacion ev : curso.getEvaluaciones()) {
                double nota = ins.getNota(ev.getNombre());
                fila[col++] = nota >= 0 ? String.format("%.1f", nota) : "—";
            }
            double prom = ins.calcularPromedioFinal();
            fila[col]   = String.format("%.2f", prom);
            fila[col+1] = ins.estaAprobado() ? "Aprobado" : "Reprobado";
            modelo.addRow(fila);
        }

        // Ajustar ancho de columnas
        tabla.getColumnModel().getColumn(1).setPreferredWidth(160);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  TAB 2 — REPORTE POR ESTUDIANTE
    // ══════════════════════════════════════════════════════════════════════

    private JPanel crearTabPorEstudiante() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.add(new JLabel("Estudiante:"));
        JComboBox<String> cbEst = new JComboBox<>();
        for (Estudiante e : gestor.getEstudiantes())
            cbEst.addItem(e.getCarnet() + " — " + e.getNombreCompleto());
        toolbar.add(cbEst);
        JButton btnGenerar = new JButton("Generar reporte");
        toolbar.add(btnGenerar);

        // Tabla: un renglón por curso
        String[] cols = {"Curso", "Evaluación", "Tipo", "Ponderación", "Nota", "Aporte al promedio"};
        DefaultTableModel modelo = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(22);

        // Panel resumen debajo
        JPanel panelResumen = new JPanel(new GridLayout(1, 3, 12, 0));
        JLabel lblCursos    = new JLabel("Cursos: —", SwingConstants.CENTER);
        JLabel lblPromProm  = new JLabel("Prom. general: —", SwingConstants.CENTER);
        JLabel lblAprobados = new JLabel("Aprobados: —", SwingConstants.CENTER);
        panelResumen.add(lblCursos); panelResumen.add(lblPromProm); panelResumen.add(lblAprobados);
        panelResumen.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            new EmptyBorder(6, 8, 6, 8)));

        btnGenerar.addActionListener(e -> {
            String sel = (String) cbEst.getSelectedItem();
            if (sel == null) return;
            String carnet = sel.split(" — ")[0].trim();

            modelo.setRowCount(0);
            List<Inscripcion> inscripciones = gestor.getInscripcionesPorEstudiante(carnet);
            double sumaProm = 0;
            int aprobados = 0;

            for (Inscripcion ins : inscripciones) {
                Curso curso = ins.getCurso();
                for (Evaluacion ev : curso.getEvaluaciones()) {
                    double nota  = ins.getNota(ev.getNombre());
                    double aporte = nota >= 0 ? nota * ev.getPonderacion() / 100.0 : 0;
                    String tipo  = ev instanceof Laboratorio ? "Laboratorio"
                                 : ev instanceof Proyecto    ? "Proyecto"
                                 : "Examen Escrito";
                    modelo.addRow(new Object[]{
                        curso.getCodigo() + " " + curso.getNombre(),
                        ev.getNombre(),
                        tipo,
                        (int)ev.getPonderacion() + "%",
                        nota >= 0 ? String.format("%.1f", nota) : "—",
                        nota >= 0 ? String.format("%.2f", aporte) : "—"
                    });
                }
                // Separador entre cursos
                modelo.addRow(new Object[]{"", "", "", "", "", ""});

                double prom = ins.calcularPromedioFinal();
                sumaProm += prom;
                if (ins.estaAprobado()) aprobados++;
            }

            lblCursos   .setText("Cursos inscritos: " + inscripciones.size());
            lblAprobados.setText("Aprobados: " + aprobados + " / " + inscripciones.size());
            lblPromProm .setText(inscripciones.isEmpty() ? "Prom. general: —" :
                "Prom. general: " + String.format("%.2f", sumaProm / inscripciones.size()));
        });

        panel.add(toolbar,                BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        panel.add(panelResumen,           BorderLayout.SOUTH);
        return panel;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  TAB 3 — REPORTE POR SALÓN (TODOS LOS CURSOS)
    // ══════════════════════════════════════════════════════════════════════

    private JPanel crearTabPorSalon() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JComboBox<String> cbFiltro = new JComboBox<>(new String[]{"Todos", "Solo aprobados", "Solo reprobados"});
        toolbar.add(new JLabel("Filtro:"));
        toolbar.add(cbFiltro);
        JButton btnGenerar = new JButton("Generar reporte de salón");
        toolbar.add(btnGenerar);

        String[] cols = {"Carnet", "Nombre estudiante", "Curso", "Promedio", "Estado"};
        DefaultTableModel modelo = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(22);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(170);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(200);
        aplicarRendererEstado(tabla);

        // Estadísticas
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        JLabel[] stats = {
            new JLabel("Total registros: —", SwingConstants.CENTER),
            new JLabel("Aprobados: —",       SwingConstants.CENTER),
            new JLabel("Reprobados: —",      SwingConstants.CENTER),
            new JLabel("Prom. general: —",   SwingConstants.CENTER)
        };
        for (JLabel s : stats) {
            s.setFont(s.getFont().deriveFont(Font.BOLD, 11f));
            statsPanel.add(s);
        }
        statsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            new EmptyBorder(8, 8, 8, 8)));

        btnGenerar.addActionListener(e -> {
            modelo.setRowCount(0);
            String filtro = (String) cbFiltro.getSelectedItem();
            int total = 0, aprobados = 0;
            double sumaProm = 0;

            for (Inscripcion ins : gestor.getInscripciones()) {
                boolean aprobado = ins.estaAprobado();
                if ("Solo aprobados".equals(filtro)  && !aprobado) continue;
                if ("Solo reprobados".equals(filtro) &&  aprobado) continue;

                double prom = ins.calcularPromedioFinal();
                modelo.addRow(new Object[]{
                    ins.getEstudiante().getCarnet(),
                    ins.getEstudiante().getNombreCompleto(),
                    ins.getCurso().getCodigo() + " — " + ins.getCurso().getNombre(),
                    String.format("%.2f", prom),
                    aprobado ? "Aprobado" : "Reprobado"
                });
                total++;
                if (aprobado) aprobados++;
                sumaProm += prom;
            }

            stats[0].setText("Total: " + total);
            stats[1].setText("Aprobados: " + aprobados);
            stats[2].setText("Reprobados: " + (total - aprobados));
            stats[3].setText(total > 0
                ? "Prom: " + String.format("%.2f", sumaProm / total) : "Prom: —");
        });

        panel.add(toolbar,                BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        panel.add(statsPanel,             BorderLayout.SOUTH);
        return panel;
    }

    // ── Renderer: colorea la columna Estado ──────────────────────────────────
    private void aplicarRendererEstado(JTable tabla) {
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                if (!sel) {
                    String txt = val == null ? "" : val.toString();
                    if      ("Aprobado".equals(txt))  c.setForeground(new Color(0, 130, 0));
                    else if ("Reprobado".equals(txt)) c.setForeground(Color.RED);
                    else                              c.setForeground(Color.BLACK);
                }
                return c;
            }
        });
    }
}