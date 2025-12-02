package models;

import java.io.Serializable;

public class Amigo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int pk_amigo;
    private int fk_usuario1;
    private int fk_usuario2;
    private int estado; // 0: pendiente, 1: aceptado, 2: rechazado
    private String nombre_usuario1;
    private String nombre_usuario2;
    
    public Amigo() {}
    
    public Amigo(int fk_usuario1, int fk_usuario2, int estado) {
        this.fk_usuario1 = fk_usuario1;
        this.fk_usuario2 = fk_usuario2;
        this.estado = estado;
    }
    
    public int getPk_amigo() {
        return pk_amigo;
    }
    
    public void setPk_amigo(int pk_amigo) {
        this.pk_amigo = pk_amigo;
    }
    
    public int getFk_usuario1() {
        return fk_usuario1;
    }
    
    public void setFk_usuario1(int fk_usuario1) {
        this.fk_usuario1 = fk_usuario1;
    }
    
    public int getFk_usuario2() {
        return fk_usuario2;
    }
    
    public void setFk_usuario2(int fk_usuario2) {
        this.fk_usuario2 = fk_usuario2;
    }
    
    public int getEstado() {
        return estado;
    }
    
    public void setEstado(int estado) {
        this.estado = estado;
    }
    
    public String getNombre_usuario1() {
        return nombre_usuario1;
    }
    
    public void setNombre_usuario1(String nombre_usuario1) {
        this.nombre_usuario1 = nombre_usuario1;
    }
    
    public String getNombre_usuario2() {
        return nombre_usuario2;
    }
    
    public void setNombre_usuario2(String nombre_usuario2) {
        this.nombre_usuario2 = nombre_usuario2;
    }
}

