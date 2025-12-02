package chatcliente;
import ui.autenticacion.ventana_login;
import javax.swing.SwingUtilities;
public class ChatCliente {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ventana_login().setVisible(true);
        });
    }
}