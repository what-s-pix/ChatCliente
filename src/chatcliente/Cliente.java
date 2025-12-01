
package chatcliente;

import common.Peticion;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Cliente {
    // 1. Instancia única 
    private static Cliente instance;
    
    // 2. Variables de conexión
    private Socket socket;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;
    private String host = "localhost"; // Se puede cambiar con setHost()
    private int puerto = 5000;

    // Constructor privado para que nadie haga "new Cliente()"
    private Cliente() {}

    // Método para obtener la única instancia
    public static Cliente getInstance() {
        if (instance == null) {
            instance = new Cliente();
        }
        return instance;
    }

    // 3. Conectar al servidor
    public void conectar() throws IOException {
        if (socket == null || socket.isClosed()) {
            socket = new Socket(host, puerto);
            // OJO: Orden importante
            salida = new ObjectOutputStream(socket.getOutputStream());
            entrada = new ObjectInputStream(socket.getInputStream());
        }
    }

    // 4. Enviar petición
    public void enviar(Peticion p) throws IOException {
        salida.writeObject(p);
        salida.flush();
    }

    // 5. Recibir respuesta (Síncrono - espera hasta recibir)
    public Peticion recibir() throws IOException, ClassNotFoundException {
        return (Peticion) entrada.readObject();
    }
    
    // 6. Cerrar
    public void cerrar() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {}
    }
    
    // 7. Setters para configurar conexión
    public void setHost(String host) {
        this.host = host;
    }
    
    public void setPuerto(int puerto) {
        this.puerto = puerto;
    }
    
    public String getHost() {
        return host;
    }
    
    public int getPuerto() {
        return puerto;
    }
}
