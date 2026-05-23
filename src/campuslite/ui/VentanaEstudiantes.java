package campuslite.ui;

import campuslite.dominio.Estudiante;
import campuslite.persistencia.GestorCampus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Pantalla de gestión de estudiantes.
 * Permite crear, editar y eliminar estudiantes con validaciones visibles.
 *
 * Componentes Swing usados:
 *   JPanel, JLabel, JTextField, JButton, JTable, JScrollPane,
 *   JOptionPane, DefaultTableModel, ListSelectionListener.
 */
public class VentanaEstudiantes extends JFrame {

    // ── Referencias al modelo ─────────────────────────────────────────────
    private final GestorCampus gestor = GestorCampus.getInstancia();

    // ── Campos del formulario ─────────────────────────────────────────────
    private JTextField txtCarnet;
    private JTextField txtNombre;
    private JTextField txtCorreo;

    // ── Tabla ─────────────────────────────────────────────────────────────
    private JTable tablaEstudiantes;
    private DefaultTableModel modeloTabla;

    // ── Estado interno ────────────────────────────────────────────────────
    /** Carnet del estudiante seleccionado actualmente (null = modo nuevo). */
    private String carnetSeleccionado = null;

    // ─────────────────────────────────────────────────────────────────────
    //  CONSTRUCTOR
    // ─────────────────────────────────────────────────────────────────────

    public VentanaEstudiantes() {
        setTitle("Campus Lite — Estudiantes");
        setSize(800, 520);
        setLocationRelativeTo(null);          // centrar en pantalla
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initComponentes();
        cargarTabla();
    }

    // ─────────────────────────────────────────────────────────────────────
    //  CONSTRUCCIÓN DE LA UI
    // ─────────────────────────────────────────────────────────────────────

    private void initComponentes() {
        // Panel raíz con dos columnas: formulario | tabla
        JPanel panelRaiz = new JPanel(new BorderLayout(10, 10));
        panelRaiz.setBorder(new EmptyBorder(14, 14, 14, 14));

        panelRaiz.add(crearPanelFormulario(), BorderLayout.WEST);
        panelRaiz.add(crearPanelTabla(),      BorderLayout.CENTER);

        setContentPane(panelRaiz);
    }

    /** Formulario izquierdo: campos + botones. */
    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(260, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Datos del estudiante"));

        // ── Carnet ───────────────────────────────────────────────────────
        panel.add(crearEtiqueta("Carnet * (8 dígitos):"));
        txtCarnet = new JTextField();
        txtCarnet.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        panel.add(txtCarnet);
        panel.add(Box.createVerticalStrut(8));

        // ── Nombre ───────────────────────────────────────────────────────
        panel.add(crearEtiqueta("Nombre completo *:"));
        txtNombre = new JTextField();
        txtNombre.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        panel.add(txtNombre);
        panel.add(Box.createVerticalStrut(8));

        // ── Correo ───────────────────────────────────────────────────────
        panel.add(crearEtiqueta("Correo (opcional):"));
        txtCorreo = new JTextField();
        txtCorreo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        panel.add(txtCorreo);
        panel.add(Box.createVerticalStrut(16));

        // ── Separador ────────────────────────────────────────────────────
        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(12));

        // ── Botones ───────────────────────────────────────────────────────
        JButton btnGuardar  = new JButton("Guardar");
        JButton btnLimpiar  = new JButton("Nuevo / Limpiar");
        JButton btnEliminar = new JButton("Eliminar");

        btnGuardar .setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLimpiar .setAlignmentX(Component.CENTER_ALIGNMENT);
        btnEliminar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnEliminar.setForeground(Color.RED);

        btnGuardar .setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        btnLimpiar .setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        btnEliminar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        panel.add(btnGuardar);
        panel.add(Box.createVerticalStrut(6));
        panel.add(btnLimpiar);
        panel.add(Box.createVerticalStrut(6));
        panel.add(btnEliminar);

        // ── Listeners ─────────────────────────────────────────────────────
        btnGuardar .addActionListener(e -> accionGuardar());
        btnLimpiar .addActionListener(e -> limpiarFormulario());
        btnEliminar.addActionListener(e -> accionEliminar());

        return panel;
    }

    /** Panel derecho: título + tabla con scroll. */
    private JPanel crearPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Lista de estudiantes"));

        // ── Modelo de tabla (no editable directamente) ────────────────────
        String[] columnas = {"Carnet", "Nombre completo", "Correo"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // edición solo a través del formulario
            }
        };

        tablaEstudiantes = new JTable(modeloTabla);
        tablaEstudiantes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaEstudiantes.setRowHeight(22);
        tablaEstudiantes.getTableHeader().setReorderingAllowed(false);

        // Anchos de columna sugeridos
        tablaEstudiantes.getColumnModel().getColumn(0).setPreferredWidth(90);
        tablaEstudiantes.getColumnModel().getColumn(1).setPreferredWidth(200);
        tablaEstudiantes.getColumnModel().getColumn(2).setPreferredWidth(160);

        // Clic en fila → carga en formulario
        tablaEstudiantes.getSelectionModel().addListSelectionListener(
            new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {
                        cargarFilaEnFormulario();
                    }
                }
            }
        );

        panel.add(new JScrollPane(tablaEstudiantes), BorderLayout.CENTER);

        // Nota al pie
        JLabel lbHint = new JLabel("Clic en una fila para editar · Guardar aplica cambios");
        lbHint.setFont(lbHint.getFont().deriveFont(10f));
        lbHint.setForeground(Color.GRAY);
        panel.add(lbHint, BorderLayout.SOUTH);

        return panel;
    }

    // ─────────────────────────────────────────────────────────────────────
    //  LÓGICA DE ACCIONES  (no mezclar con construcción de UI)
    // ─────────────────────────────────────────────────────────────────────

    /** Guarda un estudiante nuevo o actualiza el seleccionado. */
    private void accionGuardar() {
        String carnet = txtCarnet.getText().trim();
        String nombre = txtNombre.getText().trim();
        String correo = txtCorreo.getText().trim();

        // Validación básica de campos vacíos antes de llegar al dominio
        if (carnet.isEmpty() || nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "El carnet y el nombre son obligatorios.",
                "Campos requeridos",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Estudiante estudiante = new Estudiante(carnet, nombre, correo);

            if (carnetSeleccionado == null) {
                // Modo nuevo
                gestor.agregarEstudiante(estudiante);
                JOptionPane.showMessageDialog(this,
                    "Estudiante agregado correctamente.",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Modo edición
                gestor.actualizarEstudiante(carnetSeleccionado, estudiante);
                JOptionPane.showMessageDialog(this,
                    "Estudiante actualizado correctamente.",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }

            cargarTabla();
            limpiarFormulario();

        } catch (IllegalArgumentException ex) {
            // Muestra el mensaje de validación del dominio (setter o gestor)
            JOptionPane.showMessageDialog(this,
                ex.getMessage(),
                "Error de validación",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Elimina el estudiante seleccionado en la tabla. */
    private void accionEliminar() {
        if (carnetSeleccionado == null) {
            JOptionPane.showMessageDialog(this,
                "Seleccione un estudiante en la tabla primero.",
                "Sin selección", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(this,
            "¿Eliminar al estudiante con carnet " + carnetSeleccionado + "?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            gestor.eliminarEstudiante(carnetSeleccionado);
            cargarTabla();
            limpiarFormulario();
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    //  HELPERS DE UI
    // ─────────────────────────────────────────────────────────────────────

    /** Recarga la tabla completa desde el gestor. */
    private void cargarTabla() {
        modeloTabla.setRowCount(0); // limpiar filas actuales
        List<Estudiante> lista = gestor.getEstudiantes();
        for (Estudiante est : lista) {
            modeloTabla.addRow(new Object[]{
                est.getCarnet(),
                est.getNombreCompleto(),
                est.getCorreo()
            });
        }
    }

    /** Carga los datos de la fila seleccionada en el formulario. */
    private void cargarFilaEnFormulario() {
        int fila = tablaEstudiantes.getSelectedRow();
        if (fila < 0) return;

        carnetSeleccionado = (String) modeloTabla.getValueAt(fila, 0);
        txtCarnet.setText(carnetSeleccionado);
        txtNombre.setText((String) modeloTabla.getValueAt(fila, 1));
        txtCorreo.setText((String) modeloTabla.getValueAt(fila, 2));
    }

    /** Limpia el formulario y vuelve a modo "nuevo". */
    private void limpiarFormulario() {
        carnetSeleccionado = null;
        txtCarnet.setText("");
        txtNombre.setText("");
        txtCorreo.setText("");
        tablaEstudiantes.clearSelection();
        txtCarnet.requestFocus();
    }

    /** Crea una etiqueta con estilo consistente. */
    private JLabel crearEtiqueta(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(lbl.getFont().deriveFont(11f));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }
}