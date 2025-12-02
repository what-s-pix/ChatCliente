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
        // Clic simple para seleccionar
        usuariosPanel.addListSelectionListener(e -> {
            // No abrir chat automáticamente en selección simple
        });
        
        // Doble clic para abrir chat
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
        
        amigosPanel.addListSelectionListener(e -> {
            // No abrir chat automáticamente
        });
        
        amigosPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && amigosPanel.tieneSeleccion()) {
                    abrirChatAmigo();
                }
            }
        });
        
        gruposPanel.addListSelectionListener(e -> {
            // No abrir chat automáticamente
        });
        
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
            JOptionPane.showMessageDialog(this, "Selecciona un usuario primero");
            return;
        }
        
        int usuarioId = usuariosPanel.getSeleccionId();
        String nombreUsuario = usuariosPanel.getSeleccion();
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Enviar solicitud de amistad a " + nombreUsuario + "?",
            "Confirmar Solicitud",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Peticion p = new Peticion("ENVIAR_SOLICITUD_AMISTAD", usuarioId);
                Cliente.getInstance().enviar(p);
                JOptionPane.showMessageDialog(this, "Solicitud enviada!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error enviando solicitud: " + ex.getMessage());
            }
        }
    }
    
    private void configurarBotonesInvitaciones() {
        invitacionesPanel.getBtnAceptarAmigo().addActionListener(e -> {
            int invitacionId = invitacionesPanel.getSeleccionAmigoId();
            if (invitacionId != -1) {
                responderInvitacionAmigo(invitacionId, true);
            }
        });
        
        invitacionesPanel.getBtnRechazarAmigo().addActionListener(e -> {
            int invitacionId = invitacionesPanel.getSeleccionAmigoId();
            if (invitacionId != -1) {
                responderInvitacionAmigo(invitacionId, false);
            }
        });
        
        invitacionesPanel.getBtnAceptarGrupo().addActionListener(e -> {
            int invitacionId = invitacionesPanel.getSeleccionGrupoId();
            if (invitacionId != -1) {
                responderInvitacionGrupo(invitacionId, true);
            }
        });
        
        invitacionesPanel.getBtnRechazarGrupo().addActionListener(e -> {
            int invitacionId = invitacionesPanel.getSeleccionGrupoId();
            if (invitacionId != -1) {
                responderInvitacionGrupo(invitacionId, false);
            }
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
            JOptionPane.showMessageDialog(this, "Usuario no encontrado.");
            return;
        }
        
        String clave = "usuario_" + usuarioId;
        if (ventanasChatAbiertas.containsKey(clave)) {
            ventanasChatAbiertas.get(clave).toFront();
            return;
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
        
        if (ventanasChatAbiertas.containsKey(clave)) {
            ventanasChatAbiertas.get(clave).toFront();
            return;
        }
        
        Usuario amigo = mapaUsuarios.get(amigoId);
        if (amigo == null) {
            JOptionPane.showMessageDialog(this, "Amigo no encontrado.");
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
        
        if (ventanasChatAbiertas.containsKey(clave)) {
            ventanasChatAbiertas.get(clave).toFront();
            return;
        }
        
        String titulo = gruposPanel.getSeleccion().replace("[Grupo] ", "");
        ventana_conversacion ventanaChat = new ventana_conversacion(usuarioActual, grupoId, titulo);
        ventanaChat.setVisible(true);
        ventanasChatAbiertas.put(clave, ventanaChat);
    }
    
    private void responderInvitacionAmigo(int invitacionId, boolean aceptar) {
        if (!Cliente.getInstance().estaConectado()) {
            return;
        }
        try {
            Peticion p = new Peticion(aceptar ? "ACEPTAR_INVITACION" : "RECHAZAR_INVITACION",
                invitacionId);
            Cliente.getInstance().enviar(p);
            Cliente.getInstance().recibir();
            
            solicitarDatosIniciales();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
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
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
    
    private void solicitarDatosIniciales() {
        if (!Cliente.getInstance().estaConectado()) {
            System.out.println("[VENTANA_PRINCIPAL] Cliente no conectado, no se pueden solicitar datos");
            return;
        }
        try {
            System.out.println("[VENTANA_PRINCIPAL] Solicitando datos iniciales...");
            
            // Solicitar TODOS los usuarios (conectados y desconectados)
            Cliente.getInstance().enviar(new Peticion("OBTENER_USUARIOS", null));
            
            // Solicitar amigos
            Cliente.getInstance().enviar(new Peticion("OBTENER_AMIGOS", usuarioActual.getPk_usuario()));
            
            // Solicitar grupos
            Cliente.getInstance().enviar(new Peticion("OBTENER_GRUPOS", usuarioActual.getPk_usuario()));
            
            // Solicitar solicitudes de amistad pendientes
            Cliente.getInstance().enviar(new Peticion("OBTENER_SOLICITUDES", null));
            
            // Solicitar invitaciones a grupos
            Cliente.getInstance().enviar(new Peticion("OBTENER_INVITACIONES_GRUPO", usuarioActual.getPk_usuario()));
            
            System.out.println("[VENTANA_PRINCIPAL] Todas las peticiones enviadas");
        } catch (Exception ex) {
            System.err.println("[VENTANA_PRINCIPAL] Error solicitando datos: " + ex.getMessage());
        }
    }
    
    private void iniciarReceptorMensajes() {
        // Asegurarse de que no haya un receptor anterior corriendo
        if (receptor != null && receptor.isAlive()) {
            System.out.println("[VENTANA_PRINCIPAL] Deteniendo receptor anterior...");
            receptor.detener();
            try {
                receptor.join(1000); // Esperar hasta 1 segundo a que termine
            } catch (InterruptedException e) {}
        }
        
        procesador = new procesador_peticiones(usuariosPanel, amigosPanel, 
            gruposPanel, invitacionesPanel, ventanasChatAbiertas, mapaUsuarios);
        if (Cliente.getInstance().estaConectado()) {
            System.out.println("[VENTANA_PRINCIPAL] Iniciando nuevo receptor de mensajes...");
            receptor = new receptor_mensajes(null, procesador);
            receptor.start();
        } else {
            System.out.println("[VENTANA_PRINCIPAL] Cliente no conectado, no se puede iniciar receptor");
        }
    }
    
    private void cerrarChat() {
        // Detener el receptor antes de cerrar
        if (receptor != null && receptor.isAlive()) {
            System.out.println("[VENTANA_PRINCIPAL] Deteniendo receptor al cerrar chat...");
            receptor.detener();
            try {
                receptor.join(1000);
            } catch (InterruptedException e) {}
        }
        int opcion = JOptionPane.showConfirmDialog(
            this,
            "¿Estás seguro de que deseas salir?",
            "Confirmar Salida",
            JOptionPane.YES_NO_OPTION
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
                } catch (IOException ex) {
                }
            }
            
            Cliente.getInstance().cerrar();
            dispose();
            System.exit(0);
        }
    }
}

