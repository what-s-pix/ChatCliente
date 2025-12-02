package ui.conversacion.componentes;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
public class panel_envio extends JPanel {
    private JTextField campoMensaje;
    private JButton btnEnviar;
    public panel_envio(ActionListener enviarListener) {
        setLayout(new BorderLayout(5, 5));
        campoMensaje = new JTextField();
        campoMensaje.addActionListener(enviarListener);
        btnEnviar = new JButton("Enviar");
        btnEnviar.addActionListener(enviarListener);
        add(campoMensaje, BorderLayout.CENTER);
        add(btnEnviar, BorderLayout.EAST);
    }
    public String getTexto() {
        return campoMensaje.getText().trim();
    }
    public void limpiar() {
        campoMensaje.setText("");
    }
    public void focus() {
        campoMensaje.requestFocus();
    }
}
