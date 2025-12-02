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
        if (socket == null || socket.isClosed()) {
            socket = new Socket(host, puerto);
            salida = new ObjectOutputStream(socket.getOutputStream());
            entrada = new ObjectInputStream(socket.getInputStream());
        }
    }
    public void enviar(Peticion p) throws IOException {
        salida.writeObject(p);
        salida.flush();
    }
    public Peticion recibir() throws IOException, ClassNotFoundException {
        return (Peticion) entrada.readObject();
    }
    public void cerrar() {
        try {
            if (socket != null) socket.close();
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
