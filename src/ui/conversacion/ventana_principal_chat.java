package ui.conversacion;
import chatcliente.Cliente;
import common.Peticion;
import models.Amigo;
import models.Grupo;
import models.InvitacionGrupo;
import models.Usuario;
import ui.gestion_amigos.ventana_gestion_amigos;
import ui.gestion_grupos.ventana_gestion_grupos;
import ui.conversacion.componentes.panel_amigos;
import ui.conversacion.componentes.panel_grupos;
import ui.conversacion.componentes.panel_invitaciones;
import ui.conversacion.componentes.panel_usuarios;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class ventana_principal_chat extends JFrame {
    private Usuario usuarioActual;
    private panel_usuarios usuariosPanel;
    private panel_amigos amigosPanel;
    private panel_grupos gruposPanel;
    private panel_invitaciones invitacionesPanel;
    private receptor_mensajes receptor;
    private procesador_peticiones procesador;
    private boolean activo;
    private Map<String, ventana_conversacion> ventanasChatAbiertas;
    private Map<Integer, Usuario> mapaUsuarios;
    public ventana_principal_chat(Usuario usuario) {
        super("Chat Principal - " + usuario.getNombre());
        this.usuarioActual = usuario;
        this.activo = true;
        this.ventanasChatAbiertas = new HashMap<>();
        this.mapaUsuarios = new HashMap<>();
        configurarVentana();
        inicializarComponentes();
        iniciarReceptorMensajes();
        solicitarDatosIniciales();
    }
    private void configurarVentana() {
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(5, 5));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cerrarChat();
            }
        });
    }
    private void inicializarComponentes() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(5, 5));
        panelPrincipal.setBorder(new EmptyBorder(10, 10, 10, 10));
        usuariosPanel = new panel_usuarios();
        usuariosPanel.setUsuarioActualId(usuarioActual.getPk_usuario());
        amigosPanel = new panel_amigos();
        amigosPanel.setUsuarioActualId(usuarioActual.getPk_usuario());
        gruposPanel = new panel_grupos();
        invitacionesPanel = new panel_invitaciones();
        configurarListeners();
        configurarBotonesInvitaciones();
        JPanel panelListas = new JPanel(new BorderLayout(5, 5));
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Usuarios", usuariosPanel);
        tabbedPane.addTab("Amigos", amigosPanel);
        tabbedPane.addTab("Grupos", gruposPanel);
        panelListas.add(tabbedPane, BorderLayout.CENTER);
        JPanel panelBotones = new JPanel(new GridLayout(2, 2, 5, 5));
        JButton btnSolicitud = new JButton("+ Solicitud Amistad");
        JButton btnAmigos = new JButton("Gestión Amigos");
        JButton btnGrupos = new JButton("Gestión Grupos");
        JButton btnActualizar = new JButton("Actualizar");
        btnSolicitud.addActionListener(e -> enviarSolicitudAmistad());
        btnAmigos.addActionListener(e -> {
            new ventana_gestion_amigos(this, usuarioActual.getPk_usuario()).setVisible(true);
            solicitarDatosIniciales();
        });
        btnGrupos.addActionListener(e -> {
            new ventana_gestion_grupos(this, usuarioActual.getPk_usuario()).setVisible(true);
            solicitarDatosIniciales();
        });
        btnActualizar.addActionListener(e -> solicitarDatosIniciales());
        panelBotones.add(btnSolicitud);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnAmigos);
        panelBotones.add(btnGrupos);
        panelListas.add(panelBotones, BorderLayout.SOUTH);
        panelListas.setPreferredSize(new Dimension(250, 0));
        JSplitPane splitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT, panelListas, invitacionesPanel);
        splitPane.setDividerLocation(400);
        panelPrincipal.add(splitPane, BorderLayout.CENTER);
        add(panelPrincipal, BorderLayout.CENTER);
    }
    private void configurarListeners() {
        usuariosPanel.addListSelectionListener(e -> {});
        usuariosPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && usuariosPanel.tieneSeleccion()) {
                    abrirChatUsuario();
                }
            }
            @Override
            public void mousePressed(MouseEvent e) {
                mostrarMenuContextual(e);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                mostrarMenuContextual(e);
            }
            private void mostrarMenuContextual(MouseEvent e) {
                if (e.isPopupTrigger() && usuariosPanel.tieneSeleccion()) {
                    JPopupMenu menu = new JPopupMenu();
                    JMenuItem itemChat = new JMenuItem("Abrir Chat");
                    itemChat.addActionListener(ev -> abrirChatUsuario());
                    JMenuItem itemSolicitud = new JMenuItem("Enviar Solicitud de Amistad");
                    itemSolicitud.addActionListener(ev -> enviarSolicitudAmistad());
                    menu.add(itemChat);
                    menu.add(itemSolicitud);
                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
        amigosPanel.addListSelectionListener(e -> {});
        amigosPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && amigosPanel.tieneSeleccion()) {
                    abrirChatAmigo();
                }
            }
        });
        gruposPanel.addListSelectionListener(e -> {});
        gruposPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && gruposPanel.tieneSeleccion()) {
                    abrirChatGrupo();
                }
            }
        });
    }
    private void enviarSolicitudAmistad() {
        if (!usuariosPanel.tieneSeleccion()) {
            JOptionPane.showMessageDialog(this, "Selecciona un usuario primero",
                "Información", JOptionPane.PLAIN_MESSAGE);
            return;
        }
        int usuarioId = usuariosPanel.getSeleccionId();
        String seleccionCompleta = usuariosPanel.getSeleccion();
        String nombreUsuario = extraerNombreUsuario(seleccionCompleta);
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Enviar solicitud de amistad a " + nombreUsuario + "?",
            "Confirmar Solicitud",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.PLAIN_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Peticion p = new Peticion("ENVIAR_SOLICITUD_AMISTAD", usuarioId);
                Cliente.getInstance().enviar(p);
                JOptionPane.showMessageDialog(this, "Solicitud enviada!",
                    "Información", JOptionPane.PLAIN_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error enviando solicitud: " + ex.getMessage(),
                    "Error", JOptionPane.PLAIN_MESSAGE);
            }
        }
    }
    private String extraerNombreUsuario(String seleccionCompleta) {
        if (seleccionCompleta == null) return "";
        String sinEstado = seleccionCompleta.replaceFirst("\\[Online\\]\\s*", "")
                                            .replaceFirst("\\[Offline\\]\\s*", "");
        String sinParentesis = sinEstado.replaceAll("\\s*\\([^)]*\\)", "");
        return sinParentesis.trim();
    }
    private void configurarBotonesInvitaciones() {
        invitacionesPanel.getBtnAceptarAmigo().addActionListener(e -> {
            if (!invitacionesPanel.tieneSeleccionAmigo()) {
                JOptionPane.showMessageDialog(this, "Por favor selecciona una invitación primero.",
                    "Información", JOptionPane.PLAIN_MESSAGE);
                return;
            }
            int invitacionId = invitacionesPanel.getSeleccionAmigoId();
            if (invitacionId == -1) {
                JOptionPane.showMessageDialog(this, "Error al obtener el ID de la invitación.",
                    "Error", JOptionPane.PLAIN_MESSAGE);
                return;
            }
            responderInvitacionAmigo(invitacionId, true);
        });
        invitacionesPanel.getBtnRechazarAmigo().addActionListener(e -> {
            if (!invitacionesPanel.tieneSeleccionAmigo()) {
                JOptionPane.showMessageDialog(this, "Por favor selecciona una invitación primero.",
                    "Información", JOptionPane.PLAIN_MESSAGE);
                return;
            }
            int invitacionId = invitacionesPanel.getSeleccionAmigoId();
            if (invitacionId == -1) {
                JOptionPane.showMessageDialog(this, "Error al obtener el ID de la invitación.",
                    "Error", JOptionPane.PLAIN_MESSAGE);
                return;
            }
            responderInvitacionAmigo(invitacionId, false);
        });
        invitacionesPanel.getBtnAceptarGrupo().addActionListener(e -> {
            if (!invitacionesPanel.tieneSeleccionGrupo()) {
                JOptionPane.showMessageDialog(this, "Por favor selecciona una invitación primero.",
                    "Información", JOptionPane.PLAIN_MESSAGE);
                return;
            }
            int invitacionId = invitacionesPanel.getSeleccionGrupoId();
            if (invitacionId == -1) {
                JOptionPane.showMessageDialog(this, "Error al obtener el ID de la invitación.",
                    "Error", JOptionPane.PLAIN_MESSAGE);
                return;
            }
            responderInvitacionGrupo(invitacionId, true);
        });
        invitacionesPanel.getBtnRechazarGrupo().addActionListener(e -> {
            if (!invitacionesPanel.tieneSeleccionGrupo()) {
                JOptionPane.showMessageDialog(this, "Por favor selecciona una invitación primero.",
                    "Información", JOptionPane.PLAIN_MESSAGE);
                return;
            }
            int invitacionId = invitacionesPanel.getSeleccionGrupoId();
            if (invitacionId == -1) {
                JOptionPane.showMessageDialog(this, "Error al obtener el ID de la invitación.",
                    "Error", JOptionPane.PLAIN_MESSAGE);
                return;
            }
            responderInvitacionGrupo(invitacionId, false);
        });
    }
    private void abrirChatUsuario() {
        String seleccion = usuariosPanel.getSeleccion();
        if (seleccion == null) {
            return;
        }
        int usuarioId = usuariosPanel.getSeleccionId();
        Usuario destinatario = mapaUsuarios.get(usuarioId);
        if (destinatario == null) {
            JOptionPane.showMessageDialog(this, "Usuario no encontrado.",
                "Error", JOptionPane.PLAIN_MESSAGE);
            return;
        }
        String clave = "usuario_" + usuarioId;
        ventana_conversacion ventanaExistente = ventanasChatAbiertas.get(clave);
        if (ventanaExistente != null && ventanaExistente.isVisible()) {
            ventanaExistente.toFront();
            return;
        }
        if (ventanaExistente != null) {
            ventanasChatAbiertas.remove(clave);
        }
        ventana_conversacion ventanaChat = new ventana_conversacion(usuarioActual, destinatario);
        ventanaChat.setVisible(true);
        ventanasChatAbiertas.put(clave, ventanaChat);
    }
    private void abrirChatAmigo() {
        if (!amigosPanel.tieneSeleccion()) {
            return;
        }
        int amigoId = amigosPanel.getSeleccionId();
        String clave = "amigo_" + amigoId;
        ventana_conversacion ventanaExistente = ventanasChatAbiertas.get(clave);
        if (ventanaExistente != null && ventanaExistente.isVisible()) {
            ventanaExistente.toFront();
            return;
        }
        if (ventanaExistente != null) {
            ventanasChatAbiertas.remove(clave);
        }
        Usuario amigo = mapaUsuarios.get(amigoId);
        if (amigo == null) {
            JOptionPane.showMessageDialog(this, "Amigo no encontrado.",
                "Error", JOptionPane.PLAIN_MESSAGE);
            return;
        }
        ventana_conversacion ventanaChat = new ventana_conversacion(usuarioActual, amigo);
        ventanaChat.setVisible(true);
        ventanasChatAbiertas.put(clave, ventanaChat);
    }
    private void abrirChatGrupo() {
        if (!gruposPanel.tieneSeleccion()) {
            return;
        }
        int grupoId = gruposPanel.getSeleccionId();
        String clave = "grupo_" + grupoId;
        ventana_conversacion ventanaExistente = ventanasChatAbiertas.get(clave);
        if (ventanaExistente != null && ventanaExistente.isVisible()) {
            ventanaExistente.toFront();
            return;
        }
        if (ventanaExistente != null) {
            ventanasChatAbiertas.remove(clave);
        }
        String titulo = gruposPanel.getSeleccion().replace("[Grupo] ", "");
        ventana_conversacion ventanaChat = new ventana_conversacion(usuarioActual, grupoId, titulo);
        ventanaChat.setVisible(true);
        ventanasChatAbiertas.put(clave, ventanaChat);
    }
    private void responderInvitacionAmigo(int invitacionId, boolean aceptar) {
        if (!Cliente.getInstance().estaConectado()) {
            JOptionPane.showMessageDialog(this, "No hay conexión con el servidor.",
                "Error de Conexión", JOptionPane.PLAIN_MESSAGE);
            return;
        }
        if (invitacionId <= 0) {
            JOptionPane.showMessageDialog(this, "ID de invitación inválido.",
                "Error", JOptionPane.PLAIN_MESSAGE);
            return;
        }
        try {
            Peticion p = new Peticion(aceptar ? "ACEPTAR_SOLICITUD_AMISTAD" : "RECHAZAR_SOLICITUD_AMISTAD",
                invitacionId);
            Cliente.getInstance().enviar(p);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al procesar la invitación: " + ex.getMessage(),
                "Error", JOptionPane.PLAIN_MESSAGE);
        }
    }
    private void responderInvitacionGrupo(int invitacionId, boolean aceptar) {
        if (!Cliente.getInstance().estaConectado()) {
            return;
        }
        try {
            Peticion p = new Peticion(aceptar ? "ACEPTAR_INVITACION_GRUPO" :
                "RECHAZAR_INVITACION_GRUPO", invitacionId);
            Cliente.getInstance().enviar(p);
            Cliente.getInstance().recibir();
            solicitarDatosIniciales();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                "Error", JOptionPane.PLAIN_MESSAGE);
        }
    }
    private void solicitarDatosIniciales() {
        if (!Cliente.getInstance().estaConectado()) {
            return;
        }
        try {
            Cliente.getInstance().enviar(new Peticion("OBTENER_USUARIOS", null));
            Cliente.getInstance().enviar(new Peticion("OBTENER_AMIGOS", usuarioActual.getPk_usuario()));
            Cliente.getInstance().enviar(new Peticion("OBTENER_GRUPOS", usuarioActual.getPk_usuario()));
            Cliente.getInstance().enviar(new Peticion("OBTENER_SOLICITUDES", null));
            Cliente.getInstance().enviar(new Peticion("OBTENER_INVITACIONES_GRUPO", usuarioActual.getPk_usuario()));
        } catch (Exception ex) {}
    }
    private void iniciarReceptorMensajes() {
        if (receptor != null && receptor.isAlive()) {
            receptor.detener();
            try {
                receptor.join(1000);
            } catch (InterruptedException e) {}
        }
        procesador = new procesador_peticiones(usuariosPanel, amigosPanel,
            gruposPanel, invitacionesPanel, ventanasChatAbiertas, mapaUsuarios);
        if (Cliente.getInstance().estaConectado()) {
            receptor = new receptor_mensajes(null, procesador);
            receptor.start();
        }
    }
    private void cerrarChat() {
        if (receptor != null && receptor.isAlive()) {
            receptor.detener();
            try {
                receptor.join(1000);
            } catch (InterruptedException e) {}
        }
        int opcion = JOptionPane.showConfirmDialog(
            this,
            "¿Estás seguro de que deseas salir?",
            "Confirmar Salida",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );
        if (opcion == JOptionPane.YES_OPTION) {
            activo = false;
            if (receptor != null) {
                receptor.detener();
            }
            for (ventana_conversacion ventana : ventanasChatAbiertas.values()) {
                ventana.dispose();
            }
            if (Cliente.getInstance().estaConectado()) {
                try {
                    Peticion p = new Peticion("DESCONECTAR", usuarioActual);
                    Cliente.getInstance().enviar(p);
                } catch (IOException ex) {}
            }
            Cliente.getInstance().cerrar();
            dispose();
            System.exit(0);
        }
    }
}
