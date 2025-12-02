package ui.conversacion;
import models.Amigo;
import models.Grupo;
import models.InvitacionGrupo;
import models.Mensaje;
import models.MensajePendiente;
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
            case "MENSAJES_PENDIENTES": // Mensajes pendientes (al iniciar sesión)
                procesarMensajesPendientes(p.getDatos());
                break;
            case "RECIBIR_MENSAJE": // Este es el que envía el servidor
            case "MENSAJE_RECIBIDO":
            case "MENSAJE_AMIGO_RECIBIDO":
                procesarMensajeRecibido((Mensaje) p.getDatos(), false, true);
                break;
            case "MENSAJE_USUARIO_RECIBIDO":
                procesarMensajeRecibido((Mensaje) p.getDatos(), false, false);
                break;
            case "MENSAJE_GRUPO": // Este es el que envía el servidor
            case "MENSAJE_GRUPO_RECIBIDO":
                procesarMensajeRecibido((Mensaje) p.getDatos(), true, false);
                break;
            case "USUARIOS_CONECTADOS":
            case "LISTA_USUARIOS":
                @SuppressWarnings("unchecked")
                List<Usuario> usuarios = (List<Usuario>) p.getDatos();
                actualizarMapaUsuarios(usuarios);
                usuariosPanel.actualizarUsuarios(usuarios);
                break;
            case "USUARIO_CONECTO":
            case "USUARIO_DESCONECTO":
            case "ACTUALIZAR_ESTADO_USUARIO":
                try {
                    Cliente.getInstance().enviar(new Peticion("OBTENER_USUARIOS", null));
                } catch (Exception e) {}
                break;
            case "LISTA_AMIGOS_OK": // Este es el que envía el servidor
            case "AMIGOS_OBTENIDOS":
                procesarListaAmigos(p.getDatos());
                break;
            case "LISTA_GRUPOS_OK": // Este es el que envía el servidor
            case "GRUPOS_OBTENIDOS":
                @SuppressWarnings("unchecked")
                List<Grupo> grupos = (List<Grupo>) p.getDatos();
                gruposPanel.actualizarGrupos(grupos);
                break;
            case "LISTA_SOLICITUDES_OK": // Solicitudes de amistad
            case "INVITACIONES_OBTENIDAS":
                procesarSolicitudesAmistad(p.getDatos());
                break;
            case "INVITACIONES_GRUPO_OK": // Invitaciones a grupos
            case "INVITACIONES_GRUPO_OBTENIDAS":
                procesarInvitacionesGrupo(p.getDatos());
                break;
            case "NUEVA_SOLICITUD_AMISTAD":
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null,
                        "Tienes una nueva solicitud de amistad",
                        "Nueva Solicitud",
                        JOptionPane.PLAIN_MESSAGE);
                });
                try {
                    Cliente.getInstance().enviar(new Peticion("OBTENER_SOLICITUDES", null));
                } catch (Exception e) {}
                break;
            case "NUEVA_INVITACION_GRUPO":
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null,
                        "Tienes una nueva invitación a un grupo",
                        "Nueva Invitación",
                        JOptionPane.PLAIN_MESSAGE);
                });
                try {
                    Cliente.getInstance().enviar(new Peticion("OBTENER_INVITACIONES_GRUPO", null));
                } catch (Exception e) {}
                break;
            case "MENSAJE_ENVIADO":
                break;
            case "HISTORIAL_OK":
                procesarHistorial(p.getDatos(), false);
                break;
            case "HISTORIAL_GRUPO_OK":
                procesarHistorial(p.getDatos(), true);
                break;
            case "SOLICITUD_ENVIADA_OK":
                try {
                    Cliente.getInstance().enviar(new Peticion("OBTENER_SOLICITUDES", null));
                } catch (Exception e) {}
                break;
            case "ACEPTAR_SOLICITUD_OK":
                SwingUtilities.invokeLater(() -> {
                    try {
                        Cliente.getInstance().enviar(new Peticion("OBTENER_SOLICITUDES", null));
                        Cliente.getInstance().enviar(new Peticion("OBTENER_AMIGOS", null));
                        Cliente.getInstance().enviar(new Peticion("OBTENER_USUARIOS", null));
                    } catch (Exception e) {}
                });
                break;
            case "SOLICITUD_RECHAZADA":
                SwingUtilities.invokeLater(() -> {
                    try {
                        Cliente.getInstance().enviar(new Peticion("OBTENER_SOLICITUDES", null));
                    } catch (Exception e) {}
                });
                break;
            case "CREAR_GRUPO_OK":
            case "ACEPTAR_GRUPO_OK":
                SwingUtilities.invokeLater(() -> {
                    try {
                        Cliente.getInstance().enviar(new Peticion("OBTENER_INVITACIONES_GRUPO", null));
                        Cliente.getInstance().enviar(new Peticion("OBTENER_GRUPOS", null));
                    } catch (Exception e) {}
                });
                break;
            case "INVITACION_RECHAZADA":
                SwingUtilities.invokeLater(() -> {
                    try {
                        Cliente.getInstance().enviar(new Peticion("OBTENER_INVITACIONES_GRUPO", null));
                    } catch (Exception e) {}
                });
                break;
            case "MENSAJE_ERROR":
            case "SOLICITUD_ERROR":
            case "INVITACION_ERROR":
            case "ERROR_GRUPO":
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null,
                        "Error: " + p.getDatos(),
                        "Error",
                        JOptionPane.PLAIN_MESSAGE);
                });
                break;
            default:
                break;
        }
    }
    @SuppressWarnings("unchecked")
    private void procesarListaAmigos(Object datos) {
        try {
            if (datos instanceof Object[]) {
                Object[] datosAmigos = (Object[]) datos;
                List<Amigo> amigos = (List<Amigo>) datosAmigos[0];
                List<Usuario> usuariosAmigos = (List<Usuario>) datosAmigos[1];
                actualizarMapaUsuarios(usuariosAmigos);
                amigosPanel.actualizarAmigos(amigos, usuariosAmigos);
            } else if (datos instanceof List) {
                List<?> lista = (List<?>) datos;
                if (!lista.isEmpty()) {
                    amigosPanel.actualizarAmistades((List<models.Amistad>) lista);
                }
            }
        } catch (Exception e) {}
    }
    @SuppressWarnings("unchecked")
    private void procesarSolicitudesAmistad(Object datos) {
        try {
            if (datos instanceof List) {
                List<?> lista = (List<?>) datos;
                if (!lista.isEmpty() && lista.get(0) instanceof String) {
                    invitacionesPanel.actualizarSolicitudesTexto((List<String>) lista);
                } else if (!lista.isEmpty() && lista.get(0) instanceof Amigo) {
                    invitacionesPanel.actualizarInvitacionesAmigos((List<Amigo>) lista);
                }
            }
        } catch (Exception e) {}
    }
    @SuppressWarnings("unchecked")
    private void procesarInvitacionesGrupo(Object datos) {
        try {
            if (datos instanceof List) {
                List<?> lista = (List<?>) datos;
                if (!lista.isEmpty() && lista.get(0) instanceof Grupo) {
                    invitacionesPanel.actualizarGruposInvitados((List<Grupo>) lista);
                } else if (!lista.isEmpty() && lista.get(0) instanceof InvitacionGrupo) {
                    invitacionesPanel.actualizarInvitacionesGrupos((List<InvitacionGrupo>) lista);
                }
            }
        } catch (Exception e) {}
    }
    private void procesarMensajeRecibido(Mensaje mensaje, boolean esGrupo, boolean esAmigo) {
        SwingUtilities.invokeLater(() -> {
            String clave = null;
            if (esGrupo) {
                clave = "grupo_" + mensaje.getFk_destinatario();
            } else {
                int otroId = mensaje.getFk_remitente();
                // Enrutar según el tipo de chat: amigo o usuario
                if (esAmigo) {
                    clave = "amigo_" + otroId;
                } else {
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
    @SuppressWarnings("unchecked")
    private void procesarMensajesPendientes(Object datos) {
        try {
            if (datos instanceof List) {
                List<MensajePendiente> pendientes = (List<MensajePendiente>) datos;
                for (MensajePendiente mp : pendientes) {
                    SwingUtilities.invokeLater(() -> {
                        String remitente = mp.getNombreRemitente() != null ?
                            mp.getNombreRemitente() : "Usuario " + mp.getFk_remitente();
                        String tipoMsg = "grupo".equals(mp.getTipo()) ?
                            " (en grupo " + (mp.getTituloGrupo() != null ? mp.getTituloGrupo() : mp.getFk_grupo()) + ")" : "";
                        JOptionPane.showMessageDialog(null,
                            "Mensaje de " + remitente + tipoMsg + ":\n" + mp.getMensaje(),
                            "Mensaje Pendiente",
                            JOptionPane.PLAIN_MESSAGE);
                    });
                }
            }
        } catch (Exception e) {}
    }
    @SuppressWarnings("unchecked")
    private void procesarHistorial(Object datos, boolean esGrupo) {
        try {
            if (datos instanceof Object[]) {
                Object[] paquete = (Object[]) datos;
                Object identificador = paquete[0];
                List<Mensaje> mensajes = (List<Mensaje>) paquete[1];
                SwingUtilities.invokeLater(() -> {
                    String clave = null;
                    if (esGrupo) {
                        int grupoId = (Integer) identificador;
                        clave = "grupo_" + grupoId;
                    } else {
                        // Solo buscar en chats de amigos (el historial solo se carga para amigos)
                        if (identificador instanceof String) {
                            String username = (String) identificador;
                            for (Map.Entry<Integer, Usuario> entry : mapaUsuarios.entrySet()) {
                                if (entry.getValue().getUsername().equals(username)) {
                                    clave = "amigo_" + entry.getKey();
                                    break;
                                }
                            }
                        } else if (identificador instanceof Integer) {
                            int userId = (Integer) identificador;
                            clave = "amigo_" + userId;
                        }
                    }
                    if (clave != null) {
                        ventana_conversacion ventana = ventanasChatAbiertas.get(clave);
                        if (ventana != null) {
                            ventana.mostrarHistorial(mensajes);
                        }
                    }
                });
            }
        } catch (Exception e) {}
    }
}
