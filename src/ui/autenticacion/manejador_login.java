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
        String user = formPanel.getUsername();
        String pass = formPanel.getPassword();
        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                "por favor llena todos los campos.",
                "Información", JOptionPane.PLAIN_MESSAGE);
            return;
        }
        new Thread(() -> {
            try {
                Cliente.getInstance().conectar();
                Usuario u = new Usuario(null, user, pass);
                Peticion p = new Peticion("LOGIN", u);
                Cliente.getInstance().enviar(p);
                Peticion respuesta = Cliente.getInstance().recibir();
                SwingUtilities.invokeLater(() -> {
                    procesarRespuestaLogin(respuesta);
                });
            } catch (java.net.ConnectException e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null,
                        "No se pudo conectar al servidor.\n" +
                        "Verifica que el servidor esté corriendo en " +
                        Cliente.getInstance().getHost() + ":" + Cliente.getInstance().getPuerto() + "\n\n" +
                        "Error: " + e.getMessage(),
                        "Error de Conexión",
                        JOptionPane.PLAIN_MESSAGE);
                });
            } catch (java.net.SocketTimeoutException e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null,
                        "Timeout al conectar con el servidor.\n" +
                        "El servidor no respondió a tiempo.\n\n" +
                        "Error: " + e.getMessage(),
                        "Timeout de Conexión",
                        JOptionPane.PLAIN_MESSAGE);
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
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null,
                        "Error de conexión: " + mensajeError + "\n\n" +
                        "Tipo: " + tipoExcepcion + "\n\n" +
                        "Verifica que el servidor esté corriendo.",
                        "Error",
                        JOptionPane.PLAIN_MESSAGE);
                });
            }
        }).start();
    }
    private void procesarRespuestaLogin(Peticion respuesta) {
        if (respuesta.getAccion().equals("LOGIN_OK")) {
            Usuario logueado = (Usuario) respuesta.getDatos();
            intentosFallidos = 0;
            JOptionPane.showMessageDialog(null,
                "bienvenido " + logueado.getNombre() + "!",
                "Bienvenido", JOptionPane.PLAIN_MESSAGE);
            if (callback != null) {
                callback.onLoginExitoso(logueado);
            }
        } else if (respuesta.getAccion().equals("LOGIN_BLOQUEADO")) {
            JOptionPane.showMessageDialog(null,
                "cuenta bloqueada: " + respuesta.getDatos(),
                "error",
                JOptionPane.PLAIN_MESSAGE);
            intentosFallidos = MAX_INTENTOS;
        } else {
            intentosFallidos++;
            int restantes = getIntentosRestantes();
            if (restantes > 0) {
                JOptionPane.showMessageDialog(null,
                    "login fallido: " + respuesta.getDatos() +
                    "\nintentos restantes: " + restantes,
                    "error",
                    JOptionPane.PLAIN_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null,
                    "has alcanzado el maximo de intentos.\n" +
                    "seras redirigido al registro.",
                    "maximo de intentos",
                    JOptionPane.PLAIN_MESSAGE);
                if (callback != null) {
                    callback.onMaxIntentosAlcanzados();
                }
            }
        }
    }
}
