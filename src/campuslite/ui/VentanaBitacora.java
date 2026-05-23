package campuslite.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Ventana que muestra el contenido de datos/bitacora.txt en una tabla
 * con filtro por acción y búsqueda de texto libre.
 *
 * También permite exportar el archivo a otro destino.
 */
public class VentanaBitacora extends JFrame {

    private static final String ARCHIVO_BITACORA = "datos/bitacora.txt";

    private DefaultTableModel modeloTabla;
    private JTable tabla;
    private JComboBox<String> cbFiltroAccion;
    private JTextField txtBuscar;

    // Datos cargados (para filtrar sin releer el archivo)
    private final List<String[]> filasTodas = new ArrayList<>();

    public VentanaBitacora() {
        setTitle("Campus Lite — Bitácora de cambios");
        setSize(1000, 580);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initComponentes();
        cargarBitacora();
    }

    private void initComponentes() {
        JPanel raiz = new JPanel(new BorderLayout(8, 8));
        raiz.setBorder(new EmptyBorder(12, 12, 12, 12));

        // ── Barra de herramientas ────────────────────────────────────────
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

        toolbar.add(new JLabel("Filtrar acción:"));
        cbFiltroAccion = new JComboBox<>(new String[]{
            "Todas", "AGREGAR", "ACTUALIZAR", "ELIMINAR",
            "INSCRIBIR", "NOTA", "INICIO", "RESET"
        });
        cbFiltroAccion.addActionListener(e -> aplicarFiltro());
        toolbar.add(cbFiltroAccion);

        toolbar.add(new JLabel("  Buscar:"));
        txtBuscar = new JTextField(18);
        txtBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate (javax.swing.event.DocumentEvent e) { aplicarFiltro(); }
            public void removeUpdate (javax.swing.event.DocumentEvent e) { aplicarFiltro(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltro(); }
        });
        toolbar.add(txtBuscar);

        JButton btnRefrescar = new JButton("🔄 Actualizar");
        btnRefrescar.addActionListener(e -> cargarBitacora());
        toolbar.add(btnRefrescar);

        JButton btnExportar = new JButton("💾 Exportar copia");
        btnExportar.addActionListener(e -> exportarCopia());
        toolbar.add(btnExportar);

        // ── Tabla ────────────────────────────────────────────────────────
        String[] cols = {"Fecha/Hora", "Acción", "Entidad", "Detalle"};
        modeloTabla = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modeloTabla);
        tabla.setRowHeight(22);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(140);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(85);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(90);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(550);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        // Colorear según acción
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(
                    t, val, sel, focus, row, col);
                if (!sel) {
                    String accion = modeloTabla.getRowCount() > row
                        ? (String) modeloTabla.getValueAt(row, 1) : "";
                    switch (accion == null ? "" : accion.trim()) {
                        case "AGREGAR":    c.setForeground(new Color(0, 120, 0));   break;
                        case "ELIMINAR":   c.setForeground(Color.RED);              break;
                        case "ACTUALIZAR": c.setForeground(new Color(160, 100, 0)); break;
                        case "NOTA":       c.setForeground(new Color(0, 80, 160));  break;
                        case "INSCRIBIR":  c.setForeground(new Color(80, 0, 160));  break;
                        default:           c.setForeground(Color.DARK_GRAY);        break;
                    }
                }
                return c;
            }
        });

        // ── Etiqueta de contador ─────────────────────────────────────────
        JLabel lblTotal = new JLabel(" ");
        lblTotal.setFont(lblTotal.getFont().deriveFont(10f));
        lblTotal.setForeground(Color.GRAY);

        // Actualizar contador al filtrar
        modeloTabla.addTableModelListener(e ->
            lblTotal.setText("  " + modeloTabla.getRowCount() + " registro(s) mostrado(s)"));

        raiz.add(toolbar,                 BorderLayout.NORTH);
        raiz.add(new JScrollPane(tabla),  BorderLayout.CENTER);
        raiz.add(lblTotal,                BorderLayout.SOUTH);
        setContentPane(raiz);
    }

    // ── Carga y filtrado ─────────────────────────────────────────────────────

    /**
     * Lee el archivo de bitácora línea a línea y llena filasTodas.
     * Formato esperado: [fecha] | ACCION     | ENTIDAD      | detalle
     */
    private void cargarBitacora() {
        filasTodas.clear();
        File f = new File(ARCHIVO_BITACORA);
        if (!f.exists()) {
            modeloTabla.setRowCount(0);
            modeloTabla.addRow(new Object[]{
                "—", "—", "—",
                "El archivo datos/bitacora.txt aún no existe. "
                + "Realizá alguna operación para generarlo."
            });
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;
                // Formato: [2024-01-15 10:32:45] | AGREGAR    | ESTUDIANTE   | carnet=...
                // Dividir por " | " máximo 4 partes
                String[] partes = linea.split(" \\| ", 4);
                if (partes.length == 4) {
                    String fecha   = partes[0].replace("[", "").replace("]", "").trim();
                    String accion  = partes[1].trim();
                    String entidad = partes[2].trim();
                    String detalle = partes[3].trim();
                    filasTodas.add(new String[]{fecha, accion, entidad, detalle});
                } else {
                    // Línea con formato inesperado → guardarla completa en detalle
                    filasTodas.add(new String[]{"", "", "", linea});
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "Error al leer la bitácora:\n" + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
        aplicarFiltro();
    }

    /** Filtra filasTodas según combo y campo de búsqueda, y recarga la tabla. */
    private void aplicarFiltro() {
        String filtroAccion = (String) cbFiltroAccion.getSelectedItem();
        String textoBuscar  = txtBuscar.getText().trim().toLowerCase();

        modeloTabla.setRowCount(0);
        for (String[] fila : filasTodas) {
            // Filtro acción
            if (!"Todas".equals(filtroAccion) && !fila[1].trim().equals(filtroAccion))
                continue;
            // Filtro texto libre (busca en toda la fila)
            if (!textoBuscar.isEmpty()) {
                boolean encontrado = false;
                for (String campo : fila)
                    if (campo.toLowerCase().contains(textoBuscar)) { encontrado = true; break; }
                if (!encontrado) continue;
            }
            modeloTabla.addRow(fila);
        }

        // Hacer scroll al final (entrada más reciente)
        if (modeloTabla.getRowCount() > 0) {
            int ultima = modeloTabla.getRowCount() - 1;
            tabla.scrollRectToVisible(tabla.getCellRect(ultima, 0, true));
        }
    }

    // ── Exportar copia ───────────────────────────────────────────────────────

    private void exportarCopia() {
        File origen = new File(ARCHIVO_BITACORA);
        if (!origen.exists()) {
            JOptionPane.showMessageDialog(this,
                "La bitácora aún no existe.",
                "Sin datos", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("bitacora_copia.txt"));
        chooser.setDialogTitle("Guardar copia de bitácora");
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File destino = chooser.getSelectedFile();
            try {
                Files.copy(origen.toPath(), destino.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
                JOptionPane.showMessageDialog(this,
                    "Copia guardada en:\n" + destino.getAbsolutePath(),
                    "Exportación exitosa", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                    "Error al copiar:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}