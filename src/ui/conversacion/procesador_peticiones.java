package ui.conversacion;

import models.Amigo;
import models.Grupo;
import models.InvitacionGrupo;
import models.Mensaje;
import models.Usuario;
import common.Peticion;
import chatcliente.Cliente;
import ui.conversacion.componentes.panel_amigos;
import ui.conversacion.componentes.panel_grupos;
import ui.conversacion.componentes.panel_invitaciones;
import ui.conversacion.componentes.panel_usuarios;
import javax.swing.*;
import java.util.List;
import java.util.Map;

public class procesador_peticiones {
    
    private panel_usuarios usuariosPanel;
    private panel_amigos amigosPanel;
    private panel_grupos gruposPanel;
    private panel_invitaciones invitacionesPanel;
    private Map<String, ventana_conversacion> ventanasChatAbiertas;
    private Map<Integer, Usuario> mapaUsuarios;
    
    public procesador_peticiones(panel_usuarios usuariosPanel,
                               panel_amigos amigosPanel,
                               panel_grupos gruposPanel,
                               panel_invitaciones invitacionesPanel,
                               Map<String, ventana_conversacion> ventanasChatAbiertas,
                               Map<Integer, Usuario> mapaUsuarios) {
        this.usuariosPanel = usuariosPanel;
        this.amigosPanel = amigosPanel;
        this.gruposPanel = gruposPanel;
        this.invitacionesPanel = invitacionesPanel;
        this.ventanasChatAbiertas = ventanasChatAbiertas;
        this.mapaUsuarios = mapaUsuarios;
    }
    
    public void procesar(Peticion p) {
        String accion = p.getAccion();
        
        switch (accion) {
            case "MENSAJE_RECIBIDO":
            case "MENSAJE_AMIGO_RECIBIDO":
                procesarMensajeRecibido((Mensaje) p.getDatos(), false);
                break;
                
            case "MENSAJE_GRUPO_RECIBIDO":
                procesarMensajeRecibido((Mensaje) p.getDatos(), true);
                break;
                
            case "USUARIOS_CONECTADOS":
            case "LISTA_USUARIOS":
                @SuppressWarnings("unchecked")
                List<Usuario> usuarios = (List<Usuario>) p.getDatos();
                System.out.println("[PROCESADOR] Recibida lista de usuarios: " + usuarios.size());
                actualizarMapaUsuarios(usuarios);
                usuariosPanel.actualizarUsuarios(usuarios);
                break;
            case "USUARIO_CONECTO":
            case "USUARIO_DESCONECTO":
                // Actualizar la lista cuando un usuario se conecta/desconecta
                System.out.println("[PROCESADOR] Usuario " + (p.getAccion().equals("USUARIO_CONECTO") ? "conectado" : "desconectado"));
                // Solicitar lista actualizada
                try {
                    Cliente.getInstance().enviar(new Peticion("OBTENER_USUARIOS", null));
                } catch (Exception e) {
                    System.err.println("[PROCESADOR] Error solicitando lista actualizada: " + e.getMessage());
                }
                break;
                
            case "AMIGOS_OBTENIDOS":
                Object[] datosAmigos = (Object[]) p.getDatos();
                @SuppressWarnings("unchecked")
                List<Amigo> amigos = (List<Amigo>) datosAmigos[0];
                @SuppressWarnings("unchecked")
                List<Usuario> usuariosAmigos = (List<Usuario>) datosAmigos[1];
                actualizarMapaUsuarios(usuariosAmigos);
                amigosPanel.actualizarAmigos(amigos, usuariosAmigos);
                break;
                
            case "GRUPOS_OBTENIDOS":
                @SuppressWarnings("unchecked")
                List<Grupo> grupos = (List<Grupo>) p.getDatos();
                gruposPanel.actualizarGrupos(grupos);
                break;
                
            case "INVITACIONES_OBTENIDAS":
                @SuppressWarnings("unchecked")
                List<Amigo> invitacionesAmigos = (List<Amigo>) p.getDatos();
                invitacionesPanel.actualizarInvitacionesAmigos(invitacionesAmigos);
                break;
                
            case "INVITACIONES_GRUPO_OBTENIDAS":
                @SuppressWarnings("unchecked")
                List<InvitacionGrupo> invitacionesGrupos = (List<InvitacionGrupo>) p.getDatos();
                invitacionesPanel.actualizarInvitacionesGrupos(invitacionesGrupos);
                break;
                
            case "MENSAJE_ERROR":
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null, 
                        "Error: " + p.getDatos(), 
                        "Error", 
                        JOptionPane.WARNING_MESSAGE);
                });
                break;
        }
    }
    
    private void procesarMensajeRecibido(Mensaje mensaje, boolean esGrupo) {
        SwingUtilities.invokeLater(() -> {
            String clave = null;
            if (esGrupo) {
                clave = "grupo_" + mensaje.getFk_destinatario();
            } else {
                int otroId = mensaje.getFk_remitente();
                clave = "amigo_" + otroId;
                if (!ventanasChatAbiertas.containsKey(clave)) {
                    clave = "usuario_" + otroId;
                }
            }
            
            if (clave != null) {
                ventana_conversacion ventana = ventanasChatAbiertas.get(clave);
                if (ventana != null) {
                    ventana.recibirMensaje(mensaje);
                }
            }
        });
    }
    
    private void actualizarMapaUsuarios(List<Usuario> usuarios) {
        for (Usuario u : usuarios) {
            mapaUsuarios.put(u.getPk_usuario(), u);
        }
    }
}

