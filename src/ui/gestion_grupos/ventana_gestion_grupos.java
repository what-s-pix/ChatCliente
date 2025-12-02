package ui.gestion_grupos;
import chatcliente.Cliente;
import common.Peticion;
import models.Grupo;
import models.InvitacionGrupo;
import models.Usuario;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
public class ventana_gestion_grupos extends JDialog {
    private static java.util.List<ventana_gestion_grupos> ventanasAbiertas = new ArrayList<>();
    private int usuarioActualId;
    private DefaultListModel<String> modeloGrupos;
    private JList<String> listaGrupos;
    private JTextField txtTituloGrupo;
    private JList<String> listaUsuariosDisponibles;
    private DefaultListModel<String> modeloUsuarios;
    private JList<String> listaUsuariosGestion;
    private DefaultListModel<String> modeloUsuariosGestion;
    private JList<String> listaMiembros;
    private DefaultListModel<String> modeloMiembros;
    private java.util.Set<Integer> idsAmigosPendientes = new java.util.HashSet<>();
    public ventana_gestion_grupos(JFrame parent, int usuarioActualId) {
        super(parent, "Gestión de Grupos", true);
        this.usuarioActualId = usuarioActualId;
        ventanasAbiertas.add(this);
        configurarVentana();
        inicializarComponentes();
        solicitarDatos();
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                ventanasAbiertas.remove(ventana_gestion_grupos.this);
            }
        });
    }
    public static void actualizarGruposEnVentanas(List<Grupo> grupos) {
        for (ventana_gestion_grupos ventana : ventanasAbiertas) {
            if (ventana != null && ventana.isVisible()) {
                SwingUtilities.invokeLater(() -> {
                    int indiceSeleccionado = ventana.listaGrupos.getSelectedIndex();
                    String valorSeleccionado = ventana.listaGrupos.getSelectedValue();
                    ventana.actualizarListaGrupos(grupos);
                    if (valorSeleccionado != null) {
                        for (int i = 0; i < ventana.modeloGrupos.size(); i++) {
                            if (ventana.modeloGrupos.getElementAt(i).equals(valorSeleccionado)) {
                                ventana.listaGrupos.setSelectedIndex(i);
                                ventana.cargarMiembrosGrupo();
                                break;
                            }
                        }
                    } else if (indiceSeleccionado >= 0 && indiceSeleccionado < ventana.modeloGrupos.size()) {
                        ventana.listaGrupos.setSelectedIndex(indiceSeleccionado);
                        ventana.cargarMiembrosGrupo();
                    }
                });
            }
        }
    }
    public static void actualizarMiembrosEnVentanas(List<Usuario> miembros, int grupoId) {
        for (ventana_gestion_grupos ventana : ventanasAbiertas) {
            if (ventana != null && ventana.isVisible()) {
                boolean debeActualizar = false;
                if (grupoId == -1) {
                    debeActualizar = true;
                } else if (ventana.grupoSeleccionadoId == grupoId) {
                    debeActualizar = true;
                } else if (!ventana.listaGrupos.isSelectionEmpty()) {
                    String grupoStr = ventana.listaGrupos.getSelectedValue();
                    if (grupoStr != null) {
                        try {
                            int grupoSeleccionado = Integer.parseInt(grupoStr.split(" - ")[0]);
                            if (grupoSeleccionado == grupoId) {
                                ventana.grupoSeleccionadoId = grupoId;
                                debeActualizar = true;
                            }
                        } catch (Exception e) {}
                    }
                }
                if (debeActualizar || (!ventana.listaGrupos.isSelectionEmpty() && ventana.modeloMiembros.size() == 1 && ventana.modeloMiembros.getElementAt(0).equals("Cargando miembros..."))) {
                    final List<Usuario> miembrosFinal = miembros;
                    SwingUtilities.invokeLater(() -> {
                        ventana.modeloMiembros.clear();
                        if (miembrosFinal != null && !miembrosFinal.isEmpty()) {
                            for (Usuario miembro : miembrosFinal) {
                                if (miembro != null) {
                                    String estado = miembro.getEstado() == 1 ? "[Online]" : "[Offline]";
                                    ventana.modeloMiembros.addElement(
                                        miembro.getPk_usuario() + " - " +
                                        estado + " " + miembro.getNombre() + " (" + miembro.getUsername() + ")");
                                }
                            }
                        } else {
                            ventana.modeloMiembros.addElement("No hay miembros en este grupo");
                        }
                    });
                    return;
                }
            }
        }
    }
    public static void actualizarAmigosEnVentanas(List<models.Amistad> amistades, int usuarioActualIdParam) {
        for (ventana_gestion_grupos ventana : ventanasAbiertas) {
            if (ventana != null && ventana.isVisible()) {
                boolean debeActualizar = false;
                if (usuarioActualIdParam > 0) {
                    debeActualizar = (ventana.usuarioActualId == usuarioActualIdParam);
                } else if (!amistades.isEmpty()) {
                    boolean coincide = false;
                    for (models.Amistad a : amistades) {
                        if (a != null && (a.getFk_usuario1() == ventana.usuarioActualId || a.getFk_usuario2() == ventana.usuarioActualId)) {
                            coincide = true;
                            break;
                        }
                    }
                    debeActualizar = coincide;
                } else {
                    debeActualizar = true;
                }
                if (debeActualizar) {
                    final ventana_gestion_grupos ventanaFinal = ventana;
                    SwingUtilities.invokeLater(() -> {
                        ventanaFinal.procesarAmistades(amistades);
                    });
                }
            }
        }
    }
    private void procesarAmistades(List<models.Amistad> amistades) {
        idsAmigosPendientes.clear();
        if (amistades != null && !amistades.isEmpty()) {
            for (models.Amistad amistad : amistades) {
                if (amistad != null && "aceptada".equals(amistad.getEstado())) {
                    int otroUsuarioId = amistad.getFk_usuario1() == usuarioActualId ?
                        amistad.getFk_usuario2() : amistad.getFk_usuario1();
                    if (otroUsuarioId != usuarioActualId && otroUsuarioId > 0) {
                        idsAmigosPendientes.add(otroUsuarioId);
                    }
                }
            }
        }
        if (idsAmigosPendientes.isEmpty()) {
            modeloUsuarios.clear();
            modeloUsuarios.addElement("No tienes amigos disponibles");
            return;
        }
        solicitarUsuariosParaAmigos();
    }
    private void solicitarUsuariosParaAmigos() {
        new Thread(() -> {
            try {
                Peticion pUsuarios = new Peticion("OBTENER_USUARIOS", null);
                Cliente.getInstance().enviar(pUsuarios);
            } catch (Exception ex) {}
        }).start();
    }
    public static void actualizarUsuariosEnVentanas(List<Usuario> todosUsuarios) {
        for (ventana_gestion_grupos ventana : ventanasAbiertas) {
            if (ventana != null && ventana.isVisible()) {
                final ventana_gestion_grupos ventanaFinal = ventana;
                final java.util.Set<Integer> idsPendientes = new java.util.HashSet<>(ventana.idsAmigosPendientes);
                if (!idsPendientes.isEmpty()) {
                    SwingUtilities.invokeLater(() -> {
                        java.util.List<Usuario> amigosUsuarios = new java.util.ArrayList<>();
                        if (todosUsuarios != null) {
                            for (Usuario usuario : todosUsuarios) {
                                if (usuario != null && idsPendientes.contains(usuario.getPk_usuario())) {
                                    amigosUsuarios.add(usuario);
                                }
                            }
                        }
                        if (amigosUsuarios.isEmpty()) {
                            ventanaFinal.modeloUsuarios.clear();
                            ventanaFinal.modeloUsuarios.addElement("No tienes amigos disponibles");
                        } else {
                            ventanaFinal.actualizarListaUsuarios(amigosUsuarios);
                        }
                        ventanaFinal.idsAmigosPendientes.clear();
                    });
                }
            }
        }
    }
    private void solicitarDatos() {
        new Thread(() -> {
            try {
                Thread.sleep(200);
                Cliente.getInstance().enviar(new Peticion("OBTENER_GRUPOS", usuarioActualId));
                Thread.sleep(100);
                Cliente.getInstance().enviar(new Peticion("OBTENER_AMIGOS", usuarioActualId));
                cargarUsuariosGestion();
            } catch (Exception ex) {}
        }).start();
    }
    private void configurarVentana() {
        setSize(600, 500);
        setLocationRelativeTo(getParent());
    }
    private void inicializarComponentes() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JTabbedPane tabbedPane = new JTabbedPane();
        JPanel panelCrear = crearPanelCrearGrupo();
        JPanel panelGestion = crearPanelGestionGrupos();
        tabbedPane.addTab("Crear Grupo", panelCrear);
        tabbedPane.addTab("Gestionar Grupos", panelGestion);
        panelPrincipal.add(tabbedPane, BorderLayout.CENTER);
        JPanel panelBotones = new JPanel(new FlowLayout());
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());
        panelBotones.add(btnCerrar);
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);
        add(panelPrincipal);
    }
    private JPanel crearPanelCrearGrupo() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel panelCrear = new JPanel(new BorderLayout(5, 5));
        panelCrear.setBorder(BorderFactory.createTitledBorder("Nuevo Grupo"));
        txtTituloGrupo = new JTextField(20);
        JButton btnCrear = new JButton("Crear Grupo");
        btnCrear.addActionListener(e -> crearGrupo());
        JPanel panelTitulo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelTitulo.add(new JLabel("Título del grupo:"));
        panelTitulo.add(txtTituloGrupo);
        panelTitulo.add(btnCrear);
        panelCrear.add(panelTitulo, BorderLayout.NORTH);
        JPanel panelAmigos = new JPanel(new BorderLayout(5, 5));
        panelAmigos.setBorder(BorderFactory.createTitledBorder("Selecciona amigos para invitar (mínimo 2)"));
        modeloUsuarios = new DefaultListModel<>();
        listaUsuariosDisponibles = new JList<>(modeloUsuarios);
        listaUsuariosDisponibles.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listaUsuariosDisponibles.setToolTipText("Clic para seleccionar uno. Ctrl+Click para múltiple. Shift+Click para rango.");
        JScrollPane scrollAmigos = new JScrollPane(listaUsuariosDisponibles);
        JPanel panelBotonesAmigos = new JPanel(new FlowLayout());
        JButton btnActualizarAmigos = new JButton("Actualizar Lista");
        btnActualizarAmigos.addActionListener(e -> cargarUsuarios());
        panelBotonesAmigos.add(btnActualizarAmigos);
        panelAmigos.add(scrollAmigos, BorderLayout.CENTER);
        panelAmigos.add(panelBotonesAmigos, BorderLayout.SOUTH);
        panelCrear.add(panelAmigos, BorderLayout.CENTER);
        panel.add(panelCrear, BorderLayout.CENTER);
        return panel;
    }
    private JPanel crearPanelGestionGrupos() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JSplitPane splitVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitVertical.setDividerLocation(200);
        JPanel panelGrupos = new JPanel(new BorderLayout(5, 5));
        panelGrupos.setBorder(BorderFactory.createTitledBorder("Mis Grupos"));
        modeloGrupos = new DefaultListModel<>();
        listaGrupos = new JList<>(modeloGrupos);
        listaGrupos.addListSelectionListener(e -> cargarMiembrosGrupo());
        JScrollPane scrollGrupos = new JScrollPane(listaGrupos);
        panelGrupos.add(scrollGrupos, BorderLayout.CENTER);
        JPanel panelMiembros = new JPanel(new BorderLayout(5, 5));
        panelMiembros.setBorder(BorderFactory.createTitledBorder("Miembros del Grupo"));
        modeloMiembros = new DefaultListModel<>();
        listaMiembros = new JList<>(modeloMiembros);
        JScrollPane scrollMiembros = new JScrollPane(listaMiembros);
        panelMiembros.add(scrollMiembros, BorderLayout.CENTER);
        splitVertical.setTopComponent(panelGrupos);
        splitVertical.setBottomComponent(panelMiembros);
        JPanel panelDerecha = new JPanel(new BorderLayout(5, 5));
        panelDerecha.setBorder(BorderFactory.createTitledBorder("Amigos Disponibles"));
        modeloUsuariosGestion = new DefaultListModel<>();
        listaUsuariosGestion = new JList<>(modeloUsuariosGestion);
        listaUsuariosGestion.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listaUsuariosGestion.setToolTipText("Selecciona amigos para invitar al grupo");
        JScrollPane scrollUsuariosGestion = new JScrollPane(listaUsuariosGestion);
        JPanel panelBotonesGestion = new JPanel(new GridLayout(2, 1, 5, 5));
        JButton btnInvitar = new JButton("Invitar al Grupo");
        JButton btnEliminar = new JButton("Eliminar Miembro");
        JButton btnSalir = new JButton("Salir del Grupo");
        btnInvitar.addActionListener(e -> invitarMiembros());
        btnEliminar.addActionListener(e -> eliminarMiembro());
        btnSalir.addActionListener(e -> salirGrupo());
        panelBotonesGestion.add(btnInvitar);
        panelBotonesGestion.add(btnEliminar);
        panelBotonesGestion.add(btnSalir);
        panelDerecha.add(scrollUsuariosGestion, BorderLayout.CENTER);
        panelDerecha.add(panelBotonesGestion, BorderLayout.SOUTH);
        JSplitPane splitHorizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, splitVertical, panelDerecha);
        splitHorizontal.setDividerLocation(300);
        panel.add(splitHorizontal, BorderLayout.CENTER);
        return panel;
    }
    private void crearGrupo() {
        String titulo = txtTituloGrupo.getText().trim();
        if (titulo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El título es obligatorio.",
                "Información", JOptionPane.PLAIN_MESSAGE);
            return;
        }
        int[] seleccionados = listaUsuariosDisponibles.getSelectedIndices();
        if (seleccionados.length < 2) {
            JOptionPane.showMessageDialog(this,
                "Se necesitan al menos 2 usuarios para crear un grupo (mínimo 3 personas).",
                "Información", JOptionPane.PLAIN_MESSAGE);
            return;
        }
        try {
            java.util.List<Integer> usuarios = new java.util.ArrayList<>();
            for (int idx : seleccionados) {
                String usuarioStr = modeloUsuarios.getElementAt(idx);
                int usuarioId = Integer.parseInt(usuarioStr.split(" - ")[0]);
                usuarios.add(usuarioId);
            }
            Grupo grupo = new Grupo(titulo, usuarioActualId);
            grupo.setInvitaciones_pendientes(usuarios);
            Peticion p = new Peticion("CREAR_GRUPO", grupo);
            Cliente.getInstance().enviar(p);
            JOptionPane.showMessageDialog(this, "Grupo creado. Verifica en la lista de grupos.",
                "Información", JOptionPane.PLAIN_MESSAGE);
            txtTituloGrupo.setText("");
            solicitarDatos();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                "Error", JOptionPane.PLAIN_MESSAGE);
        }
    }
    private void invitarMiembros() {
        if (listaGrupos.isSelectionEmpty() || listaUsuariosGestion.isSelectionEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecciona un grupo y al menos un amigo para invitar.",
                "Información", JOptionPane.PLAIN_MESSAGE);
            return;
        }
        String grupoStr = listaGrupos.getSelectedValue();
        int grupoId = Integer.parseInt(grupoStr.split(" - ")[0]);
        int[] seleccionados = listaUsuariosGestion.getSelectedIndices();
        try {
            int invitados = 0;
            for (int idx : seleccionados) {
                String usuarioStr = modeloUsuariosGestion.getElementAt(idx);
                int usuarioId = Integer.parseInt(usuarioStr.split(" - ")[0]);
                Peticion p = new Peticion("INVITAR_A_GRUPO", new Object[]{grupoId, usuarioId});
                Cliente.getInstance().enviar(p);
                invitados++;
            }
            JOptionPane.showMessageDialog(this, "Se enviaron " + invitados + " invitación(es).",
                "Información", JOptionPane.PLAIN_MESSAGE);
            cargarUsuariosGestion();
            cargarMiembrosGrupo();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                "Error", JOptionPane.PLAIN_MESSAGE);
        }
    }
    private void eliminarMiembro() {
        if (listaGrupos.isSelectionEmpty()) {
            return;
        }
        String grupoStr = listaGrupos.getSelectedValue();
        int grupoId = Integer.parseInt(grupoStr.split(" - ")[0]);
        String usuarioStr = JOptionPane.showInputDialog(this,
            "Ingresa el ID del usuario a eliminar:");
        if (usuarioStr == null || usuarioStr.trim().isEmpty()) {
            return;
        }
        try {
            int usuarioId = Integer.parseInt(usuarioStr.trim());
            Peticion p = new Peticion("ELIMINAR_MIEMBRO_GRUPO",
                new Object[] {grupoId, usuarioId});
            Cliente.getInstance().enviar(p);
            JOptionPane.showMessageDialog(this, "Solicitud de eliminación enviada.",
                "Información", JOptionPane.PLAIN_MESSAGE);
            solicitarDatos();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                "Error", JOptionPane.PLAIN_MESSAGE);
        }
    }
    private void salirGrupo() {
        if (listaGrupos.isSelectionEmpty()) {
            return;
        }
        String grupoStr = listaGrupos.getSelectedValue();
        int grupoId = Integer.parseInt(grupoStr.split(" - ")[0]);
        int opcion = JOptionPane.showConfirmDialog(this,
            "¿Estás seguro de que deseas salir del grupo?",
            "Confirmar",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.PLAIN_MESSAGE);
        if (opcion == JOptionPane.YES_OPTION) {
            try {
                Peticion p = new Peticion("SALIR_GRUPO", grupoId);
                Cliente.getInstance().enviar(p);
                JOptionPane.showMessageDialog(this, "Solicitud de salida enviada.",
                    "Información", JOptionPane.PLAIN_MESSAGE);
                solicitarDatos();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.PLAIN_MESSAGE);
            }
        }
    }
    private void cargarGrupos() {
        new Thread(() -> {
            try {
                Peticion p = new Peticion("OBTENER_GRUPOS", usuarioActualId);
                Cliente.getInstance().enviar(p);
                Thread.sleep(500);
                Peticion respuesta = null;
                int intentos = 0;
                while (intentos < 20) {
                    try {
                        respuesta = Cliente.getInstance().recibir();
                        if (respuesta != null && 
                            (respuesta.getAccion().equals("LISTA_GRUPOS_OK") || 
                             respuesta.getAccion().equals("GRUPOS_OBTENIDOS"))) {
                            @SuppressWarnings("unchecked")
                            List<Grupo> grupos = (List<Grupo>) respuesta.getDatos();
                            SwingUtilities.invokeLater(() -> {
                                if (grupos == null || grupos.isEmpty()) {
                                    modeloGrupos.clear();
                                    modeloGrupos.addElement("No tienes grupos");
                                } else {
                                    actualizarListaGrupos(grupos);
                                }
                            });
                            return;
                        }
                    } catch (Exception e) {
                        Thread.sleep(200);
                    }
                    intentos++;
                }
                SwingUtilities.invokeLater(() -> {
                    modeloGrupos.clear();
                    modeloGrupos.addElement("No tienes grupos");
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    modeloGrupos.clear();
                    modeloGrupos.addElement("Error al cargar grupos");
                });
            }
        }).start();
    }
    private void cargarUsuarios() {
        solicitarDatos();
    }
    @Deprecated
    private void cargarUsuariosOld() {
        new Thread(() -> {
            try {
                java.util.Set<Integer> idsAmigos = new java.util.HashSet<>();
                Peticion pAmigos = new Peticion("OBTENER_AMIGOS", usuarioActualId);
                Cliente.getInstance().enviar(pAmigos);
                Thread.sleep(500);
                Peticion respuestaAmigos = null;
                int intentos = 0;
                while (intentos < 20) {
                    try {
                        respuestaAmigos = Cliente.getInstance().recibir();
                        if (respuestaAmigos != null && 
                            (respuestaAmigos.getAccion().equals("LISTA_AMIGOS_OK") || 
                             respuestaAmigos.getAccion().equals("AMIGOS_OBTENIDOS"))) {
                            @SuppressWarnings("unchecked")
                            List<models.Amistad> amistades = (List<models.Amistad>) respuestaAmigos.getDatos();
                            if (amistades != null) {
                                for (models.Amistad amistad : amistades) {
                                    if (amistad != null && "aceptada".equals(amistad.getEstado())) {
                                        int otroUsuarioId = amistad.getFk_usuario1() == usuarioActualId ?
                                            amistad.getFk_usuario2() : amistad.getFk_usuario1();
                                        idsAmigos.add(otroUsuarioId);
                                    }
                                }
                            }
                            break;
                        }
                    } catch (Exception e) {
                        Thread.sleep(200);
                    }
                    intentos++;
                }
                if (idsAmigos.isEmpty()) {
                    SwingUtilities.invokeLater(() -> {
                        modeloUsuarios.clear();
                        modeloUsuarios.addElement("No tienes amigos disponibles");
                    });
                    return;
                }
                Peticion pUsuarios = new Peticion("OBTENER_USUARIOS", null);
                Cliente.getInstance().enviar(pUsuarios);
                Thread.sleep(500);
                Peticion respuestaUsuarios = null;
                intentos = 0;
                while (intentos < 20) {
                    try {
                        respuestaUsuarios = Cliente.getInstance().recibir();
                        if (respuestaUsuarios != null && respuestaUsuarios.getAccion().equals("LISTA_USUARIOS")) {
                            @SuppressWarnings("unchecked")
                            List<Usuario> todosUsuarios = (List<Usuario>) respuestaUsuarios.getDatos();
                            java.util.List<Usuario> amigosUsuarios = new java.util.ArrayList<>();
                            if (todosUsuarios != null) {
                                for (Usuario usuario : todosUsuarios) {
                                    if (usuario != null && idsAmigos.contains(usuario.getPk_usuario())) {
                                        amigosUsuarios.add(usuario);
                                    }
                                }
                            }
                            SwingUtilities.invokeLater(() -> {
                                if (amigosUsuarios.isEmpty()) {
                                    modeloUsuarios.clear();
                                    modeloUsuarios.addElement("No tienes amigos disponibles");
                                } else {
                                    actualizarListaUsuarios(amigosUsuarios);
                                }
                            });
                            return;
                        }
                    } catch (Exception e) {
                        Thread.sleep(200);
                    }
                    intentos++;
                }
                SwingUtilities.invokeLater(() -> {
                    modeloUsuarios.clear();
                    modeloUsuarios.addElement("Error al obtener usuarios");
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    modeloUsuarios.clear();
                    modeloUsuarios.addElement("Error al cargar amigos");
                });
            }
        }).start();
    }
    private void actualizarListaGrupos(List<Grupo> grupos) {
        modeloGrupos.clear();
        for (Grupo grupo : grupos) {
            modeloGrupos.addElement(grupo.getPk_grupo() + " - " + grupo.getTitulo());
        }
    }
    private void actualizarListaUsuarios(List<Usuario> usuarios) {
        SwingUtilities.invokeLater(() -> {
            modeloUsuarios.clear();
            listaUsuariosDisponibles.clearSelection();
            for (Usuario usuario : usuarios) {
                if (usuario.getPk_usuario() != usuarioActualId) {
                    modeloUsuarios.addElement(
                        usuario.getPk_usuario() + " - " +
                        usuario.getNombre() + " (" + usuario.getUsername() + ")");
                }
            }
            listaUsuariosDisponibles.setEnabled(true);
            listaUsuariosDisponibles.setVisible(true);
            if (modeloUsuarios.size() > 0) {
                listaUsuariosDisponibles.setToolTipText("Haz clic para seleccionar. Ctrl+Click para selección múltiple. Shift+Click para rango.");
            }
        });
    }
    private void cargarUsuariosGestion() {
        procesarAmistadesParaGestion();
    }
    private void procesarAmistadesParaGestion() {
        new Thread(() -> {
            try {
                Peticion pAmigos = new Peticion("OBTENER_AMIGOS", usuarioActualId);
                Cliente.getInstance().enviar(pAmigos);
                Thread.sleep(300);
                Peticion respuestaAmigos = null;
                int intentos = 0;
                while (intentos < 15) {
                    try {
                        respuestaAmigos = Cliente.getInstance().recibir();
                        if (respuestaAmigos != null && 
                            (respuestaAmigos.getAccion().equals("LISTA_AMIGOS_OK") || 
                             respuestaAmigos.getAccion().equals("AMIGOS_OBTENIDOS"))) {
                            break;
                        }
                    } catch (Exception e) {
                        Thread.sleep(100);
                    }
                    intentos++;
                }
                if (respuestaAmigos != null && 
                    (respuestaAmigos.getAccion().equals("LISTA_AMIGOS_OK") || 
                     respuestaAmigos.getAccion().equals("AMIGOS_OBTENIDOS"))) {
                    @SuppressWarnings("unchecked")
                    List<models.Amistad> amistades = (List<models.Amistad>) respuestaAmigos.getDatos();
                    java.util.Set<Integer> idsAmigos = new java.util.HashSet<>();
                    if (amistades != null) {
                        for (models.Amistad amistad : amistades) {
                            if (amistad != null && "aceptada".equals(amistad.getEstado())) {
                                int otroUsuarioId = amistad.getFk_usuario1() == usuarioActualId ?
                                    amistad.getFk_usuario2() : amistad.getFk_usuario1();
                                if (otroUsuarioId != usuarioActualId && otroUsuarioId > 0) {
                                    idsAmigos.add(otroUsuarioId);
                                }
                            }
                        }
                    }
                    if (!idsAmigos.isEmpty()) {
                        Peticion pUsuarios = new Peticion("OBTENER_USUARIOS", null);
                        Cliente.getInstance().enviar(pUsuarios);
                        Thread.sleep(300);
                        Peticion respuestaUsuarios = null;
                        intentos = 0;
                        while (intentos < 15) {
                            try {
                                respuestaUsuarios = Cliente.getInstance().recibir();
                                if (respuestaUsuarios != null && respuestaUsuarios.getAccion().equals("LISTA_USUARIOS")) {
                                    break;
                                }
                            } catch (Exception e) {
                                Thread.sleep(100);
                            }
                            intentos++;
                        }
                        if (respuestaUsuarios != null && respuestaUsuarios.getAccion().equals("LISTA_USUARIOS")) {
                            @SuppressWarnings("unchecked")
                            List<Usuario> todosUsuarios = (List<Usuario>) respuestaUsuarios.getDatos();
                            java.util.List<Usuario> amigosUsuarios = new java.util.ArrayList<>();
                            if (todosUsuarios != null) {
                                for (Usuario usuario : todosUsuarios) {
                                    if (usuario != null && idsAmigos.contains(usuario.getPk_usuario())) {
                                        amigosUsuarios.add(usuario);
                                    }
                                }
                            }
                            SwingUtilities.invokeLater(() -> {
                                modeloUsuariosGestion.clear();
                                for (Usuario usuario : amigosUsuarios) {
                                    modeloUsuariosGestion.addElement(
                                        usuario.getPk_usuario() + " - " +
                                        usuario.getNombre() + " (" + usuario.getUsername() + ")");
                                }
                            });
                        }
                    }
                }
            } catch (Exception ex) {}
        }).start();
    }
    private int grupoSeleccionadoId = -1;
    private void cargarMiembrosGrupo() {
        if (listaGrupos.isSelectionEmpty()) {
            SwingUtilities.invokeLater(() -> {
                modeloMiembros.clear();
                modeloMiembros.addElement("Selecciona un grupo para ver sus miembros");
            });
            grupoSeleccionadoId = -1;
            return;
        }
        String grupoStr = listaGrupos.getSelectedValue();
        if (grupoStr == null || grupoStr.trim().isEmpty()) {
            SwingUtilities.invokeLater(() -> {
                modeloMiembros.clear();
                modeloMiembros.addElement("Selecciona un grupo para ver sus miembros");
            });
            grupoSeleccionadoId = -1;
            return;
        }
        try {
            int grupoId = Integer.parseInt(grupoStr.split(" - ")[0]);
            grupoSeleccionadoId = grupoId;
            SwingUtilities.invokeLater(() -> {
                modeloMiembros.clear();
                modeloMiembros.addElement("Cargando miembros...");
            });
            new Thread(() -> {
                try {
                    Thread.sleep(100);
                    Peticion p = new Peticion("OBTENER_MIEMBROS_GRUPO", grupoId);
                    Cliente.getInstance().enviar(p);
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        modeloMiembros.clear();
                        modeloMiembros.addElement("Error al solicitar miembros");
                    });
                }
            }).start();
        } catch (NumberFormatException ex) {
            SwingUtilities.invokeLater(() -> {
                modeloMiembros.clear();
                modeloMiembros.addElement("Error: formato de grupo inválido");
            });
            grupoSeleccionadoId = -1;
        }
    }
}
