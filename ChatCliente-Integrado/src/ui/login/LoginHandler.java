package ui.login;

import chatcliente.Cliente;
import common.Peticion;
import models.Usuario;
import javax.swing.*;

public class LoginHandler {
    
    private LoginFormPanel formPanel;
    private LoginCallback callback;
    private int intentosFallidos;
    private static final int MAX_INTENTOS = 3;
    
    public interface LoginCallback {
        void onLoginExitoso(Usuario usuario);
        void onMaxIntentosAlcanzados();
    }
    
    public LoginHandler(LoginFormPanel formPanel) {
        this.formPanel = formPanel;
        this.intentosFallidos = 0;
    }
    
    public void setCallback(LoginCallback callback) {
        this.callback = callback;
    }
    
    public int getIntentosRestantes() {
        return MAX_INTENTOS - intentosFallidos;
    }
    
    public void hacerLogin() {
        String user = formPanel.getUsername();
        String pass = formPanel.getPassword();
        
        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(null, 
                "por favor llena todos los campos.");
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
                intentosFallidos = 0;
                JOptionPane.showMessageDialog(null, 
                    "bienvenido " + logueado.getNombre() + "!");
                
                if (callback != null) {
                    callback.onLoginExitoso(logueado);
                }
            } else if (respuesta.getAccion().equals("LOGIN_BLOQUEADO")) {
                JOptionPane.showMessageDialog(null, 
                    "cuenta bloqueada: " + respuesta.getDatos(), 
                    "error", 
                    JOptionPane.ERROR_MESSAGE);
                intentosFallidos = MAX_INTENTOS;
            } else {
                intentosFallidos++;
                int restantes = getIntentosRestantes();
                
                if (restantes > 0) {
                    JOptionPane.showMessageDialog(null, 
                        "login fallido: " + respuesta.getDatos() + 
                        "\nintentos restantes: " + restantes, 
                        "error", 
                        JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, 
                        "has alcanzado el maximo de intentos.\n" +
                        "seras redirigido al registro.", 
                        "maximo de intentos", 
                        JOptionPane.WARNING_MESSAGE);
                    
                    if (callback != null) {
                        callback.onMaxIntentosAlcanzados();
                    }
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, 
                "error de conexion: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}

