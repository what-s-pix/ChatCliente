package ui.autenticacion;

import chatcliente.Cliente;
import common.Peticion;
import models.Usuario;
import ui.recuperar_contrasena.ventana_recuperar_contrasena;
import javax.swing.*;

public class manejador_registro {
    
    public void hacerRegistro(JFrame parent) {
        JTextField fieldUser = new JTextField();
        JPasswordField fieldPass = new JPasswordField();
        JTextField fieldNombre = new JTextField();
        
        JPanel panel = new JPanel(new java.awt.GridLayout(4, 2, 5, 5));
        panel.add(new JLabel("nombre completo:"));
        panel.add(fieldNombre);
        panel.add(new JLabel("nuevo usuario:"));
        panel.add(fieldUser);
        panel.add(new JLabel("contrasena:"));
        panel.add(fieldPass);
        
        JButton btnRecuperar = new JButton("recuperar cuenta");
        btnRecuperar.addActionListener(e -> {
            ventana_recuperar_contrasena recuperar = new ventana_recuperar_contrasena(parent);
            recuperar.setVisible(true);
        });
        panel.add(new JLabel(""));
        panel.add(btnRecuperar);
        
        int option = JOptionPane.showConfirmDialog(
            parent, panel, "crear cuenta", 
            JOptionPane.OK_CANCEL_OPTION);
        
        if (option == JOptionPane.OK_OPTION) {
            String nombre = fieldNombre.getText().trim();
            String user = fieldUser.getText().trim();
            String pass = new String(fieldPass.getPassword()).trim();
            
            if (nombre.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(parent, 
                    "todos los campos son obligatorios.");
                return;
            }
            
            try {
                Cliente.getInstance().conectar();
                Usuario uNuevo = new Usuario(nombre, user, pass);
                Peticion p = new Peticion("REGISTRO", uNuevo);
                
                Cliente.getInstance().enviar(p);
                Peticion respuesta = Cliente.getInstance().recibir();
                
                if (respuesta.getAccion().equals("REGISTRO_OK")) {
                    JOptionPane.showMessageDialog(parent, 
                        "registro exitoso. ahora puedes entrar.");
                } else {
                    JOptionPane.showMessageDialog(parent, 
                        "error: " + respuesta.getDatos());
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(parent, 
                    "error: " + ex.getMessage());
            }
        }
    }
}

