package ui.autenticacion;

import models.Usuario;
import ui.conversacion.ventana_principal_chat;
import ui.recuperar_contrasena.ventana_recuperar_contrasena;
import javax.swing.*;
import java.awt.*;

public class ventana_login extends JFrame {
    
    private panel_formulario_login formPanel;
    private manejador_login loginHandler;
    private manejador_registro registroHandler;
    
    public ventana_login() {
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
        formPanel = new panel_formulario_login();
        loginHandler = new manejador_login(formPanel);
        registroHandler = new manejador_registro();
        
        loginHandler.setCallback(new manejador_login.LoginCallback() {
            @Override
            public void onLoginExitoso(Usuario logueado) {
                System.out.println("[DEBUG] Callback onLoginExitoso() llamado con usuario: " + (logueado != null ? logueado.getNombre() : "null"));
                SwingUtilities.invokeLater(() -> {
                    System.out.println("[DEBUG] Creando ventana_principal_chat...");
                    try {
                        new ventana_principal_chat(logueado).setVisible(true);
                        System.out.println("[DEBUG] Ventana principal creada y visible!");
                        dispose();
                        System.out.println("[DEBUG] Ventana de login cerrada!");
                    } catch (Exception e) {
                        System.err.println("[ERROR] Error al crear ventana principal: " + e.getMessage());
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(null, 
                            "Error al abrir la ventana principal: " + e.getMessage(), 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                    }
                });
            }
            
            @Override
            public void onMaxIntentosAlcanzados() {
                SwingUtilities.invokeLater(() -> {
                    registroHandler.hacerRegistro(ventana_login.this);
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
            ventana_recuperar_contrasena recuperar = new ventana_recuperar_contrasena(this);
            recuperar.setVisible(true);
        });
        
        panelBotones.add(btnLogin);
        panelBotones.add(btnRegistro);
        panelBotones.add(btnRecuperar);
        
        add(formPanel, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }
}

