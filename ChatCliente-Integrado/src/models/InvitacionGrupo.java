package models;

import java.io.Serializable;

public class InvitacionGrupo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int pk_invitacion;
    private int fk_grupo;
    private int fk_usuario_invitado;
    private int fk_usuario_invitador;
    private int estado; // 0: pendiente, 1: aceptada, 2: rechazada
    private String titulo_grupo;
    private String nombre_invitador;
    
    public InvitacionGrupo() {}
    
    public InvitacionGrupo(int fk_grupo, int fk_usuario_invitado, int fk_usuario_invitador) {
        this.fk_grupo = fk_grupo;
        this.fk_usuario_invitado = fk_usuario_invitado;
        this.fk_usuario_invitador = fk_usuario_invitador;
        this.estado = 0;
    }
    
    public int getPk_invitacion() {
        return pk_invitacion;
    }
    
    public void setPk_invitacion(int pk_invitacion) {
        this.pk_invitacion = pk_invitacion;
    }
    
    public int getFk_grupo() {
        return fk_grupo;
    }
    
    public void setFk_grupo(int fk_grupo) {
        this.fk_grupo = fk_grupo;
    }
    
    public int getFk_usuario_invitado() {
        return fk_usuario_invitado;
    }
    
    public void setFk_usuario_invitado(int fk_usuario_invitado) {
        this.fk_usuario_invitado = fk_usuario_invitado;
    }
    
    public int getFk_usuario_invitador() {
        return fk_usuario_invitador;
    }
    
    public void setFk_usuario_invitador(int fk_usuario_invitador) {
        this.fk_usuario_invitador = fk_usuario_invitador;
    }
    
    public int getEstado() {
        return estado;
    }
    
    public void setEstado(int estado) {
        this.estado = estado;
    }
    
    public String getTitulo_grupo() {
        return titulo_grupo;
    }
    
    public void setTitulo_grupo(String titulo_grupo) {
        this.titulo_grupo = titulo_grupo;
    }
    
    public String getNombre_invitador() {
        return nombre_invitador;
    }
    
    public void setNombre_invitador(String nombre_invitador) {
        this.nombre_invitador = nombre_invitador;
    }
}

