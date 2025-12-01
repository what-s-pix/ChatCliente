package chatcliente;
import common.Peticion;
import models.Grupo;
import models.Mensaje;
import models.Usuario;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
public class ChatGrupoUI extends JFrame {
    private Usuario miUsuario;
    private Grupo grupo;
    private JTextArea txtAreaChat;
    private JTextField txtMensaje;
    private JButton btnEnviar;
    public ChatGrupoUI(Usuario miUsuario, Grupo grupo) {
        super("Grupo: " + grupo.getTitulo());
        this.miUsuario = miUsuario;
        this.grupo = grupo;
        configurarVentana();
        inicializarComponentes();
        pedirHistorial();
    }
    private void configurarVentana() {
        setSize(450, 550);
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
        txtAreaChat.setBackground(new Color(240, 248, 255));
        add(new JScrollPane(txtAreaChat), BorderLayout.CENTER);
        JPanel panelSur = new JPanel(new BorderLayout(5, 5));
        panelSur.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        txtMensaje = new JTextField();
        btnEnviar = new JButton("Enviar");
        panelSur.add(txtMensaje, BorderLayout.CENTER);
        panelSur.add(btnEnviar, BorderLayout.EAST);
        add(panelSur, BorderLayout.SOUTH);
        btnEnviar.addActionListener(e -> enviarMensaje());
        txtMensaje.addActionListener(e -> enviarMensaje());
    }
    private void pedirHistorial() {
        try {
            Cliente.getInstance().enviar(new Peticion("PEDIR_HISTORIAL_GRUPO", grupo.getId()));
        } catch (Exception e) { e.printStackTrace(); }
    }
    public void cargarHistorial(ArrayList<Mensaje> historial) {
        SwingUtilities.invokeLater(() -> {
            txtAreaChat.setText("");
            for (Mensaje m : historial) {
                mostrarMensaje(m);
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
        Mensaje m = new Mensaje();
        m.setFk_remitente(miUsuario.getPk_usuario());
        m.setFk_destinatario(grupo.getPk_grupo());
        m.setMensaje(texto);
        m.setNombreRemitente(miUsuario.getUsername());
        txtAreaChat.append("[Yo]: " + texto + "\n");
        try {
            Cliente.getInstance().enviar(new Peticion("ENVIAR_MENSAJE_GRUPO", m));
            txtMensaje.setText("");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}