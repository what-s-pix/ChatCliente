package chatcliente;

import common.Peticion;
<<<<<<< HEAD
import models.Mensaje;
import models.Usuario;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatUI extends JFrame {

    // Datos lógicos
    private Usuario miUsuario;
    private String usuarioDestino;

    // Componentes visuales
    private JTextArea txtAreaChat;
    private JTextField txtMensaje;
    private JButton btnEnviar;

    public ChatUI(Usuario miUsuario, String usuarioDestino) {
        super("Chat con: " + usuarioDestino);
        this.miUsuario = miUsuario;
        this.usuarioDestino = usuarioDestino;

        configurarVentana();
        inicializarComponentes();
        pedirHistorial();
    }
    
    private void pedirHistorial() {
        try {
            Cliente.getInstance().enviar(new Peticion("PEDIR_HISTORIAL", usuarioDestino));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // NUEVO: Método para cargar múltiples mensajes de golpe
    public void cargarHistorial(java.util.ArrayList<Mensaje> historial) {
        SwingUtilities.invokeLater(() -> {
            txtAreaChat.setText(""); // Limpiamos por si acaso
            for (Mensaje m : historial) {
                txtAreaChat.append("[" + m.getRemitente() + "]: " + m.getContenido() + "\n");
            }
            // Scroll abajo
            txtAreaChat.setCaretPosition(txtAreaChat.getDocument().getLength());
        });
    }

    private void configurarVentana() {
        setSize(400, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Solo cierra esta ventana
        setLayout(new BorderLayout(5, 5)); // Márgenes de 5px
    }

    private void inicializarComponentes() {
        // --- 1. ÁREA DE CHAT (CENTRO) ---
        txtAreaChat = new JTextArea();
        txtAreaChat.setEditable(false); // Solo lectura
        txtAreaChat.setLineWrap(true);  // Ajustar líneas largas
        txtAreaChat.setWrapStyleWord(true);
        txtAreaChat.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        // Scroll para el área de chat
        JScrollPane scrollPane = new JScrollPane(txtAreaChat);
        add(scrollPane, BorderLayout.CENTER);

        // --- 2. PANEL DE ENVÍO (SUR) ---
        JPanel panelInferior = new JPanel();
        panelInferior.setLayout(new BorderLayout(5, 5));
        panelInferior.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Margen interno

        txtMensaje = new JTextField();
        btnEnviar = new JButton("Enviar");

        panelInferior.add(txtMensaje, BorderLayout.CENTER);
        panelInferior.add(btnEnviar, BorderLayout.EAST);

        add(panelInferior, BorderLayout.SOUTH);

        // --- 3. EVENTOS ---
        
        // Acción al hacer clic en el botón
        btnEnviar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enviarMensaje();
            }
        });

        // Acción al presionar ENTER en el campo de texto
        txtMensaje.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enviarMensaje();
            }
        });
    }

    // --- MÉTODOS PÚBLICOS Y LÓGICOS ---

    // Este método lo llama la ListaAmigosUI cuando llega algo del server
    public void mostrarMensaje(Mensaje m) {
        SwingUtilities.invokeLater(() -> {
            txtAreaChat.append("[" + m.getRemitente() + "]: " + m.getContenido() + "\n");
            // Auto-scroll al final
            txtAreaChat.setCaretPosition(txtAreaChat.getDocument().getLength());
        });
    }

    private void enviarMensaje() {
        String texto = txtMensaje.getText().trim();
        if (texto.isEmpty()) return;

        // Crear objeto mensaje
        Mensaje m = new Mensaje(miUsuario.getUsername(), usuarioDestino, texto);

        // Feedback local (lo mostramos en nuestra pantalla)
        txtAreaChat.append("[Yo]: " + texto + "\n");

        // Enviar al servidor
        try {
            Cliente.getInstance().enviar(new Peticion("ENVIAR_MENSAJE", m));
            txtMensaje.setText(""); // Limpiar
        } catch (Exception ex) {
            txtAreaChat.append(">> Error al enviar mensaje <<\n");
            ex.printStackTrace();
        }
    }
}
=======
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
            // Solo ENVIAMOS las peticiones.
            // Las respuestas llegarán por el hilo de recepción y se procesan en procesarPeticionRecibida().
            cliente.enviar(new Peticion("OBTENER_USUARIOS", null));
            cliente.enviar(new Peticion("OBTENER_AMIGOS", null));
            cliente.enviar(new Peticion("OBTENER_GRUPOS", null));
            cliente.enviar(new Peticion("OBTENER_INVITACIONES_GRUPO", null));
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
            // Pedimos el historial; la respuesta se procesa en procesarPeticionRecibida()
            cliente.enviar(new Peticion("OBTENER_HISTORIAL", idOtroUsuario));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar historial: " + e.getMessage());
        }
    }
    
    private void cargarHistorialGrupo(int idGrupo) {
        try {
            // Pedimos el historial del grupo; se procesa de forma asíncrona
            cliente.enviar(new Peticion("OBTENER_HISTORIAL_GRUPO", idGrupo));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar historial: " + e.getMessage());
        }
    }
    
    private void enviarMensaje() {
        String texto = campoMensaje.getText().trim();
        if (texto.isEmpty()) return;
        
        try {
            if (esChatPrivado && destinatarioActual != null) {
                // Envío de mensaje privado: el servidor notificará al otro cliente
                Mensaje mensaje = new Mensaje(usuarioActual.getPk_usuario(), 
                                             destinatarioActual.getPk_usuario(), texto);
                cliente.enviar(new Peticion("ENVIAR_MENSAJE_PRIVADO", mensaje));
                // Mostramos nuestro propio mensaje inmediatamente
                areaChat.append("[Ahora] Tú: " + texto + "\n");
                campoMensaje.setText("");
            } else if (!esChatPrivado && grupoActual != null) {
                MensajeGrupo mensaje = new MensajeGrupo();
                mensaje.setFk_grupo(grupoActual.getPk_grupo());
                mensaje.setFk_remitente(usuarioActual.getPk_usuario());
                mensaje.setMensaje(texto);
                cliente.enviar(new Peticion("ENVIAR_MENSAJE_GRUPO", mensaje));
                areaChat.append("[Ahora] Tú: " + texto + "\n");
                campoMensaje.setText("");
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
                        // Solo enviamos la solicitud; la respuesta se manejará en procesarPeticionRecibida()
                        cliente.enviar(new Peticion("ENVIAR_SOLICITUD_AMISTAD", u.getPk_usuario()));
                        JOptionPane.showMessageDialog(this, "Solicitud de amistad enviada (esperando respuesta)");
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
            // Enviamos la petición de creación; la respuesta se procesará de forma asíncrona
            cliente.enviar(new Peticion("CREAR_GRUPO", grupo));
            // El servidor devolverá GRUPO_CREADO o GRUPO_ERROR que se maneja en procesarPeticionRecibida()
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
                        // Enviamos invitación; la respuesta se maneja de forma asíncrona
                        cliente.enviar(new Peticion("INVITAR_A_GRUPO", datos));
                        JOptionPane.showMessageDialog(this, "Invitación enviada (esperando respuesta)");
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
            // Pedimos la lista de solicitudes; se manejará en procesarPeticionRecibida()
            cliente.enviar(new Peticion("OBTENER_SOLICITUDES", null));
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
                    // La respuesta (SOLICITUD_ACEPTADA o SOLICITUD_ERROR) se maneja de forma asíncrona
                } else if (opcion == 1) { // Rechazar
                    cliente.enviar(new Peticion("RECHAZAR_SOLICITUD_AMISTAD", s.getPk_amistad()));
                    // Se manejará la respuesta en procesarPeticionRecibida()
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
                    // La respuesta se manejará en procesarPeticionRecibida()
                } else if (opcion == 1) { // Rechazar
                    cliente.enviar(new Peticion("RECHAZAR_INVITACION_GRUPO", inv.getPk_invitacion()));
                    // Respuesta asíncrona
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
            // --- Listas e información inicial ---
            case "LISTA_USUARIOS":
                usuarios = (List<Usuario>) p.getDatos();
                actualizarListaUsuarios();
                break;
                
            case "LISTA_AMIGOS":
                amigos = (List<Amistad>) p.getDatos();
                actualizarListaAmigos();
                break;
                
            case "LISTA_GRUPOS":
                grupos = (List<Grupo>) p.getDatos();
                actualizarListaGrupos();
                break;
                
            case "LISTA_INVITACIONES_GRUPO":
                List<InvitacionGrupo> invitaciones = (List<InvitacionGrupo>) p.getDatos();
                if (invitaciones != null && !invitaciones.isEmpty()) {
                    mostrarInvitaciones(invitaciones);
                }
                break;
                
            case "LISTA_SOLICITUDES":
                List<Amistad> solicitudes = (List<Amistad>) p.getDatos();
                if (solicitudes == null || solicitudes.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No tienes solicitudes pendientes");
                } else {
                    mostrarSolicitudesAmistad(solicitudes);
                }
                break;

            // --- Historiales ---
            case "HISTORIAL_MENSAJES":
                List<Mensaje> historial = (List<Mensaje>) p.getDatos();
                if (historial != null) {
                    for (Mensaje m : historial) {
                        String remitente = (m.getFk_remitente() == usuarioActual.getPk_usuario()) ? 
                                           "Tú" : m.getNombreRemitente();
                        areaChat.append("[" + m.getFecha_envio() + "] " + remitente + ": " + m.getMensaje() + "\n");
                    }
                }
                break;
                
            case "HISTORIAL_GRUPO":
                List<MensajeGrupo> historialGrupo = (List<MensajeGrupo>) p.getDatos();
                if (historialGrupo != null) {
                    for (MensajeGrupo m : historialGrupo) {
                        String remitente = (m.getFk_remitente() == usuarioActual.getPk_usuario()) ? 
                                           "Tú" : m.getNombreRemitente();
                        areaChat.append("[" + m.getFecha_envio() + "] " + remitente + ": " + m.getMensaje() + "\n");
                    }
                }
                break;

            // --- Mensajes en tiempo real ---
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
                
            case "MENSAJE_ENVIADO":
                // ACK opcional del servidor; ya mostramos nuestro mensaje localmente
                break;
                
            case "MENSAJE_ERROR":
                JOptionPane.showMessageDialog(this, "Error al enviar mensaje: " + p.getDatos());
                break;

            // --- Sistema de amigos ---
            case "NUEVA_SOLICITUD_AMISTAD":
                JOptionPane.showMessageDialog(this, "Tienes una nueva solicitud de amistad");
                cargarDatos();
                break;
                
            case "SOLICITUD_ENVIADA":
                JOptionPane.showMessageDialog(this, String.valueOf(p.getDatos()));
                break;
                
            case "SOLICITUD_ERROR":
                JOptionPane.showMessageDialog(this, "Error en solicitud de amistad: " + p.getDatos());
                break;
                
            case "SOLICITUD_ACEPTADA":
                JOptionPane.showMessageDialog(this, String.valueOf(p.getDatos()));
                cargarDatos();
                break;
                
            case "SOLICITUD_RECHAZADA":
                JOptionPane.showMessageDialog(this, String.valueOf(p.getDatos()));
                break;
                
            // --- Invitaciones a grupos ---
            case "NUEVA_INVITACION_GRUPO":
                InvitacionGrupo inv = (InvitacionGrupo) p.getDatos();
                mostrarInvitaciones(java.util.Arrays.asList(inv));
                break;
                
            case "INVITACION_ENVIADA":
                JOptionPane.showMessageDialog(this, String.valueOf(p.getDatos()));
                break;
                
            case "INVITACION_ERROR":
                JOptionPane.showMessageDialog(this, "Error en invitación: " + p.getDatos());
                break;
                
            case "INVITACION_ACEPTADA":
                JOptionPane.showMessageDialog(this, String.valueOf(p.getDatos()));
                cargarDatos();
                break;
                
            case "INVITACION_RECHAZADA":
                JOptionPane.showMessageDialog(this, String.valueOf(p.getDatos()));
                break;
                
            case "GRUPO_ELIMINADO":
                JOptionPane.showMessageDialog(this, String.valueOf(p.getDatos()));
                cargarDatos();
                break;
                
            case "GRUPO_CREADO":
                Grupo nuevoGrupo = (Grupo) p.getDatos();
                if (grupos == null) grupos = new ArrayList<>();
                grupos.add(nuevoGrupo);
                actualizarListaGrupos();
                JOptionPane.showMessageDialog(this, "Grupo creado. Ahora invita a al menos 2 personas más.");
                // Lanzamos diálogo para invitar al menos a otro usuario
                invitarAGrupo(nuevoGrupo.getPk_grupo());
                break;

            // --- Mensajes pendientes ---
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

>>>>>>> origin/avances
