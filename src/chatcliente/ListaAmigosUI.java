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
    private DefaultListModel<Object> modeloLista; // Object para guardar Usuario o Grupo
    private boolean escuchando = true;
    
    // Control de ventanas abiertas
    private Map<String, ChatUI> chatsPrivados = new HashMap<>();
    private Map<Integer, ChatGrupoUI> chatsGrupos = new HashMap<>(); // Clave: ID Grupo

    // Componentes
    private JList<Object> listaPrincipal;
    private JLabel lblUsuario;
    private JLabel lblTituloLista;
    
    private JButton btnVerAmigos;
    private JButton btnVerGrupos;
    private JButton btnAccionPrincipal; // Será "Chatear" o "Entrar"
    private JButton btnSecundario;      // "Agregar" o "Crear"
    private JButton btnSolicitudes;     // Común para ambos

    // Estado: 0 = Viendo Amigos, 1 = Viendo Grupos
    private int modoVista = 0; 

    public ListaAmigosUI(Usuario usuario) {
        super("Chat - " + usuario.getUsername());
        this.miUsuario = usuario;

        configurarVentana();
        inicializarComponentes();
        
        new Thread(() -> escucharServidor()).start();
        
        // Iniciar en modo amigos
        cambiarModo(0);
    }

    private void configurarVentana() {
        setSize(400, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
    }

    private void inicializarComponentes() {
        // --- NORTE: DATOS Y TABS ---
        JPanel panelNorte = new JPanel(new BorderLayout());
        
        // Saludo
        JPanel pnlInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblUsuario = new JLabel("Usuario: " + miUsuario.getUsername());
        lblUsuario.setFont(new Font("Arial", Font.BOLD, 14));
        pnlInfo.add(lblUsuario);
        panelNorte.add(pnlInfo, BorderLayout.NORTH);
        
        // Botones tipo "Pestaña"
        JPanel pnlTabs = new JPanel(new GridLayout(1, 2));
        btnVerAmigos = new JButton("Amigos");
        btnVerGrupos = new JButton("Grupos");
        pnlTabs.add(btnVerAmigos);
        pnlTabs.add(btnVerGrupos);
        panelNorte.add(pnlTabs, BorderLayout.SOUTH);
        
        add(panelNorte, BorderLayout.NORTH);

        // --- CENTRO: LISTA ---
        modeloLista = new DefaultListModel<>();
        listaPrincipal = new JList<>(modeloLista);
        listaPrincipal.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        JScrollPane scroll = new JScrollPane(listaPrincipal);
        lblTituloLista = new JLabel("Mis Amigos");
        lblTituloLista.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Un panel wrapper para ponerle titulito
        JPanel pnlCentro = new JPanel(new BorderLayout());
        pnlCentro.add(lblTituloLista, BorderLayout.NORTH);
        pnlCentro.add(scroll, BorderLayout.CENTER);
        
        add(pnlCentro, BorderLayout.CENTER);

        // --- SUR: ACCIONES ---
        JPanel panelSur = new JPanel(new FlowLayout());
        btnAccionPrincipal = new JButton("Chatear");
        btnSecundario = new JButton("Agregar (+)");
        btnSolicitudes = new JButton("Solicitudes/Invitaciones");
        
        panelSur.add(btnAccionPrincipal);
        panelSur.add(btnSecundario);
        panelSur.add(btnSolicitudes);
        add(panelSur, BorderLayout.SOUTH);

        // --- EVENTOS ---
        btnVerAmigos.addActionListener(e -> cambiarModo(0));
        btnVerGrupos.addActionListener(e -> cambiarModo(1));
        
        btnAccionPrincipal.addActionListener(e -> accionPrincipal());
        btnSecundario.addActionListener(e -> accionSecundaria());
        btnSolicitudes.addActionListener(e -> accionSolicitudes());
    }
    
    // --- LÓGICA DE MODOS ---
    private void cambiarModo(int modo) {
        this.modoVista = modo;
        modeloLista.clear(); // Limpiar lista visual
        
        if (modo == 0) {
            // MODO AMIGOS
            lblTituloLista.setText("Lista de Amigos");
            btnAccionPrincipal.setText("Chatear");
            btnSecundario.setText("Agregar Amigo");
            btnSolicitudes.setText("Ver Solicitudes");
            pedirListaAmigos();
        } else {
            // MODO GRUPOS
            lblTituloLista.setText("Mis Grupos");
            btnAccionPrincipal.setText("Entrar al Grupo");
            btnSecundario.setText("Crear Grupo");
            btnSolicitudes.setText("Ver Solicitudes");
            pedirListaGrupos();
        }
    }

    // --- ACCIONES DE LOS BOTONES ---

    private void accionPrincipal() { // Chatear o Entrar
        Object seleccion = listaPrincipal.getSelectedValue();
        if (seleccion == null) return;

        if (modoVista == 0) {
            // Es un String "usuario [ON]"
            String val = (String) seleccion;
            String username = val.split(" ")[0];
            abrirChatPrivado(username);
        } else {
            // Es un objeto Grupo (gracias al toString() vemos el nombre)
            if (seleccion instanceof Grupo) {
                Grupo g = (Grupo) seleccion;
                abrirChatGrupo(g);
            }
        }
    }

    private void accionSecundaria() { // Agregar o Crear
        if (modoVista == 0) {
            // Agregar Amigo
            String destino = JOptionPane.showInputDialog(this, "Username del amigo:");
            if (destino != null && !destino.isEmpty()) {
                enviar(new Peticion("ENVIAR_SOLICITUD", destino));
            }
        } else {
            // Crear Grupo
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

    // --- COMUNICACIÓN CON SERVER ---
    
    private void enviar(Peticion p) {
        try { Cliente.getInstance().enviar(p); } catch (Exception e) { e.printStackTrace(); }
    }
    
    private void pedirListaAmigos() { enviar(new Peticion("LISTAR_AMIGOS", null)); }
    private void pedirListaGrupos() { enviar(new Peticion("LISTAR_MIS_GRUPOS", null)); }

    private void escucharServidor() {
        try {
            while (escuchando) {
                Peticion p = Cliente.getInstance().recibir();
                String accion = p.getAccion();
                Object datos = p.getDatos();

                SwingUtilities.invokeLater(() -> {
                    switch (accion) {
                        // AMIGOS
                        case "LISTA_AMIGOS_OK":
                            if (modoVista == 0) actualizarListaAmigos((ArrayList<Usuario>) datos);
                            break;
                        case "RECIBIR_MENSAJE":
                            redirigirMensajePrivado((Mensaje) datos);
                            break;
                        case "HISTORIAL_OK":
                            // Datos: Object[] { "usuario_remitente", ArrayList<Mensaje> }
                            Object[] paquete = (Object[]) datos;
                            String amigo = (String) paquete[0];
                            ArrayList<Mensaje> listaHistorial = (ArrayList<Mensaje>) paquete[1];
                            
                            // Buscar la ventana y cargarle los mensajes
                            if (chatsPrivados.containsKey(amigo)) {
                                chatsPrivados.get(amigo).cargarHistorial(listaHistorial);
                            }
                            break;

                        case "HISTORIAL_GRUPO_OK":
                            // Datos: Object[] { int idGrupo, ArrayList<Mensaje> }
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
                            
                        // GRUPOS
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
                            if (modoVista == 1) pedirListaGrupos(); // Refrescar
                            break;

                        // GENÉRICOS
                        default:
                            if (datos instanceof String) {
                                // Mensajes simples (OK/Error)
                                // JOptionPane.showMessageDialog(this, datos);
                            }
                    }
                });
            }
        } catch (Exception e) { System.err.println("Desconectado."); }
    }

    // --- ACTUALIZADORES VISUALES ---

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
            modeloLista.addElement(g); // Se agrega el objeto, JList usa toString() para mostrar
        }
    }

    // --- REDIRECCIÓN DE MENSAJES Y APERTURA DE CHATS ---

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
        // En mensajes de grupo, usamos m.getId() para saber el ID del grupo (truco que hicimos en el DAO)
        // OJO: El servidor debe haber puesto el id_grupo en el ID del mensaje o en algún lado.
        // En nuestro diseño del DAO Servidor: mGrupo.setId(idGrupoDestino); <- Esto hicimos.
        
        int idGrupo = m.getId();
        
        if (chatsGrupos.containsKey(idGrupo) && chatsGrupos.get(idGrupo).isVisible()) {
            chatsGrupos.get(idGrupo).mostrarMensaje(m);
        } else {
            // Si la ventana está cerrada, NO la abrimos sola (molesto), solo notificamos o ignoramos
            // Opcional: Sonido
            // Toolkit.getDefaultToolkit().beep();
        }
    }

    // --- PROCESAR SOLICITUDES ---
    
    private void procesarSolicitudesAmistad(ArrayList<String> sols) {
        if (sols.isEmpty()) JOptionPane.showMessageDialog(this, "Sin solicitudes de amistad.");
        for (String s : sols) {
            String[] p = s.split(":");
            if (JOptionPane.showConfirmDialog(this, p[0] + " quiere ser tu amigo", "Amistad", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                enviar(new Peticion("ACEPTAR_SOLICITUD", Integer.parseInt(p[1])));
                if (modoVista == 0) pedirListaAmigos();
            }
        }
    }
    
    private void procesarInvitacionesGrupo(ArrayList<Grupo> grupos) {
        if (grupos.isEmpty()) JOptionPane.showMessageDialog(this, "Sin invitaciones a grupos.");
        for (Grupo g : grupos) {
            if (JOptionPane.showConfirmDialog(this, "Invitación al grupo: " + g.getTitulo(), "Grupo", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                enviar(new Peticion("ACEPTAR_GRUPO", g.getId()));
                if (modoVista == 1) pedirListaGrupos();
            }
        }
    }
}