package ui.recuperar_contrasena;

import chatcliente.Cliente;
import common.Peticion;
import javax.swing.*;
import java.awt.*;

public class ventana_recuperar_contrasena extends JDialog {
    
    private JTextField txtUsername;
    private JPasswordField txtNuevaContrasena;
    private JPasswordField txtConfirmarContrasena;
    private boolean exito;
    
    public ventana_recuperar_contrasena(JFrame parent) {
        super(parent, "recuperar contrasena", true);
        this.exito = false;
        configurarVentana();
        inicializarComponentes();
    }
    
    private void configurarVentana() {
        setSize(400, 250);
        setLocationRelativeTo(getParent());
        setResizable(false);
    }
    
    private void inicializarComponentes() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("usuario:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtUsername = new JTextField(20);
        panel.add(txtUsername, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("nueva contrasena:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtNuevaContrasena = new JPasswordField(20);
        panel.add(txtNuevaContrasena, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("confirmar contrasena:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtConfirmarContrasena = new JPasswordField(20);
        panel.add(txtConfirmarContrasena, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JPanel panelBotones = new JPanel(new FlowLayout());
        JButton btnRecuperar = new JButton("recuperar");
        JButton btnCancelar = new JButton("cancelar");
        
        btnRecuperar.addActionListener(e -> recuperarContrasena());
        btnCancelar.addActionListener(e -> dispose());
        
        panelBotones.add(btnRecuperar);
        panelBotones.add(btnCancelar);
        panel.add(panelBotones, gbc);
        
        add(panel);
    }
    
    private void recuperarContrasena() {
        String username = txtUsername.getText().trim();
        String nuevaPass = new String(txtNuevaContrasena.getPassword()).trim();
        String confirmarPass = new String(txtConfirmarContrasena.getPassword()).trim();
        
        if (username.isEmpty() || nuevaPass.isEmpty() || confirmarPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "por favor llena todos los campos.");
            return;
        }
        
        if (!nuevaPass.equals(confirmarPass)) {
            JOptionPane.showMessageDialog(this, 
                "las contrasenas no coinciden.");
            return;
        }
        
        // Deshabilitar botones mientras se procesa
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        // Ejecutar en un hilo separado para no bloquear la UI
        new Thread(() -> {
            try {
                System.out.println("[RECUPERAR] Conectando al servidor...");
                
                // Cerrar conexión anterior si existe
                Cliente.getInstance().cerrar();
                
                // Crear nueva conexión
                Cliente.getInstance().conectar();
                
                // Enviar la petición con formato correcto (String[])
                String[] datosRecuperacion = new String[] {username, nuevaPass};
                Peticion p = new Peticion("RECUPERAR_CONTRASENA", datosRecuperacion);
                
                System.out.println("[RECUPERAR] Enviando petición...");
                Cliente.getInstance().enviar(p);
                
                System.out.println("[RECUPERAR] Esperando respuesta...");
                Peticion respuesta = Cliente.getInstance().recibir();
                System.out.println("[RECUPERAR] Respuesta recibida: " + respuesta.getAccion());
                
                // Cerrar la conexión temporal
                Cliente.getInstance().cerrar();
                
                // Actualizar UI en el hilo de Swing
                SwingUtilities.invokeLater(() -> {
                    setCursor(Cursor.getDefaultCursor());
                    if (respuesta.getAccion().equals("RECUPERAR_OK")) {
                        JOptionPane.showMessageDialog(this, 
                            "Contraseña recuperada exitosamente.\nYa puedes iniciar sesión con tu nueva contraseña.");
                        exito = true;
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(this, 
                            "Error: " + respuesta.getDatos(), 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                    }
                });
            } catch (java.net.ConnectException e) {
                // Cerrar la conexión si falló
                Cliente.getInstance().cerrar();
                final String mensajeError = e.getMessage() != null ? e.getMessage() : "No se pudo conectar";
                SwingUtilities.invokeLater(() -> {
                    setCursor(Cursor.getDefaultCursor());
                    JOptionPane.showMessageDialog(this, 
                        "No se pudo conectar al servidor.\n" +
                        "Verifica que el servidor esté corriendo.\n\n" +
                        "Error: " + mensajeError, 
                        "Error de Conexión", 
                        JOptionPane.ERROR_MESSAGE);
                });
            } catch (Exception ex) {
                // Cerrar la conexión si falló
                Cliente.getInstance().cerrar();
                final String tipoExcepcion = ex.getClass().getSimpleName();
                String mensajeTemp = ex.getMessage();
                if (mensajeTemp == null || mensajeTemp.isEmpty()) {
                    mensajeTemp = tipoExcepcion;
                    if (ex.getCause() != null && ex.getCause().getMessage() != null) {
                        mensajeTemp += ": " + ex.getCause().getMessage();
                    }
                }
                final String mensajeError = mensajeTemp;
                System.err.println("[RECUPERAR] Error: " + tipoExcepcion + " - " + mensajeError);
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    setCursor(Cursor.getDefaultCursor());
                    JOptionPane.showMessageDialog(this, 
                        "Error de conexión: " + mensajeError + "\n\n" +
                        "Tipo: " + tipoExcepcion + "\n\n" +
                        "Verifica que el servidor esté corriendo.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    public boolean fueExitoso() {
        return exito;
    }
}

