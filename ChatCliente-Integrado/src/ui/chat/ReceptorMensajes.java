package ui.chat;

import chatcliente.Cliente;
import common.Peticion;
import javax.swing.*;
import java.io.IOException;

public class ReceptorMensajes extends Thread {
    
    private boolean activo;
    private ProcesadorPeticiones procesador;
    
    public ReceptorMensajes(Object mensajesPanel, 
                           ProcesadorPeticiones procesador) {
        this.procesador = procesador;
        this.activo = true;
        setDaemon(true);
    }
    
    public void detener() {
        activo = false;
    }
    
    @Override
    public void run() {
        while (activo) {
            try {
                if (!Cliente.getInstance().estaConectado()) {
                    Thread.sleep(1000);
                    continue;
                }
                Peticion respuesta = Cliente.getInstance().recibir();
                if (procesador != null) {
                    procesador.procesar(respuesta);
                }
            } catch (IOException | ClassNotFoundException ex) {
                if (activo) {
                    activo = false;
                    break;
                }
            } catch (InterruptedException ex) {
                break;
            }
        }
    }
}

