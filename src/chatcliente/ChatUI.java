package chatcliente;

import common.Peticion;
import models.Mensaje;
import models.Usuario;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatUI extends JFrame {

    // Datos lógicos
    private Usuario miUsuario;
    private String usuarioDestino;

    // Componentes visuales
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

    // NUEVO: Método para cargar múltiples mensajes de golpe
    public void cargarHistorial(java.util.ArrayList<Mensaje> historial) {
        SwingUtilities.invokeLater(() -> {
            txtAreaChat.setText(""); // Limpiamos por si acaso
            for (Mensaje m : historial) {
                txtAreaChat.append("[" + m.getRemitente() + "]: " + m.getContenido() + "\n");
            }
            // Scroll abajo
            txtAreaChat.setCaretPosition(txtAreaChat.getDocument().getLength());
        });
    }

    private void configurarVentana() {
        setSize(400, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Solo cierra esta ventana
        setLayout(new BorderLayout(5, 5)); // Márgenes de 5px
    }

    private void inicializarComponentes() {
        // --- 1. ÁREA DE CHAT (CENTRO) ---
        txtAreaChat = new JTextArea();
        txtAreaChat.setEditable(false); // Solo lectura
        txtAreaChat.setLineWrap(true);  // Ajustar líneas largas
        txtAreaChat.setWrapStyleWord(true);
        txtAreaChat.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        // Scroll para el área de chat
        JScrollPane scrollPane = new JScrollPane(txtAreaChat);
        add(scrollPane, BorderLayout.CENTER);

        // --- 2. PANEL DE ENVÍO (SUR) ---
        JPanel panelInferior = new JPanel();
        panelInferior.setLayout(new BorderLayout(5, 5));
        panelInferior.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Margen interno

        txtMensaje = new JTextField();
        btnEnviar = new JButton("Enviar");

        panelInferior.add(txtMensaje, BorderLayout.CENTER);
        panelInferior.add(btnEnviar, BorderLayout.EAST);

        add(panelInferior, BorderLayout.SOUTH);

        // --- 3. EVENTOS ---
        
        // Acción al hacer clic en el botón
        btnEnviar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enviarMensaje();
            }
        });

        // Acción al presionar ENTER en el campo de texto
        txtMensaje.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enviarMensaje();
            }
        });
    }

    // --- MÉTODOS PÚBLICOS Y LÓGICOS ---

    // Este método lo llama la ListaAmigosUI cuando llega algo del server
    public void mostrarMensaje(Mensaje m) {
        SwingUtilities.invokeLater(() -> {
            txtAreaChat.append("[" + m.getRemitente() + "]: " + m.getContenido() + "\n");
            // Auto-scroll al final
            txtAreaChat.setCaretPosition(txtAreaChat.getDocument().getLength());
        });
    }

    private void enviarMensaje() {
        String texto = txtMensaje.getText().trim();
        if (texto.isEmpty()) return;

        // Crear objeto mensaje
        Mensaje m = new Mensaje(miUsuario.getUsername(), usuarioDestino, texto);

        // Feedback local (lo mostramos en nuestra pantalla)
        txtAreaChat.append("[Yo]: " + texto + "\n");

        // Enviar al servidor
        try {
            Cliente.getInstance().enviar(new Peticion("ENVIAR_MENSAJE", m));
            txtMensaje.setText(""); // Limpiar
        } catch (Exception ex) {
            txtAreaChat.append(">> Error al enviar mensaje <<\n");
            ex.printStackTrace();
        }
    }
}
