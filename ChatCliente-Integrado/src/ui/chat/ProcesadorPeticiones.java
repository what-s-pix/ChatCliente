package ui.chat;

import models.Amigo;
import models.Grupo;
import models.InvitacionGrupo;
import models.Mensaje;
import models.Usuario;
import common.Peticion;
import ui.chat.components.AmigosPanel;
import ui.chat.components.GruposPanel;
import ui.chat.components.InvitacionesPanel;
import ui.chat.components.UsuariosPanel;
import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class ProcesadorPeticiones {
    
    private UsuariosPanel usuariosPanel;
    private AmigosPanel amigosPanel;
    private GruposPanel gruposPanel;
    private InvitacionesPanel invitacionesPanel;
    private Map<String, VentanaChat> ventanasChatAbiertas;
    private Map<Integer, Usuario> mapaUsuarios;
    
    public ProcesadorPeticiones(UsuariosPanel usuariosPanel,
                               AmigosPanel amigosPanel,
                               GruposPanel gruposPanel,
                               InvitacionesPanel invitacionesPanel,
                               Map<String, VentanaChat> ventanasChatAbiertas,
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
                @SuppressWarnings("unchecked")
                List<Usuario> usuarios = (List<Usuario>) p.getDatos();
                actualizarMapaUsuarios(usuarios);
                usuariosPanel.actualizarUsuarios(usuarios);
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
                        "error: " + p.getDatos(), 
                        "error", 
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
                VentanaChat ventana = ventanasChatAbiertas.get(clave);
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

