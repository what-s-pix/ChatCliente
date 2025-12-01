
package models;

import java.io.Serializable;
import java.sql.Timestamp;

public class Mensaje implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int pk_mensaje;
    private int fk_remitente;
    private int fk_destinatario;
    private String contenido;
    private Timestamp fecha_envio;
    private boolean leido;
    
    // Constructor para mensaje nuevo
    public Mensaje(int fk_remitente, int fk_destinatario, String contenido) {
        this.fk_remitente = fk_remitente;
        this.fk_destinatario = fk_destinatario;
        this.contenido = contenido;
        this.leido = false;
        this.fecha_envio = new Timestamp(System.currentTimeMillis());
    }
    
    // Constructor completo
    public Mensaje(int pk_mensaje, int fk_remitente, int fk_destinatario, String contenido, Timestamp fecha_envio, boolean leido) {
        this.pk_mensaje = pk_mensaje;
        this.fk_remitente = fk_remitente;
        this.fk_destinatario = fk_destinatario;
        this.contenido = contenido;
        this.fecha_envio = fecha_envio;
        this.leido = leido;
    }
    
    // Getters y Setters
    public int getPk_mensaje() {
        return pk_mensaje;
    }
    
    public void setPk_mensaje(int pk_mensaje) {
        this.pk_mensaje = pk_mensaje;
    }
    
    public int getFk_remitente() {
        return fk_remitente;
    }
    
    public void setFk_remitente(int fk_remitente) {
        this.fk_remitente = fk_remitente;
    }
    
    public int getFk_destinatario() {
        return fk_destinatario;
    }
    
    public void setFk_destinatario(int fk_destinatario) {
        this.fk_destinatario = fk_destinatario;
    }
    
    public String getContenido() {
        return contenido;
    }
    
    public void setContenido(String contenido) {
        this.contenido = contenido;
    }
    
    public Timestamp getFecha_envio() {
        return fecha_envio;
    }
    
    public void setFecha_envio(Timestamp fecha_envio) {
        this.fecha_envio = fecha_envio;
    }
    
    public boolean isLeido() {
        return leido;
    }
    
    public void setLeido(boolean leido) {
        this.leido = leido;
    }
}

