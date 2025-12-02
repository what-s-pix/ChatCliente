package chatcliente;
import javax.swing.SwingUtilities;
public class ChatCliente {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginUI().setVisible(true);
        });
    }
}
