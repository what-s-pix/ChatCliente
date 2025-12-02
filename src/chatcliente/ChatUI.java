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
    private List<Usuario> usuarios;
    private List<Amistad> amigos;
    private List<Grupo> grupos;
    private Usuario destinatarioActual;
    private Grupo grupoActual;
    private boolean esChatPrivado = true;
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
        JPanel panelIzquierdo = new JPanel(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();
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
        add(panelIzquierdo, BorderLayout.WEST);
        add(panelCentral, BorderLayout.CENTER);
        JLabel lblInfo = new JLabel("Usuario: " + usuarioActual.getNombre() + " | Username: " + usuarioActual.getUsername());
        add(lblInfo, BorderLayout.NORTH);
    }
    private void cargarDatos() {
        try {
            cliente.enviar(new Peticion("OBTENER_USUARIOS", null));
            cliente.enviar(new Peticion("LISTAR_AMIGOS", null));
            cliente.enviar(new Peticion("LISTAR_MIS_GRUPOS", null));
            cliente.enviar(new Peticion("LISTAR_INVITACIONES_GRUPO", null));
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
                campoMensaje.setEnabled(false);
            }
        }
    }
    private void seleccionarAmigo() {
        int index = listaAmigos.getSelectedIndex();
        if (index >= 0 && amigos != null && index < amigos.size()) {
            Amistad amistad = amigos.get(index);
            int idOtro = (amistad.getFk_usuario1() == usuarioActual.getPk_usuario()) ?
                         amistad.getFk_usuario2() : amistad.getFk_usuario1();
            for (Usuario u : usuarios) {
                if (u.getPk_usuario() == idOtro) {
                    destinatarioActual = u;
                    grupoActual = null;
                    esChatPrivado = true;
                    areaChat.setText("=== Chat con " + u.getNombre() + " (Amigo) ===\n\n");
                    campoMensaje.setEnabled(true);
                    cargarHistorial(u.getUsername());
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
            cargarHistorialGrupo(grupoActual.getPk_grupo());
        }
    }
    private void cargarHistorial(String username) {
        try {
            cliente.enviar(new Peticion("PEDIR_HISTORIAL", username));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar historial: " + e.getMessage());
        }
    }
    private void cargarHistorialGrupo(int idGrupo) {
        try {
            cliente.enviar(new Peticion("PEDIR_HISTORIAL_GRUPO", idGrupo));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar historial: " + e.getMessage());
        }
    }
    private void enviarMensaje() {
        String texto = campoMensaje.getText().trim();
        if (texto.isEmpty()) return;
        try {
            if (esChatPrivado && destinatarioActual != null) {
                Mensaje mensaje = new Mensaje(usuarioActual.getUsername(), destinatarioActual.getUsername(), texto);
                cliente.enviar(new Peticion("ENVIAR_MENSAJE", mensaje));
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
            nombres.length > 0 ? nombres[0] : null);
        if (seleccionado != null) {
            for (Usuario u : usuarios) {
                if (seleccionado.contains(u.getUsername())) {
                    try {
                        cliente.enviar(new Peticion("ENVIAR_SOLICITUD", u.getUsername()));
                        JOptionPane.showMessageDialog(this, "Solicitud de amistad enviada");
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
        String invitados = JOptionPane.showInputDialog(this, "Invitar usuarios (separar por comas):");
        if (invitados == null) invitados = "";
        try {
            String payload = titulo.trim() + ":" + invitados.trim();
            cliente.enviar(new Peticion("CREAR_GRUPO", payload));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    private void verSolicitudes() {
        try {
            cliente.enviar(new Peticion("LISTAR_SOLICITUDES", null));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    @SuppressWarnings("unchecked")
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
    @SuppressWarnings("unchecked")
    private void procesarPeticionRecibida(Peticion p) {
        switch (p.getAccion()) {
            case "LISTA_USUARIOS":
                usuarios = (List<Usuario>) p.getDatos();
                actualizarListaUsuarios();
                break;
            case "LISTA_AMIGOS_OK":
                amigos = (List<Amistad>) p.getDatos();
                actualizarListaAmigos();
                break;
            case "LISTA_GRUPOS_OK":
                grupos = (List<Grupo>) p.getDatos();
                actualizarListaGrupos();
                break;
            case "INVITACIONES_GRUPO_OK":
                List<Grupo> invitaciones = (List<Grupo>) p.getDatos();
                if (invitaciones != null && !invitaciones.isEmpty()) {
                    mostrarInvitacionesGrupo(invitaciones);
                }
                break;
            case "LISTA_SOLICITUDES_OK":
                List<String> solicitudes = (List<String>) p.getDatos();
                if (solicitudes == null || solicitudes.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No tienes solicitudes pendientes");
                } else {
                    mostrarSolicitudesAmistad(solicitudes);
                }
                break;
            case "HISTORIAL_OK":
                Object[] paquete = (Object[]) p.getDatos();
                String amigo = (String) paquete[0];
                List<Mensaje> historial = (List<Mensaje>) paquete[1];
                if (historial != null) {
                    for (Mensaje m : historial) {
                        String remitente = (m.getFk_remitente() == usuarioActual.getPk_usuario()) ?
                                           "Tú" : m.getRemitente();
                        areaChat.append("[" + m.getFecha_envio() + "] " + remitente + ": " + m.getContenido() + "\n");
                    }
                }
                break;
            case "HISTORIAL_GRUPO_OK":
                Object[] paqueteG = (Object[]) p.getDatos();
                int idGrupo = (int) paqueteG[0];
                List<MensajeGrupo> historialGrupo = (List<MensajeGrupo>) paqueteG[1];
                if (historialGrupo != null) {
                    for (MensajeGrupo m : historialGrupo) {
                        String remitente = (m.getFk_remitente() == usuarioActual.getPk_usuario()) ?
                                           "Tú" : m.getNombreRemitente();
                        areaChat.append("[" + m.getFecha_envio() + "] " + remitente + ": " + m.getMensaje() + "\n");
                    }
                }
                break;
            case "RECIBIR_MENSAJE":
                Mensaje mensaje = (Mensaje) p.getDatos();
                if (destinatarioActual != null && mensaje.getRemitente().equals(destinatarioActual.getUsername())) {
                    areaChat.append("[Ahora] " + mensaje.getRemitente() + ": " + mensaje.getContenido() + "\n");
                } else {
                    JOptionPane.showMessageDialog(this, "Nuevo mensaje de: " + mensaje.getRemitente());
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
            case "SOLICITUD_ENVIADA_OK":
                JOptionPane.showMessageDialog(this, String.valueOf(p.getDatos()));
                cargarDatos();
                break;
            case "SOLICITUD_ERROR":
                JOptionPane.showMessageDialog(this, "Error en solicitud de amistad: " + p.getDatos());
                break;
            case "ACEPTAR_SOLICITUD_OK":
                JOptionPane.showMessageDialog(this, String.valueOf(p.getDatos()));
                cargarDatos();
                break;
            case "CREAR_GRUPO_OK":
                JOptionPane.showMessageDialog(this, String.valueOf(p.getDatos()));
                cargarDatos();
                break;
            case "ACEPTAR_GRUPO_OK":
                JOptionPane.showMessageDialog(this, String.valueOf(p.getDatos()));
                cargarDatos();
                break;
            case "ERROR_GRUPO":
                JOptionPane.showMessageDialog(this, "Error: " + p.getDatos());
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
    @SuppressWarnings("unchecked")
    private void mostrarSolicitudesAmistad(List<String> sols) {
        if (sols.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Sin solicitudes de amistad.");
            return;
        }
        for (String s : sols) {
            String[] p = s.split(":");
            int opcion = JOptionPane.showOptionDialog(this,
                p[0] + " quiere ser tu amigo",
                "Amistad",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"Sí", "No"},
                "Sí");
            if (opcion == JOptionPane.YES_OPTION) {
                try {
                    cliente.enviar(new Peticion("ACEPTAR_SOLICITUD", Integer.parseInt(p[1])));
                    cargarDatos();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
                }
            }
        }
    }
    @SuppressWarnings("unchecked")
    private void mostrarInvitacionesGrupo(List<Grupo> gruposInv) {
        if (gruposInv.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Sin invitaciones a grupos.");
            return;
        }
        for (Grupo g : gruposInv) {
            int opcion = JOptionPane.showOptionDialog(this,
                "Invitación al grupo: " + g.getTitulo(),
                "Grupo",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"Sí", "No"},
                "Sí");
            if (opcion == JOptionPane.YES_OPTION) {
                try {
                    cliente.enviar(new Peticion("ACEPTAR_GRUPO", g.getId()));
                    cargarDatos();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
                }
            }
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
