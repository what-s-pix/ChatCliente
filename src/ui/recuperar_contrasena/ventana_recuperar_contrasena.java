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
        
        try {
            Cliente.getInstance().conectar();
            
            Peticion p = new Peticion("RECUPERAR_CONTRASENA", 
                new Object[] {username, nuevaPass});
            
            Cliente.getInstance().enviar(p);
            Peticion respuesta = Cliente.getInstance().recibir();
            
            if (respuesta.getAccion().equals("RECUPERAR_OK")) {
                JOptionPane.showMessageDialog(this, 
                    "contrasena recuperada exitosamente.");
                exito = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "error: " + respuesta.getDatos(), 
                    "error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "error de conexion: " + ex.getMessage());
        }
    }
    
    public boolean fueExitoso() {
        return exito;
    }
}

