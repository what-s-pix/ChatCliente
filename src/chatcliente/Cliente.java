package chatcliente;
import common.Peticion;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
public class Cliente {
    private static Cliente instance;
    private Socket socket;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;
    private String host = "localhost";
    private int puerto = 5000; 
    private Cliente() {}
    public static Cliente getInstance() {
        if (instance == null) {
            instance = new Cliente();
        }
        return instance;
    }
    public void conectar() throws IOException {
        System.out.println("[DEBUG] Intentando conectar a " + host + ":" + puerto);
        // Cerrar conexión anterior si existe
        cerrar();
        
        // Crear nueva conexión
        System.out.println("[DEBUG] Creando socket...");
        socket = new Socket();
        System.out.println("[DEBUG] Conectando a " + host + ":" + puerto + "...");
        socket.connect(new java.net.InetSocketAddress(host, puerto), 5000); // Timeout de 5 segundos
        System.out.println("[DEBUG] Conexión establecida! Creando streams...");
        salida = new ObjectOutputStream(socket.getOutputStream());
        entrada = new ObjectInputStream(socket.getInputStream());
        System.out.println("[DEBUG] Conexión completada exitosamente!");
    }
    public void enviar(Peticion p) throws IOException {
        if (salida == null) {
            throw new IOException("No hay conexión activa. Llama a conectar() primero.");
        }
        salida.writeObject(p);
        salida.flush();
    }
    public Peticion recibir() throws IOException, ClassNotFoundException {
        if (entrada == null) {
            throw new IOException("No hay conexión activa. Llama a conectar() primero.");
        }
        return (Peticion) entrada.readObject();
    }
    public boolean estaConectado() {
        return socket != null && !socket.isClosed() && salida != null && entrada != null;
    }
    public void cerrar() {
        try {
            if (entrada != null) {
                entrada.close();
                entrada = null;
            }
        } catch (IOException e) {}
        try {
            if (salida != null) {
                salida.close();
                salida = null;
            }
        } catch (IOException e) {}
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            socket = null;
        } catch (IOException e) {}
    }
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
