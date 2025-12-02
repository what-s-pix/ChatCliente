package ui.autenticacion;

import chatcliente.Cliente;
import common.Peticion;
import models.Usuario;
import javax.swing.*;

public class manejador_login {
    
    private panel_formulario_login formPanel;
    private LoginCallback callback;
    private int intentosFallidos;
    private static final int MAX_INTENTOS = 3;
    
    public interface LoginCallback {
        void onLoginExitoso(Usuario usuario);
        void onMaxIntentosAlcanzados();
    }
    
    public manejador_login(panel_formulario_login formPanel) {
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
        System.out.println("[DEBUG] ========== hacerLogin() llamado ==========");
        
        String user = formPanel.getUsername();
        String pass = formPanel.getPassword();
        
        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(null, 
                "por favor llena todos los campos.");
            return;
        }
        
        // Deshabilitar el botón mientras se procesa
        // (Esto se haría desde la ventana, pero por ahora solo hacemos el proceso asíncrono)
        
        // Ejecutar el login en un hilo separado para no bloquear la UI
        new Thread(() -> {
            try {
                System.out.println("[DEBUG] [THREAD] Intentando conectar al servidor...");
                Cliente.getInstance().conectar();
                System.out.println("[DEBUG] [THREAD] Conexión exitosa, enviando petición LOGIN...");
                
                Usuario u = new Usuario(null, user, pass);
                Peticion p = new Peticion("LOGIN", u);
                
                Cliente.getInstance().enviar(p);
                System.out.println("[DEBUG] [THREAD] Petición enviada, esperando respuesta...");
                Peticion respuesta = Cliente.getInstance().recibir();
                System.out.println("[DEBUG] [THREAD] Respuesta recibida: " + respuesta.getAccion());
                
                // Actualizar UI en el hilo de Swing
                SwingUtilities.invokeLater(() -> {
                    procesarRespuestaLogin(respuesta);
                });
                
            } catch (java.net.ConnectException e) {
                System.out.println("[ERROR] ConnectException: " + e.getMessage());
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null, 
                        "No se pudo conectar al servidor.\n" +
                        "Verifica que el servidor esté corriendo en " + 
                        Cliente.getInstance().getHost() + ":" + Cliente.getInstance().getPuerto() + "\n\n" +
                        "Error: " + e.getMessage(), 
                        "Error de Conexión", 
                        JOptionPane.ERROR_MESSAGE);
                });
            } catch (java.net.SocketTimeoutException e) {
                System.out.println("[ERROR] SocketTimeoutException: " + e.getMessage());
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null, 
                        "Timeout al conectar con el servidor.\n" +
                        "El servidor no respondió a tiempo.\n\n" +
                        "Error: " + e.getMessage(), 
                        "Timeout de Conexión", 
                        JOptionPane.ERROR_MESSAGE);
                });
            } catch (Exception ex) {
                final String tipoExcepcion = ex.getClass().getSimpleName();
                String mensajeTemp = ex.getMessage();
                if (mensajeTemp == null || mensajeTemp.isEmpty()) {
                    mensajeTemp = tipoExcepcion;
                    if (ex.getCause() != null && ex.getCause().getMessage() != null) {
                        mensajeTemp += ": " + ex.getCause().getMessage();
                    }
                }
                final String mensajeError = mensajeTemp;
                System.out.println("[ERROR] Excepción: " + tipoExcepcion + " - " + mensajeError);
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null, 
                        "Error de conexión: " + mensajeError + "\n\n" +
                        "Tipo: " + tipoExcepcion + "\n\n" +
                        "Verifica que el servidor esté corriendo.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    private void procesarRespuestaLogin(Peticion respuesta) {
        if (respuesta.getAccion().equals("LOGIN_OK")) {
            System.out.println("[DEBUG] Login exitoso! Procesando usuario...");
            Usuario logueado = (Usuario) respuesta.getDatos();
            System.out.println("[DEBUG] Usuario logueado: " + (logueado != null ? logueado.getNombre() : "null"));
            intentosFallidos = 0;
            
            JOptionPane.showMessageDialog(null, 
                "bienvenido " + logueado.getNombre() + "!");
            
            if (callback != null) {
                System.out.println("[DEBUG] Llamando callback.onLoginExitoso()...");
                callback.onLoginExitoso(logueado);
                System.out.println("[DEBUG] Callback ejecutado!");
            } else {
                System.out.println("[ERROR] Callback es null! No se puede abrir la ventana principal.");
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
    }
}

