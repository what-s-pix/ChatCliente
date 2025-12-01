package ui.login;

import models.Usuario;
import ui.chat.ChatUI;
import ui.chat.VentanaChat;
import ui.recuperar.RecuperarContrasenaUI;
import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;

public class LoginUI extends JFrame {
    
    private LoginFormPanel formPanel;
    private LoginHandler loginHandler;
    private RegistroHandler registroHandler;
    
    public LoginUI() {
        super("acceso al chat");
        configurarVentana();
        inicializarComponentes();
    }
    
    private void configurarVentana() {
        setSize(350, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));
    }
    
    private void inicializarComponentes() {
        formPanel = new LoginFormPanel();
        loginHandler = new LoginHandler(formPanel);
        registroHandler = new RegistroHandler();
        
        loginHandler.setCallback(new LoginHandler.LoginCallback() {
            @Override
            public void onLoginExitoso(Usuario logueado) {
                SwingUtilities.invokeLater(() -> {
                    new ChatUI(logueado).setVisible(true);
                    dispose();
                });
            }
            
            @Override
            public void onMaxIntentosAlcanzados() {
                SwingUtilities.invokeLater(() -> {
                    registroHandler.hacerRegistro(LoginUI.this);
                });
            }
        });
        
        JPanel panelBotones = new JPanel();
        panelBotones.setLayout(new FlowLayout());
        
        JButton btnLogin = new JButton("entrar");
        btnLogin.addActionListener(e -> loginHandler.hacerLogin());
        
        JButton btnRegistro = new JButton("registrarse");
        btnRegistro.addActionListener(e -> registroHandler.hacerRegistro(this));
        
        JButton btnRecuperar = new JButton("recuperar contrasena");
        btnRecuperar.addActionListener(e -> {
            RecuperarContrasenaUI recuperar = new RecuperarContrasenaUI(this);
            recuperar.setVisible(true);
        });
        
        JButton btnEntrarDirecto = new JButton("entrar directamente al chat");
        btnEntrarDirecto.addActionListener(e -> entrarDirectoAlChat());
        btnEntrarDirecto.setForeground(new Color(0, 100, 0));
        
        panelBotones.add(btnLogin);
        panelBotones.add(btnRegistro);
        panelBotones.add(btnRecuperar);
        
        JPanel panelDirecto = new JPanel(new FlowLayout());
        panelDirecto.add(btnEntrarDirecto);
        
        add(formPanel, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
        add(panelDirecto, BorderLayout.NORTH);
    }
    
    private void entrarDirectoAlChat() {
        Usuario usuarioPrueba = new Usuario(1, "usuario prueba", "test", 1, false);
        SwingUtilities.invokeLater(() -> {
            ChatUI chatUI = new ChatUI(usuarioPrueba);
            chatUI.setVisible(true);
            
            // abrir ventana de ejemplo chat individual
            Usuario contactoEjemplo = new Usuario(2, "contacto ejemplo", "ejemplo", 1, false);
            VentanaChat ventanaPersona = new VentanaChat(usuarioPrueba, contactoEjemplo);
            ventanaPersona.setTitle("chat con contacto ejemplo");
            ventanaPersona.setLocation(100, 100);
            ventanaPersona.setVisible(true);
            
            // abrir ventana de ejemplo chat grupo
            VentanaChat ventanaGrupo = new VentanaChat(usuarioPrueba, 1, "grupo ejemplo");
            ventanaGrupo.setLocation(750, 100);
            ventanaGrupo.setVisible(true);
            
            // agregar mensajes de ejemplo
            agregarMensajesEjemplo(ventanaPersona, ventanaGrupo);
            
            dispose();
        });
    }
    
    private void agregarMensajesEjemplo(VentanaChat ventanaPersona, VentanaChat ventanaGrupo) {
        // mensajes para chat individual
        javax.swing.Timer timer1 = new javax.swing.Timer(500, e -> {
            ventanaPersona.recibirMensajeEjemplo("hola, como estas?");
        });
        timer1.setRepeats(false);
        timer1.start();
        
        // mensajes para chat grupo
        javax.swing.Timer timer2 = new javax.swing.Timer(1000, e -> {
            ventanaGrupo.recibirMensajeEjemplo("mensaje de ejemplo en grupo");
        });
        timer2.setRepeats(false);
        timer2.start();
    }
}

