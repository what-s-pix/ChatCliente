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
        System.out.println("[PROCESADOR] Procesando acción: " + accion);
        
        switch (accion) {
            // === MENSAJES PENDIENTES (al iniciar sesión) ===
            case "MENSAJES_PENDIENTES":
                System.out.println("[PROCESADOR] Mensajes pendientes recibidos");
                procesarMensajesPendientes(p.getDatos());
                break;
                
            // === MENSAJES PRIVADOS ===
            case "RECIBIR_MENSAJE":  // Este es el que envía el servidor
            case "MENSAJE_RECIBIDO":
            case "MENSAJE_AMIGO_RECIBIDO":
                System.out.println("[PROCESADOR] Mensaje privado recibido");;
                procesarMensajeRecibido((Mensaje) p.getDatos(), false);
                break;
                
            // === MENSAJES DE GRUPO ===
            case "MENSAJE_GRUPO":  // Este es el que envía el servidor
            case "MENSAJE_GRUPO_RECIBIDO":
                System.out.println("[PROCESADOR] Mensaje de grupo recibido");
                procesarMensajeRecibido((Mensaje) p.getDatos(), true);
                break;
                
            // === USUARIOS ===
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
            case "ACTUALIZAR_ESTADO_USUARIO":
                System.out.println("[PROCESADOR] Usuario " + (p.getAccion().equals("USUARIO_CONECTO") ? "conectado" : "desconectado"));
                try {
                    Cliente.getInstance().enviar(new Peticion("OBTENER_USUARIOS", null));
                } catch (Exception e) {
                    System.err.println("[PROCESADOR] Error solicitando lista actualizada: " + e.getMessage());
                }
                break;
                
            // === AMIGOS ===
            case "LISTA_AMIGOS_OK":  // Este es el que envía el servidor
            case "AMIGOS_OBTENIDOS":
                System.out.println("[PROCESADOR] Lista de amigos recibida");
                procesarListaAmigos(p.getDatos());
                break;
                
            // === GRUPOS ===
            case "LISTA_GRUPOS_OK":  // Este es el que envía el servidor
            case "GRUPOS_OBTENIDOS":
                System.out.println("[PROCESADOR] Lista de grupos recibida");
                @SuppressWarnings("unchecked")
                List<Grupo> grupos = (List<Grupo>) p.getDatos();
                gruposPanel.actualizarGrupos(grupos);
                break;
                
            // === INVITACIONES ===
            case "LISTA_SOLICITUDES_OK":  // Solicitudes de amistad
            case "INVITACIONES_OBTENIDAS":
                System.out.println("[PROCESADOR] Invitaciones de amistad recibidas");
                procesarSolicitudesAmistad(p.getDatos());
                break;
                
            case "INVITACIONES_GRUPO_OK":  // Invitaciones a grupos
            case "INVITACIONES_GRUPO_OBTENIDAS":
                System.out.println("[PROCESADOR] Invitaciones de grupo recibidas");
                procesarInvitacionesGrupo(p.getDatos());
                break;
            
            case "NUEVA_SOLICITUD_AMISTAD":
                System.out.println("[PROCESADOR] Nueva solicitud de amistad recibida");
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null, 
                        "Tienes una nueva solicitud de amistad", 
                        "Nueva Solicitud", 
                        JOptionPane.INFORMATION_MESSAGE);
                });
                // Actualizar lista de solicitudes
                try {
                    Cliente.getInstance().enviar(new Peticion("OBTENER_SOLICITUDES", null));
                } catch (Exception e) {}
                break;
                
            case "NUEVA_INVITACION_GRUPO":
                System.out.println("[PROCESADOR] Nueva invitación de grupo recibida");
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null, 
                        "Tienes una nueva invitación a un grupo", 
                        "Nueva Invitación", 
                        JOptionPane.INFORMATION_MESSAGE);
                });
                try {
                    Cliente.getInstance().enviar(new Peticion("OBTENER_INVITACIONES_GRUPO", null));
                } catch (Exception e) {}
                break;
            
            // === CONFIRMACIONES ===
            case "MENSAJE_ENVIADO":
                System.out.println("[PROCESADOR] Mensaje enviado correctamente");
                break;
            
            // === HISTORIAL ===
            case "HISTORIAL_OK":
                System.out.println("[PROCESADOR] Historial privado recibido");
                procesarHistorial(p.getDatos(), false);
                break;
                
            case "HISTORIAL_GRUPO_OK":
                System.out.println("[PROCESADOR] Historial de grupo recibido");
                procesarHistorial(p.getDatos(), true);
                break;
                
            case "SOLICITUD_ENVIADA_OK":
                System.out.println("[PROCESADOR] Solicitud enviada exitosamente");
                // Actualizar lista de solicitudes
                try {
                    Cliente.getInstance().enviar(new Peticion("OBTENER_SOLICITUDES", null));
                } catch (Exception e) {
                    System.err.println("[PROCESADOR] Error actualizando solicitudes: " + e.getMessage());
                }
                break;
                
            case "ACEPTAR_SOLICITUD_OK":
                System.out.println("[PROCESADOR] Solicitud aceptada exitosamente");
                // Actualizar listas de amigos y solicitudes inmediatamente
                SwingUtilities.invokeLater(() -> {
                    try {
                        // Actualizar solicitudes primero para que desaparezcan de la UI
                        Cliente.getInstance().enviar(new Peticion("OBTENER_SOLICITUDES", null));
                        Cliente.getInstance().enviar(new Peticion("OBTENER_AMIGOS", null));
                        // También actualizar usuarios para tener la información completa
                        Cliente.getInstance().enviar(new Peticion("OBTENER_USUARIOS", null));
                    } catch (Exception e) {
                        System.err.println("[PROCESADOR] Error actualizando datos: " + e.getMessage());
                    }
                });
                break;
                
            case "SOLICITUD_RECHAZADA":
                System.out.println("[PROCESADOR] Solicitud rechazada");
                // Actualizar lista de solicitudes inmediatamente
                SwingUtilities.invokeLater(() -> {
                    try {
                        Cliente.getInstance().enviar(new Peticion("OBTENER_SOLICITUDES", null));
                    } catch (Exception e) {
                        System.err.println("[PROCESADOR] Error actualizando solicitudes: " + e.getMessage());
                    }
                });
                break;
                
            case "CREAR_GRUPO_OK":
            case "ACEPTAR_GRUPO_OK":
                System.out.println("[PROCESADOR] Operación exitosa: " + accion);
                // Actualizar lista de grupos e invitaciones inmediatamente
                SwingUtilities.invokeLater(() -> {
                    try {
                        Cliente.getInstance().enviar(new Peticion("OBTENER_INVITACIONES_GRUPO", null));
                        Cliente.getInstance().enviar(new Peticion("OBTENER_GRUPOS", null));
                    } catch (Exception e) {
                        System.err.println("[PROCESADOR] Error actualizando grupos: " + e.getMessage());
                    }
                });
                break;
                
            case "INVITACION_RECHAZADA":
                System.out.println("[PROCESADOR] Invitación de grupo rechazada");
                // Actualizar lista de invitaciones de grupo inmediatamente
                SwingUtilities.invokeLater(() -> {
                    try {
                        Cliente.getInstance().enviar(new Peticion("OBTENER_INVITACIONES_GRUPO", null));
                    } catch (Exception e) {
                        System.err.println("[PROCESADOR] Error actualizando invitaciones de grupo: " + e.getMessage());
                    }
                });
                break;
                
            // === ERRORES ===
            case "MENSAJE_ERROR":
            case "SOLICITUD_ERROR":
            case "INVITACION_ERROR":
            case "ERROR_GRUPO":
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null, 
                        "Error: " + p.getDatos(), 
                        "Error", 
                        JOptionPane.WARNING_MESSAGE);
                });
                break;
                
            default:
                System.out.println("[PROCESADOR] Acción no manejada: " + accion);
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
                // El servidor puede enviar solo la lista de amistades
                List<?> lista = (List<?>) datos;
                if (!lista.isEmpty()) {
                    // Convertir a formato que el panel entiende
                    amigosPanel.actualizarAmistades((List<models.Amistad>) lista);
                }
            }
        } catch (Exception e) {
            System.err.println("[PROCESADOR] Error procesando lista de amigos: " + e.getMessage());
        }
    }
    
    @SuppressWarnings("unchecked")
    private void procesarSolicitudesAmistad(Object datos) {
        try {
            if (datos instanceof List) {
                List<?> lista = (List<?>) datos;
                if (!lista.isEmpty() && lista.get(0) instanceof String) {
                    // Formato: "username:pk_amistad"
                    invitacionesPanel.actualizarSolicitudesTexto((List<String>) lista);
                } else if (!lista.isEmpty() && lista.get(0) instanceof Amigo) {
                    invitacionesPanel.actualizarInvitacionesAmigos((List<Amigo>) lista);
                }
            }
        } catch (Exception e) {
            System.err.println("[PROCESADOR] Error procesando solicitudes: " + e.getMessage());
        }
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
        } catch (Exception e) {
            System.err.println("[PROCESADOR] Error procesando invitaciones grupo: " + e.getMessage());
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
    
    @SuppressWarnings("unchecked")
    private void procesarMensajesPendientes(Object datos) {
        try {
            if (datos instanceof List) {
                List<MensajePendiente> pendientes = (List<MensajePendiente>) datos;
                System.out.println("[PROCESADOR] Procesando " + pendientes.size() + " mensajes pendientes");
                
                for (MensajePendiente mp : pendientes) {
                    SwingUtilities.invokeLater(() -> {
                        // Mostrar notificación de mensaje pendiente
                        String remitente = mp.getNombreRemitente() != null ? 
                            mp.getNombreRemitente() : "Usuario " + mp.getFk_remitente();
                        String tipoMsg = "grupo".equals(mp.getTipo()) ? 
                            " (en grupo " + (mp.getTituloGrupo() != null ? mp.getTituloGrupo() : mp.getFk_grupo()) + ")" : "";
                        
                        JOptionPane.showMessageDialog(null, 
                            "Mensaje de " + remitente + tipoMsg + ":\n" + mp.getMensaje(), 
                            "Mensaje Pendiente", 
                            JOptionPane.INFORMATION_MESSAGE);
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("[PROCESADOR] Error procesando mensajes pendientes: " + e.getMessage());
        }
    }
    
    @SuppressWarnings("unchecked")
    private void procesarHistorial(Object datos, boolean esGrupo) {
        try {
            if (datos instanceof Object[]) {
                Object[] paquete = (Object[]) datos;
                // paquete[0] = username/grupoId, paquete[1] = lista de mensajes
                Object identificador = paquete[0];
                List<Mensaje> mensajes = (List<Mensaje>) paquete[1];
                
                System.out.println("[PROCESADOR] Historial recibido con " + mensajes.size() + " mensajes");
                
                SwingUtilities.invokeLater(() -> {
                    String clave = null;
                    
                    if (esGrupo) {
                        int grupoId = (Integer) identificador;
                        clave = "grupo_" + grupoId;
                    } else {
                        // Buscar la ventana por username o ID
                        if (identificador instanceof String) {
                            // Buscar el ID del usuario por username
                            String username = (String) identificador;
                            for (Map.Entry<Integer, Usuario> entry : mapaUsuarios.entrySet()) {
                                if (entry.getValue().getUsername().equals(username)) {
                                    clave = "amigo_" + entry.getKey();
                                    if (!ventanasChatAbiertas.containsKey(clave)) {
                                        clave = "usuario_" + entry.getKey();
                                    }
                                    break;
                                }
                            }
                        } else if (identificador instanceof Integer) {
                            int userId = (Integer) identificador;
                            clave = "amigo_" + userId;
                            if (!ventanasChatAbiertas.containsKey(clave)) {
                                clave = "usuario_" + userId;
                            }
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
        } catch (Exception e) {
            System.err.println("[PROCESADOR] Error procesando historial: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

