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
    private JTextField txtUsername; // Componentes de la interfaz
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
        JPanel panelFormulario = new JPanel(); // PANEL CENTRAL (Formulario)
        panelFormulario.setLayout(new GridLayout(3, 2, 5, 10)); // 3 filas, 2 columnas, espacios
        panelFormulario.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20)); // Margen interno
        panelFormulario.add(new JLabel("Usuario:")); // Fila 1: Usuario
        txtUsername = new JTextField();
        panelFormulario.add(txtUsername);
        panelFormulario.add(new JLabel("Contraseña:")); // Fila 2: Password
        txtPassword = new JPasswordField();
        panelFormulario.add(txtPassword);
        JPanel panelBotones = new JPanel(); // PANEL INFERIOR (Botones)
        panelBotones.setLayout(new FlowLayout());
        btnLogin = new JButton("Entrar");
        btnRegistro = new JButton("Registrarse");
        panelBotones.add(btnLogin);
        panelBotones.add(btnRegistro);
        add(panelFormulario, BorderLayout.CENTER); // Agregamos paneles a la ventana
        add(panelBotones, BorderLayout.SOUTH);
        btnLogin.addActionListener(new ActionListener() { // Acción del botón LOGIN
            @Override
            public void actionPerformed(ActionEvent e) {
                hacerLogin();
            }
        });
        btnRegistro.addActionListener(new ActionListener() { // Acción del botón REGISTRO
            @Override
            public void actionPerformed(ActionEvent e) {
                hacerRegistro();
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
            Cliente.getInstance().conectar(); // 1. Conectar (si no estaba conectado)
            Usuario u = new Usuario(null, user, pass); // 2. Preparar el objeto Usuario y la Petición
            Peticion p = new Peticion("LOGIN", u);
            Cliente.getInstance().enviar(p); // 3. Enviar
            Peticion respuesta = Cliente.getInstance().recibir(); // 4. Recibir respuesta
            if (respuesta.getAccion().equals("LOGIN_OK")) { // 5. Analizar respuesta
                Usuario logueado = (Usuario) respuesta.getDatos();
                new ChatUI(logueado).setVisible(true);
                this.dispose();
            } else if (respuesta.getAccion().equals("LOGIN_BLOQUEADO")) {
                JOptionPane.showMessageDialog(this, "CUENTA BLOQUEADA: " + respuesta.getDatos(), "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Login fallido: " + respuesta.getDatos(), "Error", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error de conexión: " + ex.getMessage());
        }
    }
    private void hacerRegistro() {
        JTextField fieldUser = new JTextField(); // Creamos un panel con 3 campos
        JPasswordField fieldPass = new JPasswordField();
        JTextField fieldNombre = new JTextField();
        Object[] message = {
            "Nombre Completo:", fieldNombre,
            "Nuevo Usuario:", fieldUser,
            "Contraseña:", fieldPass
        };
        int option = JOptionPane.showConfirmDialog(this, message, "Crear Cuenta", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String nombre = fieldNombre.getText().trim(); // Obtenemos los datos del panel, no de la ventana principal
            String user = fieldUser.getText().trim();
            String pass = new String(fieldPass.getPassword()).trim();
            if (nombre.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.");
                return;
            }
            try {
                Cliente.getInstance().conectar(); // Enviamos al servidor
                Usuario uNuevo = new Usuario(nombre, user, pass);
                Peticion p = new Peticion("REGISTRO", uNuevo);
                Cliente.getInstance().enviar(p);
                Peticion respuesta = Cliente.getInstance().recibir();
                if (respuesta.getAccion().equals("REGISTRO_OK")) {
                    JOptionPane.showMessageDialog(this, "Registro exitoso. Ahora puedes entrar.");
                } else {
                    JOptionPane.showMessageDialog(this, "Error: " + respuesta.getDatos());
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }
}
