package campuslite.ui;

import campuslite.dominio.Maestro;
import campuslite.persistencia.GestorCampus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Pantalla CRUD de maestros.
 */
public class VentanaMaestros extends JFrame {

    private final GestorCampus gestor = GestorCampus.getInstancia();

    private JTextField txtCodigo, txtNombre, txtCorreo, txtEspecialidad;
    private JTable tablaMaestros;
    private DefaultTableModel modeloTabla;
    private String codigoSeleccionado = null;

    public VentanaMaestros() {
        setTitle("Campus Lite — Maestros");
        setSize(820, 480);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initComponentes();
        cargarTabla();
    }

    private void initComponentes() {
        JPanel raiz = new JPanel(new BorderLayout(10, 10));
        raiz.setBorder(new EmptyBorder(14, 14, 14, 14));
        raiz.add(crearFormulario(),  BorderLayout.WEST);
        raiz.add(crearPanelTabla(), BorderLayout.CENTER);
        setContentPane(raiz);
    }

    private JPanel crearFormulario() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setPreferredSize(new Dimension(250, 0));
        p.setBorder(BorderFactory.createTitledBorder("Datos del maestro"));

        p.add(lbl("Código empleado *:")); txtCodigo       = campo(); p.add(txtCodigo);       p.add(strut());
        p.add(lbl("Nombre completo *:")); txtNombre       = campo(); p.add(txtNombre);       p.add(strut());
        p.add(lbl("Correo:"));            txtCorreo       = campo(); p.add(txtCorreo);       p.add(strut());
        p.add(lbl("Especialidad:"));      txtEspecialidad = campo(); p.add(txtEspecialidad); p.add(strut(16));
        p.add(new JSeparator()); p.add(strut(10));

        JButton btnGuardar  = btn("Guardar",         () -> accionGuardar());
        JButton btnLimpiar  = btn("Nuevo / Limpiar", () -> limpiar());
        JButton btnEliminar = btn("Eliminar",        () -> accionEliminar());
        btnEliminar.setForeground(Color.RED);

        p.add(btnGuardar); p.add(strut(6));
        p.add(btnLimpiar); p.add(strut(6));
        p.add(btnEliminar);
        return p;
    }

    private JPanel crearPanelTabla() {
        String[] cols = {"Código", "Nombre", "Correo", "Especialidad", "Cursos asignados"};
        modeloTabla = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaMaestros = new JTable(modeloTabla);
        tablaMaestros.setRowHeight(22);
        tablaMaestros.getColumnModel().getColumn(1).setPreferredWidth(180);
        tablaMaestros.getColumnModel().getColumn(2).setPreferredWidth(150);
        tablaMaestros.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarFila();
        });

        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Lista de maestros"));
        p.add(new JScrollPane(tablaMaestros));
        return p;
    }

    private void accionGuardar() {
        String cod = txtCodigo.getText().trim();
        String nom = txtNombre.getText().trim();
        if (cod.isEmpty() || nom.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Código y nombre son obligatorios.",
                "Campos requeridos", JOptionPane.WARNING_MESSAGE); return;
        }
        try {
            Maestro m = new Maestro(cod, nom, txtCorreo.getText().trim(), txtEspecialidad.getText().trim());
            if (codigoSeleccionado == null) {
                gestor.agregarMaestro(m);
                JOptionPane.showMessageDialog(this, "Maestro agregado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                gestor.actualizarMaestro(codigoSeleccionado, m);
                JOptionPane.showMessageDialog(this, "Maestro actualizado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }
            cargarTabla(); limpiar();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionEliminar() {
        if (codigoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un maestro primero.",
                "Sin selección", JOptionPane.WARNING_MESSAGE); return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
            "¿Eliminar al maestro " + codigoSeleccionado + "?\nLos cursos asignados quedarán sin maestro.",
            "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) { gestor.eliminarMaestro(codigoSeleccionado); cargarTabla(); limpiar(); }
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        for (Maestro m : gestor.getMaestros()) {
            long cursos = gestor.getCursos().stream()
                .filter(c -> c.getMaestro() != null &&
                             c.getMaestro().getCodigoEmpleado().equals(m.getCodigoEmpleado()))
                .count();
            modeloTabla.addRow(new Object[]{
                m.getCodigoEmpleado(), m.getNombreCompleto(),
                m.getCorreo(), m.getEspecialidad(), cursos + " curso(s)"
            });
        }
    }

    private void cargarFila() {
        int fila = tablaMaestros.getSelectedRow();
        if (fila < 0) return;
        codigoSeleccionado = (String) modeloTabla.getValueAt(fila, 0);
        txtCodigo.setText(codigoSeleccionado);
        txtNombre.setText((String) modeloTabla.getValueAt(fila, 1));
        txtCorreo.setText((String) modeloTabla.getValueAt(fila, 2));
        txtEspecialidad.setText((String) modeloTabla.getValueAt(fila, 3));
    }

    private void limpiar() {
        codigoSeleccionado = null;
        txtCodigo.setText(""); txtNombre.setText("");
        txtCorreo.setText(""); txtEspecialidad.setText("");
        tablaMaestros.clearSelection();
    }

    private JLabel     lbl(String t) { JLabel l = new JLabel(t); l.setAlignmentX(Component.LEFT_ALIGNMENT); return l; }
    private JTextField campo()       { JTextField tf = new JTextField(); tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28)); return tf; }
    private Component  strut()       { return Box.createVerticalStrut(8); }
    private Component  strut(int n)  { return Box.createVerticalStrut(n); }
    private JButton    btn(String t, Runnable r) {
        JButton b = new JButton(t); b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        b.setAlignmentX(Component.LEFT_ALIGNMENT); b.addActionListener(e -> r.run()); return b;
    }
}