package chatcliente;
import common.Peticion;
import models.Usuario;
import models.Mensaje;
import models.Grupo;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
public class ListaAmigosUI extends JFrame {
    private Usuario miUsuario;
    private DefaultListModel<Object> modeloLista;
    private boolean escuchando = true;
    private Map<String, ChatUI> chatsPrivados = new HashMap<>();
    private Map<Integer, ChatGrupoUI> chatsGrupos = new HashMap<>();
    private JList<Object> listaPrincipal;
    private JLabel lblUsuario;
    private JLabel lblTituloLista;
    private JButton btnVerAmigos;
    private JButton btnVerGrupos;
    private JButton btnAccionPrincipal;
    private JButton btnSecundario;
    private JButton btnSolicitudes;
    private int modoVista = 0;
    public ListaAmigosUI(Usuario usuario) {
        super("Chat - " + usuario.getUsername());
        this.miUsuario = usuario;
        configurarVentana();
        inicializarComponentes();
        new Thread(() -> escucharServidor()).start();
        cambiarModo(0);
    }
    private void configurarVentana() {
        setSize(400, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
    }
    private void inicializarComponentes() {
        JPanel panelNorte = new JPanel(new BorderLayout());
        JPanel pnlInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblUsuario = new JLabel("Usuario: " + miUsuario.getUsername());
        lblUsuario.setFont(new Font("Arial", Font.BOLD, 14));
        pnlInfo.add(lblUsuario);
        panelNorte.add(pnlInfo, BorderLayout.NORTH);
        JPanel pnlTabs = new JPanel(new GridLayout(1, 2));
        btnVerAmigos = new JButton("Amigos");
        btnVerGrupos = new JButton("Grupos");
        pnlTabs.add(btnVerAmigos);
        pnlTabs.add(btnVerGrupos);
        panelNorte.add(pnlTabs, BorderLayout.SOUTH);
        add(panelNorte, BorderLayout.NORTH);
        modeloLista = new DefaultListModel<>();
        listaPrincipal = new JList<>(modeloLista);
        listaPrincipal.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(listaPrincipal);
        lblTituloLista = new JLabel("Mis Amigos");
        lblTituloLista.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JPanel pnlCentro = new JPanel(new BorderLayout());
        pnlCentro.add(lblTituloLista, BorderLayout.NORTH);
        pnlCentro.add(scroll, BorderLayout.CENTER);
        add(pnlCentro, BorderLayout.CENTER);
        JPanel panelSur = new JPanel(new FlowLayout());
        btnAccionPrincipal = new JButton("Chatear");
        btnSecundario = new JButton("Agregar (+)");
        btnSolicitudes = new JButton("Solicitudes/Invitaciones");
        panelSur.add(btnAccionPrincipal);
        panelSur.add(btnSecundario);
        panelSur.add(btnSolicitudes);
        add(panelSur, BorderLayout.SOUTH);
        btnVerAmigos.addActionListener(e -> cambiarModo(0));
        btnVerGrupos.addActionListener(e -> cambiarModo(1));
        btnAccionPrincipal.addActionListener(e -> accionPrincipal());
        btnSecundario.addActionListener(e -> accionSecundaria());
        btnSolicitudes.addActionListener(e -> accionSolicitudes());
    }
    private void cambiarModo(int modo) {
        this.modoVista = modo;
        modeloLista.clear();
        if (modo == 0) {
            lblTituloLista.setText("Lista de Amigos");
            btnAccionPrincipal.setText("Chatear");
            btnSecundario.setText("Agregar Amigo");
            btnSolicitudes.setText("Ver Solicitudes");
            pedirListaAmigos();
        } else {
            lblTituloLista.setText("Mis Grupos");
            btnAccionPrincipal.setText("Entrar al Grupo");
            btnSecundario.setText("Crear Grupo");
            btnSolicitudes.setText("Ver Solicitudes");
            pedirListaGrupos();
        }
    }
    private void accionPrincipal() {
        Object seleccion = listaPrincipal.getSelectedValue();
        if (seleccion == null) return;
        if (modoVista == 0) {
            String val = (String) seleccion;
            String username = val.split(" ")[0];
            abrirChatPrivado(username);
        } else {
            if (seleccion instanceof Grupo) {
                Grupo g = (Grupo) seleccion;
                abrirChatGrupo(g);
            }
        }
    }
    private void accionSecundaria() {
        if (modoVista == 0) {
            String destino = JOptionPane.showInputDialog(this, "Username del amigo:");
            if (destino != null && !destino.isEmpty()) {
                enviar(new Peticion("ENVIAR_SOLICITUD", destino));
            }
        } else {
            new CrearGrupoUI().setVisible(true);
        }
    }
    private void accionSolicitudes() {
        if (modoVista == 0) {
            enviar(new Peticion("LISTAR_SOLICITUDES", null));
        } else {
            enviar(new Peticion("LISTAR_INVITACIONES_GRUPO", null));
        }
    }
    private void enviar(Peticion p) {
        try { Cliente.getInstance().enviar(p); } catch (Exception e) { e.printStackTrace(); }
    }
    private void pedirListaAmigos() { enviar(new Peticion("LISTAR_AMIGOS", null)); }
    private void pedirListaGrupos() { enviar(new Peticion("LISTAR_MIS_GRUPOS", null)); }
    private void escucharServidor() {
        try {
            while (escuchando) {
                try {
                    Peticion p = Cliente.getInstance().recibir();
                    String accion = p.getAccion();
                    Object datos = p.getDatos();
                    SwingUtilities.invokeLater(() -> {
                        try {
                            switch (accion) {
                                case "LISTA_AMIGOS_OK":
                                    if (modoVista == 0) actualizarListaAmigos((ArrayList<Usuario>) datos);
                                    break;
                                case "RECIBIR_MENSAJE":
                                    redirigirMensajePrivado((Mensaje) datos);
                                    break;
                                case "HISTORIAL_OK":
                                    Object[] paquete = (Object[]) datos;
                                    String amigo = (String) paquete[0];
                                    ArrayList<Mensaje> listaHistorial = (ArrayList<Mensaje>) paquete[1];
                                    if (chatsPrivados.containsKey(amigo)) {
                                        chatsPrivados.get(amigo).cargarHistorial(listaHistorial);
                                    }
                                    break;
                                case "HISTORIAL_GRUPO_OK":
                                    Object[] paqueteG = (Object[]) datos;
                                    int idGrupo = (int) paqueteG[0];
                                    ArrayList<Mensaje> listaGrupo = (ArrayList<Mensaje>) paqueteG[1];
                                    if (chatsGrupos.containsKey(idGrupo)) {
                                        chatsGrupos.get(idGrupo).cargarHistorial(listaGrupo);
                                    }
                                    break;
                                case "LISTA_SOLICITUDES_OK":
                                    procesarSolicitudesAmistad((ArrayList<String>) datos);
                                    break;
                                case "SOLICITUD_ENVIADA_OK":
                                    JOptionPane.showMessageDialog(this, datos);
                                    if (modoVista == 0) pedirListaAmigos();
                                    break;
                                case "SOLICITUD_ERROR":
                                    JOptionPane.showMessageDialog(this, "Error: " + datos, "Error", JOptionPane.ERROR_MESSAGE);
                                    break;
                                case "ACEPTAR_SOLICITUD_OK":
                                    JOptionPane.showMessageDialog(this, datos);
                                    if (modoVista == 0) pedirListaAmigos();
                                    break;
                                case "LISTA_GRUPOS_OK":
                                    if (modoVista == 1) actualizarListaGrupos((ArrayList<Grupo>) datos);
                                    break;
                                case "RECIBIR_MENSAJE_GRUPO":
                                    redirigirMensajeGrupo((Mensaje) datos);
                                    break;
                                case "INVITACIONES_GRUPO_OK":
                                    procesarInvitacionesGrupo((ArrayList<Grupo>) datos);
                                    break;
                                case "CREAR_GRUPO_OK":
                                    JOptionPane.showMessageDialog(this, datos);
                                    if (modoVista == 1) pedirListaGrupos();
                                    break;
                                case "ACEPTAR_GRUPO_OK":
                                    JOptionPane.showMessageDialog(this, datos);
                                    if (modoVista == 1) pedirListaGrupos();
                                    break;
                                case "ERROR_GRUPO":
                                    JOptionPane.showMessageDialog(this, "Error: " + datos, "Error", JOptionPane.ERROR_MESSAGE);
                                    break;
                                default:
                                    if (datos instanceof String) {
                                    }
                            }
                        } catch (Exception ex) {
                            System.err.println("Error procesando petición: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    });
                } catch (java.io.EOFException e) {
                    break;
                } catch (java.io.StreamCorruptedException e) {
                    System.err.println("Error de serialización, ignorando paquete: " + e.getMessage());
                    continue;
                }
            }
        } catch (Exception e) {
            System.err.println("Desconectado: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void actualizarListaAmigos(ArrayList<Usuario> lista) {
        modeloLista.clear();
        for (Usuario u : lista) {
            String st = (u.getEstado() == 1) ? "[ON]" : "[OFF]";
            modeloLista.addElement(u.getUsername() + " " + st);
        }
    }
    private void actualizarListaGrupos(ArrayList<Grupo> lista) {
        modeloLista.clear();
        for (Grupo g : lista) {
            modeloLista.addElement(g);
        }
    }
    private void abrirChatPrivado(String username) {
        if (!chatsPrivados.containsKey(username) || !chatsPrivados.get(username).isVisible()) {
            ChatUI chat = new ChatUI(miUsuario, username);
            chat.setVisible(true);
            chatsPrivados.put(username, chat);
        } else {
            chatsPrivados.get(username).toFront();
        }
    }
    private void redirigirMensajePrivado(Mensaje m) {
        String remitente = m.getRemitente();
        if (chatsPrivados.containsKey(remitente) && chatsPrivados.get(remitente).isVisible()) {
            chatsPrivados.get(remitente).mostrarMensaje(m);
        } else {
            abrirChatPrivado(remitente);
            chatsPrivados.get(remitente).mostrarMensaje(m);
        }
    }
    private void abrirChatGrupo(Grupo g) {
        int id = g.getId();
        if (!chatsGrupos.containsKey(id) || !chatsGrupos.get(id).isVisible()) {
            ChatGrupoUI chat = new ChatGrupoUI(miUsuario, g);
            chat.setVisible(true);
            chatsGrupos.put(id, chat);
        } else {
            chatsGrupos.get(id).toFront();
        }
    }
    private void redirigirMensajeGrupo(Mensaje m) {
        int idGrupo = m.getId();
        if (chatsGrupos.containsKey(idGrupo) && chatsGrupos.get(idGrupo).isVisible()) {
            chatsGrupos.get(idGrupo).mostrarMensaje(m);
        }
    }
    private void procesarSolicitudesAmistad(ArrayList<String> sols) {
        if (sols.isEmpty()) JOptionPane.showMessageDialog(this, "Sin solicitudes de amistad.");
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
                enviar(new Peticion("ACEPTAR_SOLICITUD", Integer.parseInt(p[1])));
                if (modoVista == 0) pedirListaAmigos();
            }
        }
    }
    private void procesarInvitacionesGrupo(ArrayList<Grupo> grupos) {
        if (grupos.isEmpty()) JOptionPane.showMessageDialog(this, "Sin invitaciones a grupos.");
        for (Grupo g : grupos) {
            int opcion = JOptionPane.showOptionDialog(this,
                "Invitación al grupo: " + g.getTitulo(),
                "Grupo",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"Sí", "No"},
                "Sí");
            if (opcion == JOptionPane.YES_OPTION) {
                enviar(new Peticion("ACEPTAR_GRUPO", g.getId()));
                if (modoVista == 1) pedirListaGrupos();
            }
        }
    }
}
