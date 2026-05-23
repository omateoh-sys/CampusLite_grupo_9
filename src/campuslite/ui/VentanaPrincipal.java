package campuslite.ui;

import campuslite.persistencia.GestorCampus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Ventana principal de Campus Lite.
 * Navegación a los 6 módulos del sistema.
 */
public class VentanaPrincipal extends JFrame {

    private final GestorCampus gestor = GestorCampus.getInstancia();

    private JLabel lblNumEstudiantes;
    private JLabel lblNumCursos;
    private JLabel lblNumMaestros;

    public VentanaPrincipal() {
        setTitle("Campus Lite — Sistema Académico");
        setSize(660, 480);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initComponentes();
    }

    private void initComponentes() {
        JPanel raiz = new JPanel(new BorderLayout(0, 0));
        raiz.setBorder(new EmptyBorder(20, 24, 20, 24));
        raiz.add(crearEncabezado(), BorderLayout.NORTH);
        raiz.add(crearStats(),     BorderLayout.CENTER);
        raiz.add(crearNavegacion(),BorderLayout.SOUTH);
        setContentPane(raiz);
    }

    private JPanel crearEncabezado() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(0, 0, 16, 0));
        JLabel titulo = new JLabel("Campus Lite");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));
        JLabel sub = new JLabel("Sistema de Gestión Académica");
        sub.setFont(sub.getFont().deriveFont(12f));
        sub.setForeground(Color.GRAY);
        JPanel tx = new JPanel();
        tx.setLayout(new BoxLayout(tx, BoxLayout.Y_AXIS));
        tx.add(titulo); tx.add(sub);
        p.add(tx, BorderLayout.WEST);
        return p;
    }

    private JPanel crearStats() {
        JPanel p = new JPanel(new GridLayout(1, 3, 12, 0));
        p.setBorder(new EmptyBorder(0, 0, 20, 0));
        lblNumEstudiantes = new JLabel(str(gestor.getEstudiantes().size()), SwingConstants.CENTER);
        lblNumCursos      = new JLabel(str(gestor.getCursos().size()),      SwingConstants.CENTER);
        lblNumMaestros    = new JLabel(str(gestor.getMaestros().size()),    SwingConstants.CENTER);
        p.add(tarjeta(lblNumEstudiantes, "Estudiantes"));
        p.add(tarjeta(lblNumCursos,      "Cursos activos"));
        p.add(tarjeta(lblNumMaestros,    "Maestros"));
        return p;
    }

    private JPanel tarjeta(JLabel num, String desc) {
        JPanel t = new JPanel();
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            new EmptyBorder(14, 12, 14, 12)));
        num.setFont(num.getFont().deriveFont(Font.BOLD, 34f));
        num.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel d = new JLabel(desc, SwingConstants.CENTER);
        d.setFont(d.getFont().deriveFont(11f));
        d.setForeground(Color.GRAY);
        d.setAlignmentX(Component.CENTER_ALIGNMENT);
        t.add(num); t.add(Box.createVerticalStrut(4)); t.add(d);
        return t;
    }

    private JPanel crearNavegacion() {
        // 2 filas × 3 columnas = 6 botones
        JPanel p = new JPanel(new GridLayout(2, 3, 10, 10));

        p.add(boton("👤  Estudiantes",
            "Crear, editar y eliminar",
            e -> abrir(new VentanaEstudiantes())));

        p.add(boton("🎓  Maestros",
            "Gestionar docentes",
            e -> abrir(new VentanaMaestros())));

        p.add(boton("📚  Cursos y Evaluaciones",
            "Cursos, evaluaciones, inscripciones",
            e -> abrir(new VentanaCursos())));

        p.add(boton("📝  Notas",
            "Registrar notas por estudiante",
            e -> abrir(new VentanaNotas())));

        p.add(boton("📊  Reportes",
            "Por curso, estudiante y salón",
            e -> abrir(new VentanaReporte())));

        p.add(boton("📋  Bitácora",
            "Historial de todos los cambios",
            e -> abrir(new VentanaBitacora())));

        return p;
    }

    private JButton boton(String titulo, String desc,
                          java.awt.event.ActionListener l) {
        JButton b = new JButton(
            "<html><b>" + titulo + "</b><br>"
            + "<font size='2' color='gray'>" + desc + "</font></html>");
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setMargin(new Insets(10, 12, 10, 12));
        b.addActionListener(l);
        return b;
    }

    private void abrir(JFrame v) {
        v.setVisible(true);
        v.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent e) {
                actualizarStats();
            }
        });
    }

    public void actualizarStats() {
        lblNumEstudiantes.setText(str(gestor.getEstudiantes().size()));
        lblNumCursos     .setText(str(gestor.getCursos().size()));
        lblNumMaestros   .setText(str(gestor.getMaestros().size()));
    }

    private String str(int n) { return String.valueOf(n); }
}