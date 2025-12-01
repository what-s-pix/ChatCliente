package models;

import java.io.Serializable;
import java.util.List;

public class Grupo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int pk_grupo;
    private String titulo;
    private int fk_creador;
    private List<Integer> miembros;
    private List<Integer> invitaciones_pendientes;
    
    public Grupo() {}
    
    public Grupo(String titulo, int fk_creador) {
        this.titulo = titulo;
        this.fk_creador = fk_creador;
    }
    
    public int getPk_grupo() {
        return pk_grupo;
    }
    
    public void setPk_grupo(int pk_grupo) {
        this.pk_grupo = pk_grupo;
    }
    
    public String getTitulo() {
        return titulo;
    }
    
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    
    public int getFk_creador() {
        return fk_creador;
    }
    
    public void setFk_creador(int fk_creador) {
        this.fk_creador = fk_creador;
    }
    
    public List<Integer> getMiembros() {
        return miembros;
    }
    
    public void setMiembros(List<Integer> miembros) {
        this.miembros = miembros;
    }
    
    public List<Integer> getInvitaciones_pendientes() {
        return invitaciones_pendientes;
    }
    
    public void setInvitaciones_pendientes(List<Integer> invitaciones_pendientes) {
        this.invitaciones_pendientes = invitaciones_pendientes;
    }
}

