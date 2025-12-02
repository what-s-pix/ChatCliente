package ui.conversacion;

import chatcliente.Cliente;
import common.Peticion;
import javax.swing.*;
import java.io.IOException;

public class receptor_mensajes extends Thread {
    
    private boolean activo;
    private procesador_peticiones procesador;
    
    public receptor_mensajes(Object mensajesPanel, 
                           procesador_peticiones procesador) {
        this.procesador = procesador;
        this.activo = true;
        setDaemon(true);
    }
    
    public void detener() {
        activo = false;
    }
    
    @Override
    public void run() {
        System.out.println("[RECEPTOR] Receptor de mensajes iniciado");
        int erroresConsecutivos = 0;
        final int MAX_ERRORES = 3;
        
        while (activo) {
            try {
                if (!Cliente.getInstance().estaConectado()) {
                    Thread.sleep(1000);
                    continue;
                }
                
                // Solo mostrar este mensaje cada cierto tiempo para no saturar la consola
                // System.out.println("[RECEPTOR] Esperando petición del servidor...");
                
                Peticion respuesta = Cliente.getInstance().recibir();
                erroresConsecutivos = 0; // Resetear contador de errores si recibimos algo
                System.out.println("[RECEPTOR] Petición recibida: " + respuesta.getAccion());
                
                if (procesador != null) {
                    procesador.procesar(respuesta);
                } else {
                    System.out.println("[RECEPTOR] ERROR: procesador es null!");
                }
            } catch (java.io.EOFException eof) {
                // EOFException es normal cuando el stream se cierra
                System.out.println("[RECEPTOR] Stream cerrado (EOF), deteniendo receptor");
                activo = false;
                break;
            } catch (java.io.StreamCorruptedException sce) {
                erroresConsecutivos++;
                System.err.println("[RECEPTOR] Stream corrupto (" + erroresConsecutivos + "/" + MAX_ERRORES + "): " + sce.getMessage());
                
                if (erroresConsecutivos >= MAX_ERRORES) {
                    System.err.println("[RECEPTOR] Demasiados errores de stream corrupto, deteniendo receptor");
                    activo = false;
                    break;
                }
                
                // Esperar un poco antes de reintentar
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    break;
                }
                
                // Verificar si aún está conectado
                if (!Cliente.getInstance().estaConectado()) {
                    System.out.println("[RECEPTOR] Cliente desconectado, deteniendo receptor");
                    activo = false;
                    break;
                }
            } catch (java.net.SocketException se) {
                // SocketException indica que la conexión fue cerrada o abortada
                System.err.println("[RECEPTOR] Conexión cerrada/abortada: " + se.getMessage());
                activo = false;
                break;
            } catch (ClassCastException cce) {
                erroresConsecutivos++;
                System.err.println("[RECEPTOR] Error de tipo (" + erroresConsecutivos + "/" + MAX_ERRORES + "): " + cce.getMessage());
                
                if (erroresConsecutivos >= MAX_ERRORES) {
                    System.err.println("[RECEPTOR] Demasiados errores de tipo, deteniendo receptor");
                    activo = false;
                    break;
                }
                
                // Esperar antes de reintentar
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    break;
                }
                
                if (!Cliente.getInstance().estaConectado()) {
                    activo = false;
                    break;
                }
            } catch (IOException | ClassNotFoundException ex) {
                erroresConsecutivos++;
                System.err.println("[RECEPTOR] Error recibiendo mensaje (" + erroresConsecutivos + "/" + MAX_ERRORES + "): " + ex.getMessage());
                
                if (erroresConsecutivos >= MAX_ERRORES) {
                    System.err.println("[RECEPTOR] Demasiados errores, deteniendo receptor");
                    activo = false;
                    break;
                }
                
                // Esperar antes de reintentar
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    break;
                }
                
                if (!Cliente.getInstance().estaConectado()) {
                    activo = false;
                    break;
                }
            } catch (InterruptedException ex) {
                break;
            }
        }
        System.out.println("[RECEPTOR] Receptor de mensajes detenido");
    }
}

