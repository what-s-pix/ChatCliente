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
        int erroresConsecutivos = 0;
        final int MAX_ERRORES = 3;
        while (activo) {
            try {
                if (!Cliente.getInstance().estaConectado()) {
                    Thread.sleep(1000);
                    continue;
                }
                Peticion respuesta = Cliente.getInstance().recibir();
                erroresConsecutivos = 0;
                if (procesador != null) {
                    procesador.procesar(respuesta);
                }
            } catch (java.io.EOFException eof) {
                activo = false;
                break;
            } catch (java.io.StreamCorruptedException sce) {
                erroresConsecutivos++;
                if (erroresConsecutivos >= MAX_ERRORES) {
                    activo = false;
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    break;
                }
                if (!Cliente.getInstance().estaConectado()) {
                    activo = false;
                    break;
                }
            } catch (java.net.SocketException se) {
                activo = false;
                break;
            } catch (ClassCastException cce) {
                erroresConsecutivos++;
                if (erroresConsecutivos >= MAX_ERRORES) {
                    activo = false;
                    break;
                }
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
                if (erroresConsecutivos >= MAX_ERRORES) {
                    activo = false;
                    break;
                }
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
    }
}
