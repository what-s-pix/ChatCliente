package ui.autenticacion;

import javax.swing.*;
import java.awt.*;

public class panel_formulario_login extends JPanel {
    
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    
    public panel_formulario_login() {
        setLayout(new GridLayout(3, 2, 5, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));
        
        add(new JLabel("Usuario:"));
        txtUsername = new JTextField();
        add(txtUsername);
        
        add(new JLabel("Contrasena:"));
        txtPassword = new JPasswordField();
        add(txtPassword);
    }
    
    public String getUsername() {
        return txtUsername.getText().trim();
    }
    
    public String getPassword() {
        return new String(txtPassword.getPassword()).trim();
    }
    
    public void limpiar() {
        txtUsername.setText("");
        txtPassword.setText("");
    }
}

