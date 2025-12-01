package chatcliente;

import common.Peticion;
import models.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ChatUI extends JFrame {
    
    private Usuario usuarioActual;
    private Cliente cliente;
    
    // Componentes principales
    private JList<String> listaUsuarios;
    private JList<String> listaAmigos;
    private JList<String> listaGrupos;
    private DefaultListModel<String> modeloUsuarios;
    private DefaultListModel<String> modeloAmigos;
    private DefaultListModel<String> modeloGrupos;
    
    private JTextArea areaChat;
    private JTextField campoMensaje;
    private JButton btnEnviar;
    
    private JButton btnAgregarAmigo;
    private JButton btnCrearGrupo;
    private JButton btnSolicitudes;
    
    // Datos
    private List<Usuario> usuarios;
    private List<Amistad> amigos;
    private List<Grupo> grupos;
    private Usuario destinatarioActual;
    private Grupo grupoActual;
    private boolean esChatPrivado = true;
    
    // Hilo para recibir mensajes
    private Thread hiloRecepcion;
    private boolean activo = true;
    
    public ChatUI(Usuario usuario) {
        this.usuarioActual = usuario;
        this.cliente = Cliente.getInstance();
        
        configurarVentana();
        inicializarComponentes();
        cargarDatos();
        iniciarHiloRecepcion();
    }
    
    private void configurarVentana() {
        setTitle("What's Pix - " + usuarioActual.getNombre());
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cerrarSesion();
            }
        });
    }
    
    private void inicializarComponentes() {
        setLayout(new BorderLayout());
        
        // Panel izquierdo: Listas
        JPanel panelIzquierdo = new JPanel(new BorderLayout());
        
        // Tabs para usuarios, amigos y grupos
        JTabbedPane tabs = new JTabbedPane();
        
        // Tab Usuarios
        modeloUsuarios = new DefaultListModel<>();
        listaUsuarios = new JList<>(modeloUsuarios);
        listaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaUsuarios.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listaUsuarios.getSelectedIndex() >= 0) {
                seleccionarUsuario();
            }
        });
        JScrollPane scrollUsuarios = new JScrollPane(listaUsuarios);
        tabs.addTab("Usuarios", scrollUsuarios);
        
        // Tab Amigos
        modeloAmigos = new DefaultListModel<>();
        listaAmigos = new JList<>(modeloAmigos);
        listaAmigos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaAmigos.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listaAmigos.getSelectedIndex() >= 0) {
                seleccionarAmigo();
            }
        });
        JScrollPane scrollAmigos = new JScrollPane(listaAmigos);
        tabs.addTab("Amigos", scrollAmigos);
        
        // Tab Grupos
        modeloGrupos = new DefaultListModel<>();
        listaGrupos = new JList<>(modeloGrupos);
        listaGrupos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaGrupos.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listaGrupos.getSelectedIndex() >= 0) {
                seleccionarGrupo();
            }
        });
        JScrollPane scrollGrupos = new JScrollPane(listaGrupos);
        tabs.addTab("Grupos", scrollGrupos);
        
        panelIzquierdo.add(tabs, BorderLayout.CENTER);
        
        // Botones de acción
        JPanel panelBotones = new JPanel(new FlowLayout());
        btnAgregarAmigo = new JButton("Agregar Amigo");
        btnAgregarAmigo.addActionListener(e -> agregarAmigo());
        btnCrearGrupo = new JButton("Crear Grupo");
        btnCrearGrupo.addActionListener(e -> crearGrupo());
        btnSolicitudes = new JButton("Solicitudes");
        btnSolicitudes.addActionListener(e -> verSolicitudes());
        
        panelBotones.add(btnAgregarAmigo);
        panelBotones.add(btnCrearGrupo);
        panelBotones.add(btnSolicitudes);
        
        panelIzquierdo.add(panelBotones, BorderLayout.SOUTH);
        
        // Panel central: Chat
        JPanel panelCentral = new JPanel(new BorderLayout());
        
        areaChat = new JTextArea();
        areaChat.setEditable(false);
        areaChat.setFont(new Font("Arial", Font.PLAIN, 12));
        JScrollPane scrollChat = new JScrollPane(areaChat);
        
        JPanel panelMensaje = new JPanel(new BorderLayout());
        campoMensaje = new JTextField();
        campoMensaje.addActionListener(e -> enviarMensaje());
        btnEnviar = new JButton("Enviar");
        btnEnviar.addActionListener(e -> enviarMensaje());
        
        panelMensaje.add(campoMensaje, BorderLayout.CENTER);
        panelMensaje.add(btnEnviar, BorderLayout.EAST);
        
        panelCentral.add(scrollChat, BorderLayout.CENTER);
        panelCentral.add(panelMensaje, BorderLayout.SOUTH);
        
        // Layout principal
        add(panelIzquierdo, BorderLayout.WEST);
        add(panelCentral, BorderLayout.CENTER);
        
        // Panel superior con info
        JLabel lblInfo = new JLabel("Usuario: " + usuarioActual.getNombre() + " | Username: " + usuarioActual.getUsername());
        add(lblInfo, BorderLayout.NORTH);
    }
    
    private void cargarDatos() {
        try {
            // Cargar usuarios
            cliente.enviar(new Peticion("OBTENER_USUARIOS", null));
            Peticion respuesta = cliente.recibir();
            if (respuesta.getAccion().equals("LISTA_USUARIOS")) {
                usuarios = (List<Usuario>) respuesta.getDatos();
                actualizarListaUsuarios();
            }
            
            // Cargar amigos
            cliente.enviar(new Peticion("OBTENER_AMIGOS", null));
            respuesta = cliente.recibir();
            if (respuesta.getAccion().equals("LISTA_AMIGOS")) {
                amigos = (List<Amistad>) respuesta.getDatos();
                actualizarListaAmigos();
            }
            
            // Cargar grupos
            cliente.enviar(new Peticion("OBTENER_GRUPOS", null));
            respuesta = cliente.recibir();
            if (respuesta.getAccion().equals("LISTA_GRUPOS")) {
                grupos = (List<Grupo>) respuesta.getDatos();
                actualizarListaGrupos();
            }
            
            // Cargar invitaciones pendientes
            cliente.enviar(new Peticion("OBTENER_INVITACIONES_GRUPO", null));
            respuesta = cliente.recibir();
            if (respuesta.getAccion().equals("LISTA_INVITACIONES_GRUPO")) {
                List<InvitacionGrupo> invitaciones = (List<InvitacionGrupo>) respuesta.getDatos();
                if (!invitaciones.isEmpty()) {
                    mostrarInvitaciones(invitaciones);
                }
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar datos: " + e.getMessage());
        }
    }
    
    private void actualizarListaUsuarios() {
        modeloUsuarios.clear();
        if (usuarios != null) {
            for (Usuario u : usuarios) {
                String estado = u.getEstado() == 1 ? "● Conectado" : "○ Desconectado";
                modeloUsuarios.addElement(u.getNombre() + " (" + u.getUsername() + ") - " + estado);
            }
        }
    }
    
    private void actualizarListaAmigos() {
        modeloAmigos.clear();
        if (amigos != null) {
            for (Amistad a : amigos) {
                modeloAmigos.addElement(a.getNombreUsuario());
            }
        }
    }
    
    private void actualizarListaGrupos() {
        modeloGrupos.clear();
        if (grupos != null) {
            for (Grupo g : grupos) {
                modeloGrupos.addElement(g.getTitulo() + " (Creador: " + g.getNombreCreador() + ")");
            }
        }
    }
    
    private void seleccionarUsuario() {
        int index = listaUsuarios.getSelectedIndex();
        if (index >= 0 && usuarios != null && index < usuarios.size()) {
            Usuario seleccionado = usuarios.get(index);
            if (seleccionado.getPk_usuario() != usuarioActual.getPk_usuario()) {
                destinatarioActual = seleccionado;
                grupoActual = null;
                esChatPrivado = true;
                areaChat.setText("=== Chat con " + seleccionado.getNombre() + " ===\n");
                areaChat.append("(Nota: Solo se guarda historial entre amigos)\n\n");
                campoMensaje.setEnabled(false); // Deshabilitar hasta verificar si es amigo
            }
        }
    }
    
    private void seleccionarAmigo() {
        int index = listaAmigos.getSelectedIndex();
        if (index >= 0 && amigos != null && index < amigos.size()) {
            Amistad amistad = amigos.get(index);
            int idOtro = (amistad.getFk_usuario1() == usuarioActual.getPk_usuario()) ? 
                         amistad.getFk_usuario2() : amistad.getFk_usuario1();
            
            // Buscar el usuario completo
            for (Usuario u : usuarios) {
                if (u.getPk_usuario() == idOtro) {
                    destinatarioActual = u;
                    grupoActual = null;
                    esChatPrivado = true;
                    areaChat.setText("=== Chat con " + u.getNombre() + " (Amigo) ===\n\n");
                    campoMensaje.setEnabled(true);
                    
                    // Cargar historial
                    cargarHistorial(idOtro);
                    break;
                }
            }
        }
    }
    
    private void seleccionarGrupo() {
        int index = listaGrupos.getSelectedIndex();
        if (index >= 0 && grupos != null && index < grupos.size()) {
            grupoActual = grupos.get(index);
            destinatarioActual = null;
            esChatPrivado = false;
            areaChat.setText("=== Grupo: " + grupoActual.getTitulo() + " ===\n\n");
            campoMensaje.setEnabled(true);
            
            // Cargar historial del grupo
            cargarHistorialGrupo(grupoActual.getPk_grupo());
        }
    }
    
    private void cargarHistorial(int idOtroUsuario) {
        try {
            cliente.enviar(new Peticion("OBTENER_HISTORIAL", idOtroUsuario));
            Peticion respuesta = cliente.recibir();
            if (respuesta.getAccion().equals("HISTORIAL_MENSAJES")) {
                List<Mensaje> historial = (List<Mensaje>) respuesta.getDatos();
                for (Mensaje m : historial) {
                    String remitente = (m.getFk_remitente() == usuarioActual.getPk_usuario()) ? 
                                      "Tú" : m.getNombreRemitente();
                    areaChat.append("[" + m.getFecha_envio() + "] " + remitente + ": " + m.getMensaje() + "\n");
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar historial: " + e.getMessage());
        }
    }
    
    private void cargarHistorialGrupo(int idGrupo) {
        try {
            cliente.enviar(new Peticion("OBTENER_HISTORIAL_GRUPO", idGrupo));
            Peticion respuesta = cliente.recibir();
            if (respuesta.getAccion().equals("HISTORIAL_GRUPO")) {
                List<MensajeGrupo> historial = (List<MensajeGrupo>) respuesta.getDatos();
                for (MensajeGrupo m : historial) {
                    String remitente = (m.getFk_remitente() == usuarioActual.getPk_usuario()) ? 
                                      "Tú" : m.getNombreRemitente();
                    areaChat.append("[" + m.getFecha_envio() + "] " + remitente + ": " + m.getMensaje() + "\n");
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar historial: " + e.getMessage());
        }
    }
    
    private void enviarMensaje() {
        String texto = campoMensaje.getText().trim();
        if (texto.isEmpty()) return;
        
        try {
            if (esChatPrivado && destinatarioActual != null) {
                Mensaje mensaje = new Mensaje(usuarioActual.getPk_usuario(), 
                                             destinatarioActual.getPk_usuario(), texto);
                cliente.enviar(new Peticion("ENVIAR_MENSAJE_PRIVADO", mensaje));
                Peticion respuesta = cliente.recibir();
                
                if (respuesta.getAccion().equals("MENSAJE_ENVIADO")) {
                    areaChat.append("[Ahora] Tú: " + texto + "\n");
                    campoMensaje.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Error: " + respuesta.getDatos());
                }
            } else if (!esChatPrivado && grupoActual != null) {
                MensajeGrupo mensaje = new MensajeGrupo();
                mensaje.setFk_grupo(grupoActual.getPk_grupo());
                mensaje.setFk_remitente(usuarioActual.getPk_usuario());
                mensaje.setMensaje(texto);
                cliente.enviar(new Peticion("ENVIAR_MENSAJE_GRUPO", mensaje));
                Peticion respuesta = cliente.recibir();
                
                if (respuesta.getAccion().equals("MENSAJE_ENVIADO")) {
                    areaChat.append("[Ahora] Tú: " + texto + "\n");
                    campoMensaje.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Error: " + respuesta.getDatos());
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al enviar mensaje: " + e.getMessage());
        }
    }
    
    private void agregarAmigo() {
        if (usuarios == null || usuarios.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay usuarios disponibles");
            return;
        }
        
        String[] nombres = usuarios.stream()
            .filter(u -> u.getPk_usuario() != usuarioActual.getPk_usuario())
            .map(u -> u.getNombre() + " (" + u.getUsername() + ")")
            .toArray(String[]::new);
        
        String seleccionado = (String) JOptionPane.showInputDialog(this,
            "Selecciona un usuario para agregar como amigo:",
            "Agregar Amigo",
            JOptionPane.QUESTION_MESSAGE,
            null,
            nombres,
            nombres[0]);
        
        if (seleccionado != null) {
            // Encontrar el usuario seleccionado
            for (Usuario u : usuarios) {
                if (seleccionado.contains(u.getUsername())) {
                    try {
                        cliente.enviar(new Peticion("ENVIAR_SOLICITUD_AMISTAD", u.getPk_usuario()));
                        Peticion respuesta = cliente.recibir();
                        if (respuesta.getAccion().equals("SOLICITUD_ENVIADA")) {
                            JOptionPane.showMessageDialog(this, "Solicitud de amistad enviada");
                        } else {
                            JOptionPane.showMessageDialog(this, "Error: " + respuesta.getDatos());
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
                    }
                    break;
                }
            }
        }
    }
    
    private void crearGrupo() {
        String titulo = JOptionPane.showInputDialog(this, "Ingresa el título del grupo:");
        if (titulo == null || titulo.trim().isEmpty()) return;
        
        try {
            Grupo grupo = new Grupo(titulo.trim(), usuarioActual.getPk_usuario());
            cliente.enviar(new Peticion("CREAR_GRUPO", grupo));
            Peticion respuesta = cliente.recibir();
            
            if (respuesta.getAccion().equals("GRUPO_CREADO")) {
                Grupo nuevoGrupo = (Grupo) respuesta.getDatos();
                if (grupos == null) grupos = new ArrayList<>();
                grupos.add(nuevoGrupo);
                actualizarListaGrupos();
                JOptionPane.showMessageDialog(this, "Grupo creado. Ahora invita a al menos 2 personas más.");
                
                // Invitar usuarios
                invitarAGrupo(nuevoGrupo.getPk_grupo());
            } else {
                JOptionPane.showMessageDialog(this, "Error: " + respuesta.getDatos());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private void invitarAGrupo(int idGrupo) {
        if (usuarios == null || usuarios.isEmpty()) {
            return;
        }
        
        String[] nombres = usuarios.stream()
            .filter(u -> u.getPk_usuario() != usuarioActual.getPk_usuario())
            .map(u -> u.getNombre() + " (" + u.getUsername() + ")")
            .toArray(String[]::new);
        
        String seleccionado = (String) JOptionPane.showInputDialog(this,
            "Selecciona un usuario para invitar al grupo:",
            "Invitar al Grupo",
            JOptionPane.QUESTION_MESSAGE,
            null,
            nombres,
            nombres.length > 0 ? nombres[0] : null);
        
        if (seleccionado != null) {
            for (Usuario u : usuarios) {
                if (seleccionado.contains(u.getUsername())) {
                    try {
                        Object[] datos = {idGrupo, u.getPk_usuario()};
                        cliente.enviar(new Peticion("INVITAR_A_GRUPO", datos));
                        Peticion respuesta = cliente.recibir();
                        if (respuesta.getAccion().equals("INVITACION_ENVIADA")) {
                            JOptionPane.showMessageDialog(this, "Invitación enviada");
                        } else {
                            JOptionPane.showMessageDialog(this, "Error: " + respuesta.getDatos());
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
                    }
                    break;
                }
            }
        }
    }
    
    private void verSolicitudes() {
        try {
            cliente.enviar(new Peticion("OBTENER_SOLICITUDES", null));
            Peticion respuesta = cliente.recibir();
            if (respuesta.getAccion().equals("LISTA_SOLICITUDES")) {
                List<Amistad> solicitudes = (List<Amistad>) respuesta.getDatos();
                if (solicitudes.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No tienes solicitudes pendientes");
                } else {
                    mostrarSolicitudesAmistad(solicitudes);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private void mostrarSolicitudesAmistad(List<Amistad> solicitudes) {
        for (Amistad s : solicitudes) {
            int opcion = JOptionPane.showOptionDialog(this,
                "Solicitud de amistad de: " + s.getNombreUsuario(),
                "Solicitud de Amistad",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"Aceptar", "Rechazar"},
                "Aceptar");
            
            try {
                if (opcion == 0) { // Aceptar
                    cliente.enviar(new Peticion("ACEPTAR_SOLICITUD_AMISTAD", s.getPk_amistad()));
                    cliente.recibir();
                    JOptionPane.showMessageDialog(this, "Solicitud aceptada");
                    cargarDatos(); // Recargar lista de amigos
                } else if (opcion == 1) { // Rechazar
                    cliente.enviar(new Peticion("RECHAZAR_SOLICITUD_AMISTAD", s.getPk_amistad()));
                    cliente.recibir();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }
    
    private void mostrarInvitaciones(List<InvitacionGrupo> invitaciones) {
        for (InvitacionGrupo inv : invitaciones) {
            int opcion = JOptionPane.showOptionDialog(this,
                "Invitación al grupo: " + inv.getTituloGrupo() + "\nDe: " + inv.getNombreInvitador(),
                "Invitación a Grupo",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"Aceptar", "Rechazar"},
                "Aceptar");
            
            try {
                if (opcion == 0) { // Aceptar
                    cliente.enviar(new Peticion("ACEPTAR_INVITACION_GRUPO", inv.getPk_invitacion()));
                    Peticion respuesta = cliente.recibir();
                    if (respuesta.getAccion().equals("INVITACION_ACEPTADA")) {
                        JOptionPane.showMessageDialog(this, "Invitación aceptada");
                        cargarDatos(); // Recargar grupos
                    } else if (respuesta.getAccion().equals("GRUPO_ELIMINADO")) {
                        JOptionPane.showMessageDialog(this, respuesta.getDatos().toString());
                    }
                } else if (opcion == 1) { // Rechazar
                    cliente.enviar(new Peticion("RECHAZAR_INVITACION_GRUPO", inv.getPk_invitacion()));
                    Peticion respuesta = cliente.recibir();
                    if (respuesta.getAccion().equals("GRUPO_ELIMINADO")) {
                        JOptionPane.showMessageDialog(this, respuesta.getDatos().toString());
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }
    
    private void iniciarHiloRecepcion() {
        hiloRecepcion = new Thread(() -> {
            while (activo) {
                try {
                    Peticion peticion = cliente.recibir();
                    SwingUtilities.invokeLater(() -> procesarPeticionRecibida(peticion));
                } catch (Exception e) {
                    if (activo) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, "Error de conexión: " + e.getMessage());
                        });
                    }
                    break;
                }
            }
        });
        hiloRecepcion.start();
    }
    
    private void procesarPeticionRecibida(Peticion p) {
        switch (p.getAccion()) {
            case "MENSAJE_PRIVADO":
                Mensaje mensaje = (Mensaje) p.getDatos();
                if (destinatarioActual != null && mensaje.getFk_remitente() == destinatarioActual.getPk_usuario()) {
                    areaChat.append("[Ahora] " + mensaje.getNombreRemitente() + ": " + mensaje.getMensaje() + "\n");
                } else {
                    JOptionPane.showMessageDialog(this, "Nuevo mensaje de: " + mensaje.getNombreRemitente());
                }
                break;
                
            case "MENSAJE_GRUPO":
                MensajeGrupo mensajeGrupo = (MensajeGrupo) p.getDatos();
                if (grupoActual != null && mensajeGrupo.getFk_grupo() == grupoActual.getPk_grupo()) {
                    String remitente = (mensajeGrupo.getFk_remitente() == usuarioActual.getPk_usuario()) ? 
                                      "Tú" : mensajeGrupo.getNombreRemitente();
                    areaChat.append("[Ahora] " + remitente + ": " + mensajeGrupo.getMensaje() + "\n");
                } else {
                    JOptionPane.showMessageDialog(this, "Nuevo mensaje en grupo: " + mensajeGrupo.getTituloGrupo());
                }
                break;
                
            case "NUEVA_SOLICITUD_AMISTAD":
                JOptionPane.showMessageDialog(this, "Tienes una nueva solicitud de amistad");
                cargarDatos();
                break;
                
            case "NUEVA_INVITACION_GRUPO":
                InvitacionGrupo inv = (InvitacionGrupo) p.getDatos();
                mostrarInvitaciones(java.util.Arrays.asList(inv));
                break;
                
            case "MENSAJES_PENDIENTES":
                List<MensajePendiente> pendientes = (List<MensajePendiente>) p.getDatos();
                if (!pendientes.isEmpty()) {
                    StringBuilder sb = new StringBuilder("Tienes " + pendientes.size() + " mensajes pendientes:\n");
                    for (MensajePendiente mp : pendientes) {
                        if (mp.getTipo().equals("privado")) {
                            sb.append("De ").append(mp.getNombreRemitente()).append(": ").append(mp.getMensaje()).append("\n");
                        } else {
                            sb.append("Grupo ").append(mp.getTituloGrupo()).append(" - ").append(mp.getNombreRemitente()).append(": ").append(mp.getMensaje()).append("\n");
                        }
                    }
                    JOptionPane.showMessageDialog(this, sb.toString());
                }
                break;
        }
    }
    
    private void cerrarSesion() {
        int opcion = JOptionPane.showConfirmDialog(this,
            "¿Estás seguro de que deseas cerrar sesión?",
            "Cerrar Sesión",
            JOptionPane.YES_NO_OPTION);
        
        if (opcion == JOptionPane.YES_OPTION) {
            activo = false;
            try {
                cliente.cerrar();
            } catch (Exception e) {}
            dispose();
            System.exit(0);
        }
    }
}

