package campuslite.ui;

import javax.swing.SwingUtilities;

/**
 * Punto de entrada de Campus Lite.
 * SwingUtilities.invokeLater garantiza que la UI se crea en el hilo de Swing (EDT).
 *
 * Para ejecutar desde Eclipse:
 *   Clic derecho en este archivo → Run As → Java Application
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
        });
    }
}