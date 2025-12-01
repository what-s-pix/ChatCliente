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
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnRegistro;
    private JButton btnRecuperar;
    public LoginUI() {
        super("Acceso al Chat");
        configurarVentana();
        inicializarComponentes();
    }
    private void configurarVentana() {
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));
    }
    private void inicializarComponentes() {
        JPanel panelFormulario = new JPanel();
        panelFormulario.setLayout(new GridLayout(3, 2, 5, 10));
        panelFormulario.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));
        panelFormulario.add(new JLabel("Usuario:"));
        txtUsername = new JTextField();
        panelFormulario.add(txtUsername);
        panelFormulario.add(new JLabel("Contraseña:"));
        txtPassword = new JPasswordField();
        panelFormulario.add(txtPassword);
        JPanel panelBotones = new JPanel();
        panelBotones.setLayout(new FlowLayout());
        btnLogin = new JButton("Entrar");
        btnRegistro = new JButton("Registrarse");
        btnRecuperar = new JButton("Recuperar Contraseña");
        panelBotones.add(btnLogin);
        panelBotones.add(btnRegistro);
        panelBotones.add(btnRecuperar);
        add(panelFormulario, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hacerLogin();
            }
        });
        btnRegistro.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hacerRegistro();
            }
        });
        btnRecuperar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                recuperarContrasena();
            }
        });
    }
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
                new ListaAmigosUI(logueado).setVisible(true);
                this.dispose();
            } else if (respuesta.getAccion().equals("LOGIN_BLOQUEADO")) {
                JOptionPane.showMessageDialog(this, "CUENTA BLOQUEADA: " + respuesta.getDatos() + "\n\nSerás redirigido a recuperar tu contraseña.",
                    "Error", JOptionPane.ERROR_MESSAGE);
                recuperarContrasena();
            } else {
                String mensajeError = respuesta.getDatos().toString();
                JOptionPane.showMessageDialog(this, "Login fallido: " + mensajeError, "Error", JOptionPane.WARNING_MESSAGE);
                if (mensajeError.contains("3")) {
                    int opcion = JOptionPane.showOptionDialog(this,
                        "Has alcanzado el límite de intentos.\n¿Deseas recuperar tu contraseña?",
                        "Límite de intentos",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        new String[]{"Sí", "No"},
                        "Sí");
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
    private void hacerRegistro() {
        JTextField fieldUser = new JTextField();
        JPasswordField fieldPass = new JPasswordField();
        JTextField fieldNombre = new JTextField();
        Object[] message = {
            "Nombre Completo:", fieldNombre,
            "Nuevo Usuario:", fieldUser,
            "Contraseña:", fieldPass
        };
        int option = JOptionPane.showConfirmDialog(this, message, "Crear Cuenta", JOptionPane.OK_CANCEL_OPTION);
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
}
