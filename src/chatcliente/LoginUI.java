package chatcliente;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import common.Peticion;
import models.Usuario;
import java.io.IOException;
import javax.swing.JOptionPane;

public class LoginUI extends JFrame {

    // Componentes de la interfaz
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnRegistro;

    public LoginUI() {
        super("Acceso al Chat");
        configurarVentana();
        inicializarComponentes();
    }

    private void configurarVentana() {
        setSize(350, 200); // Tamaño ancho x alto
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Cerrar app al salir
        setLocationRelativeTo(null); // Centrar en pantalla
        setResizable(false); // No permitir cambiar tamaño
        setLayout(new BorderLayout(10, 10)); // Layout principal con márgenes
    }

    private void inicializarComponentes() {
        // --- PANEL CENTRAL (Formulario) ---
        JPanel panelFormulario = new JPanel();
        panelFormulario.setLayout(new GridLayout(3, 2, 5, 10)); // 3 filas, 2 columnas, espacios
        panelFormulario.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20)); // Margen interno

        // Fila 1: Usuario
        panelFormulario.add(new JLabel("Usuario:"));
        txtUsername = new JTextField();
        panelFormulario.add(txtUsername);

        // Fila 2: Password
        panelFormulario.add(new JLabel("Contraseña:"));
        txtPassword = new JPasswordField();
        panelFormulario.add(txtPassword);

        // --- PANEL INFERIOR (Botones) ---
        JPanel panelBotones = new JPanel();
        panelBotones.setLayout(new FlowLayout());
        
        btnLogin = new JButton("Entrar");
        btnRegistro = new JButton("Registrarse");
        
        
        
        panelBotones.add(btnLogin);
        panelBotones.add(btnRegistro);

        // Agregamos paneles a la ventana
        add(panelFormulario, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);

        // --- EVENTOS (ACCIONES) ---
        
        // Acción del botón LOGIN
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hacerLogin();
            }
        });

        // Acción del botón REGISTRO
        btnRegistro.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hacerRegistro();
            }
        });
    }

    // --- MÉTODOS LÓGICOS (Aún vacíos) ---
    
    private void hacerLogin() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword()).trim();

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor llena todos los campos.");
            return;
        }

        try {
            Cliente.getInstance().conectar();

            Usuario u = new Usuario(null, user, pass);
            Peticion p = new Peticion("LOGIN", u);

            Cliente.getInstance().enviar(p);
            Peticion respuesta = Cliente.getInstance().recibir();

            if (respuesta.getAccion().equals("LOGIN_OK")) {
                Usuario logueado = (Usuario) respuesta.getDatos();
                JOptionPane.showMessageDialog(this, "¡Bienvenido " + logueado.getNombre() + "!");
                
                // Abrir ventana del chat
                chatcliente.ChatUI chatUI = new chatcliente.ChatUI(logueado);
                chatUI.setVisible(true);
                this.dispose();
                
            } else if (respuesta.getAccion().equals("LOGIN_BLOQUEADO")) {
                JOptionPane.showMessageDialog(this, "CUENTA BLOQUEADA: " + respuesta.getDatos() + "\n\nSerás redirigido a recuperar tu contraseña.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                // Redirigir automáticamente a recuperación
                recuperarContrasena();
            } else {
                String mensajeError = respuesta.getDatos().toString();
                JOptionPane.showMessageDialog(this, "Login fallido: " + mensajeError, "Error", JOptionPane.WARNING_MESSAGE);
                
                // Si tiene 3 intentos, redirigir a recuperación
                if (mensajeError.contains("3")) {
                    int opcion = JOptionPane.showConfirmDialog(this, 
                        "Has alcanzado el límite de intentos.\n¿Deseas recuperar tu contraseña?", 
                        "Límite de intentos", JOptionPane.YES_NO_OPTION);
                    if (opcion == JOptionPane.YES_OPTION) {
                        recuperarContrasena();
                    }
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error de conexión: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    private void recuperarContrasena() {
        JTextField fieldUser = new JTextField();
        JPasswordField fieldPass = new JPasswordField();
        JPasswordField fieldPassConfirm = new JPasswordField();

        Object[] message = {
            "Usuario:", fieldUser,
            "Nueva Contraseña:", fieldPass,
            "Confirmar Contraseña:", fieldPassConfirm
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Recuperar Contraseña", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String user = fieldUser.getText().trim();
            String pass = new String(fieldPass.getPassword()).trim();
            String passConfirm = new String(fieldPassConfirm.getPassword()).trim();

            if (user.isEmpty() || pass.isEmpty() || passConfirm.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.");
                return;
            }

            if (!pass.equals(passConfirm)) {
                JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden.");
                return;
            }

            try {
                Cliente.getInstance().conectar();
                String[] datos = {user, pass};
                Peticion p = new Peticion("RECUPERAR_CONTRASENA", datos);

                Cliente.getInstance().enviar(p);
                Peticion respuesta = Cliente.getInstance().recibir();

                if (respuesta.getAccion().equals("RECUPERAR_OK")) {
                    JOptionPane.showMessageDialog(this, "Contraseña recuperada exitosamente. Ahora puedes iniciar sesión.");
                    txtUsername.setText(user);
                    txtPassword.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Error: " + respuesta.getDatos());
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    private void hacerRegistro() {
        // Panel con opción de recuperar cuenta
        JPanel panel = new JPanel(new BorderLayout());
        JPanel panelCampos = new JPanel(new GridLayout(3, 2, 5, 5));
        
        JTextField fieldNombre = new JTextField();
        JTextField fieldUser = new JTextField();
        JPasswordField fieldPass = new JPasswordField();
        
        panelCampos.add(new JLabel("Nombre Completo:"));
        panelCampos.add(fieldNombre);
        panelCampos.add(new JLabel("Nuevo Usuario:"));
        panelCampos.add(fieldUser);
        panelCampos.add(new JLabel("Contraseña:"));
        panelCampos.add(fieldPass);
        
        JButton btnRecuperar = new JButton("¿Ya tienes cuenta? Recuperar contraseña");
        btnRecuperar.addActionListener(e -> {
            recuperarContrasena();
        });
        
        panel.add(panelCampos, BorderLayout.CENTER);
        panel.add(btnRecuperar, BorderLayout.SOUTH);

        int option = JOptionPane.showConfirmDialog(this, panel, "Crear Cuenta", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String nombre = fieldNombre.getText().trim();
            String user = fieldUser.getText().trim();
            String pass = new String(fieldPass.getPassword()).trim();

            if (nombre.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.");
                return;
            }

            try {
                Cliente.getInstance().conectar();
                Usuario uNuevo = new Usuario(nombre, user, pass);
                Peticion p = new Peticion("REGISTRO", uNuevo);

                Cliente.getInstance().enviar(p);
                Peticion respuesta = Cliente.getInstance().recibir();

                if (respuesta.getAccion().equals("REGISTRO_OK")) {
                    JOptionPane.showMessageDialog(this, "Registro exitoso. Ahora puedes entrar.");
                    txtUsername.setText(user);
                    txtPassword.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Error: " + respuesta.getDatos());
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    
}