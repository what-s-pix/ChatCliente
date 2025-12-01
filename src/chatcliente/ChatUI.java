package chatcliente;
import common.Peticion;
import models.Mensaje;
import models.Usuario;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
public class ChatUI extends JFrame {
    private Usuario miUsuario;
    private String usuarioDestino;
    private JTextArea txtAreaChat;
    private JTextField txtMensaje;
    private JButton btnEnviar;
    public ChatUI(Usuario miUsuario, String usuarioDestino) {
        super("Chat con: " + usuarioDestino);
        this.miUsuario = miUsuario;
        this.usuarioDestino = usuarioDestino;
        configurarVentana();
        inicializarComponentes();
        pedirHistorial();
    }
    private void pedirHistorial() {
        try {
            Cliente.getInstance().enviar(new Peticion("PEDIR_HISTORIAL", usuarioDestino));
        } catch (Exception e) { e.printStackTrace(); }
    }
    public void cargarHistorial(java.util.ArrayList<Mensaje> historial) {
        SwingUtilities.invokeLater(() -> {
            txtAreaChat.setText("");
            for (Mensaje m : historial) {
                txtAreaChat.append("[" + m.getRemitente() + "]: " + m.getContenido() + "\n");
            }
            txtAreaChat.setCaretPosition(txtAreaChat.getDocument().getLength());
        });
    }
    private void configurarVentana() {
        setSize(400, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));
    }
    private void inicializarComponentes() {
        txtAreaChat = new JTextArea();
        txtAreaChat.setEditable(false);
        txtAreaChat.setLineWrap(true);
        txtAreaChat.setWrapStyleWord(true);
        txtAreaChat.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(txtAreaChat);
        add(scrollPane, BorderLayout.CENTER);
        JPanel panelInferior = new JPanel();
        panelInferior.setLayout(new BorderLayout(5, 5));
        panelInferior.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        txtMensaje = new JTextField();
        btnEnviar = new JButton("Enviar");
        panelInferior.add(txtMensaje, BorderLayout.CENTER);
        panelInferior.add(btnEnviar, BorderLayout.EAST);
        add(panelInferior, BorderLayout.SOUTH);
        btnEnviar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enviarMensaje();
            }
        });
        txtMensaje.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enviarMensaje();
            }
        });
    }
    public void mostrarMensaje(Mensaje m) {
        SwingUtilities.invokeLater(() -> {
            txtAreaChat.append("[" + m.getRemitente() + "]: " + m.getContenido() + "\n");
            txtAreaChat.setCaretPosition(txtAreaChat.getDocument().getLength());
        });
    }
    private void enviarMensaje() {
        String texto = txtMensaje.getText().trim();
        if (texto.isEmpty()) return;
        Mensaje m = new Mensaje(miUsuario.getUsername(), usuarioDestino, texto);
        txtAreaChat.append("[Yo]: " + texto + "\n");
        try {
            Cliente.getInstance().enviar(new Peticion("ENVIAR_MENSAJE", m));
            txtMensaje.setText("");
        } catch (Exception ex) {
            txtAreaChat.append(">> Error al enviar mensaje <<\n");
            ex.printStackTrace();
        }
    }
}