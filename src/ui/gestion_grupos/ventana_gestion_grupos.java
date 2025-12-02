package ui.gestion_grupos;
import chatcliente.Cliente;
import common.Peticion;
import models.Grupo;
import models.InvitacionGrupo;
import models.Usuario;
import javax.swing.*;
import java.awt.*;
import java.util.List;
public class ventana_gestion_grupos extends JDialog {
    private int usuarioActualId;
    private DefaultListModel<String> modeloGrupos;
    private JList<String> listaGrupos;
    private JTextField txtTituloGrupo;
    private JList<String> listaUsuariosDisponibles;
    private DefaultListModel<String> modeloUsuarios;
    public ventana_gestion_grupos(JFrame parent, int usuarioActualId) {
        super(parent, "Gestión de Grupos", true);
        this.usuarioActualId = usuarioActualId;
        configurarVentana();
        inicializarComponentes();
        cargarGrupos();
        cargarUsuarios();
    }
    private void configurarVentana() {
        setSize(600, 500);
        setLocationRelativeTo(getParent());
    }
    private void inicializarComponentes() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel panelCrear = new JPanel(new BorderLayout(5, 5));
        panelCrear.setBorder(BorderFactory.createTitledBorder("Crear Nuevo Grupo"));
        txtTituloGrupo = new JTextField();
        JButton btnCrear = new JButton("Crear Grupo");
        btnCrear.addActionListener(e -> crearGrupo());
        panelCrear.add(new JLabel("Título:"), BorderLayout.WEST);
        panelCrear.add(txtTituloGrupo, BorderLayout.CENTER);
        panelCrear.add(btnCrear, BorderLayout.EAST);
        JPanel panelGrupos = new JPanel(new BorderLayout());
        panelGrupos.setBorder(BorderFactory.createTitledBorder("Mis Grupos"));
        modeloGrupos = new DefaultListModel<>();
        listaGrupos = new JList<>(modeloGrupos);
        JScrollPane scrollGrupos = new JScrollPane(listaGrupos);
        panelGrupos.add(scrollGrupos, BorderLayout.CENTER);
        JPanel panelBotonesGrupos = new JPanel(new FlowLayout());
        JButton btnAgregarMiembro = new JButton("Agregar Miembro");
        JButton btnEliminarMiembro = new JButton("Eliminar Miembro");
        JButton btnSalirGrupo = new JButton("Salir del Grupo");
        btnAgregarMiembro.addActionListener(e -> agregarMiembro());
        btnEliminarMiembro.addActionListener(e -> eliminarMiembro());
        btnSalirGrupo.addActionListener(e -> salirGrupo());
        panelBotonesGrupos.add(btnAgregarMiembro);
        panelBotonesGrupos.add(btnEliminarMiembro);
        panelBotonesGrupos.add(btnSalirGrupo);
        panelGrupos.add(panelBotonesGrupos, BorderLayout.SOUTH);
        JPanel panelUsuarios = new JPanel(new BorderLayout());
        panelUsuarios.setBorder(BorderFactory.createTitledBorder("Amigos Disponibles"));
        modeloUsuarios = new DefaultListModel<>();
        listaUsuariosDisponibles = new JList<>(modeloUsuarios);
        listaUsuariosDisponibles.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollUsuarios = new JScrollPane(listaUsuariosDisponibles);
        panelUsuarios.add(scrollUsuarios, BorderLayout.CENTER);
        JPanel panelBotones = new JPanel(new FlowLayout());
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());
        panelBotones.add(btnCerrar);
        JSplitPane splitHorizontal = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT, panelGrupos, panelUsuarios);
        splitHorizontal.setDividerLocation(300);
        panelPrincipal.add(panelCrear, BorderLayout.NORTH);
        panelPrincipal.add(splitHorizontal, BorderLayout.CENTER);
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);
        add(panelPrincipal);
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
            Peticion respuesta = Cliente.getInstance().recibir();
            if (respuesta.getAccion().equals("GRUPO_CREADO")) {
                JOptionPane.showMessageDialog(this, "Grupo creado exitosamente.",
                    "Información", JOptionPane.PLAIN_MESSAGE);
                txtTituloGrupo.setText("");
                cargarGrupos();
            } else {
                JOptionPane.showMessageDialog(this, "Error: " + respuesta.getDatos(),
                    "Error", JOptionPane.PLAIN_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                "Error", JOptionPane.PLAIN_MESSAGE);
        }
    }
    private void agregarMiembro() {
        if (listaGrupos.isSelectionEmpty() || listaUsuariosDisponibles.isSelectionEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecciona un grupo y usuarios.",
                "Información", JOptionPane.PLAIN_MESSAGE);
            return;
        }
        String grupoStr = listaGrupos.getSelectedValue();
        int grupoId = Integer.parseInt(grupoStr.split(" - ")[0]);
        int[] seleccionados = listaUsuariosDisponibles.getSelectedIndices();
        try {
            for (int idx : seleccionados) {
                String usuarioStr = modeloUsuarios.getElementAt(idx);
                int usuarioId = Integer.parseInt(usuarioStr.split(" - ")[0]);
                InvitacionGrupo invitacion = new InvitacionGrupo(grupoId, usuarioId, usuarioActualId);
                Peticion p = new Peticion("AGREGAR_MIEMBRO_GRUPO", invitacion);
                Cliente.getInstance().enviar(p);
                Cliente.getInstance().recibir();
            }
            JOptionPane.showMessageDialog(this, "Invitaciones enviadas.",
                "Información", JOptionPane.PLAIN_MESSAGE);
            cargarGrupos();
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
            Peticion respuesta = Cliente.getInstance().recibir();
            if (respuesta.getAccion().equals("MIEMBRO_ELIMINADO")) {
                JOptionPane.showMessageDialog(this, "Miembro eliminado.",
                    "Información", JOptionPane.PLAIN_MESSAGE);
                cargarGrupos();
            } else {
                JOptionPane.showMessageDialog(this, "Error: " + respuesta.getDatos(),
                    "Error", JOptionPane.PLAIN_MESSAGE);
            }
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
                Peticion p = new Peticion("SALIR_GRUPO",
                    new Object[] {grupoId, usuarioActualId});
                Cliente.getInstance().enviar(p);
                Cliente.getInstance().recibir();
                cargarGrupos();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.PLAIN_MESSAGE);
            }
        }
    }
    private void cargarGrupos() {
        try {
            Peticion p = new Peticion("OBTENER_GRUPOS", usuarioActualId);
            Cliente.getInstance().enviar(p);
            Peticion respuesta = Cliente.getInstance().recibir();
            if (respuesta.getAccion().equals("GRUPOS_OBTENIDOS")) {
                @SuppressWarnings("unchecked")
                List<Grupo> grupos = (List<Grupo>) respuesta.getDatos();
                actualizarListaGrupos(grupos);
            }
        } catch (Exception ex) {}
    }
    private void cargarUsuarios() {
        try {
            java.util.Set<Integer> idsAmigos = new java.util.HashSet<>();
            Peticion pAmigos = new Peticion("OBTENER_AMIGOS", usuarioActualId);
            Cliente.getInstance().enviar(pAmigos);
            Peticion respuestaAmigos = Cliente.getInstance().recibir();
            if (respuestaAmigos.getAccion().equals("LISTA_AMIGOS_OK") || respuestaAmigos.getAccion().equals("AMIGOS_OBTENIDOS")) {
                @SuppressWarnings("unchecked")
                List<models.Amistad> amistades = (List<models.Amistad>) respuestaAmigos.getDatos();
                for (models.Amistad amistad : amistades) {
                    if ("aceptada".equals(amistad.getEstado())) {
                        int otroUsuarioId = amistad.getFk_usuario1() == usuarioActualId ?
                            amistad.getFk_usuario2() : amistad.getFk_usuario1();
                        idsAmigos.add(otroUsuarioId);
                    }
                }
            }
            if (idsAmigos.isEmpty()) {
                modeloUsuarios.clear();
                return;
            }
            Peticion pUsuarios = new Peticion("OBTENER_USUARIOS", null);
            Cliente.getInstance().enviar(pUsuarios);
            Peticion respuestaUsuarios = Cliente.getInstance().recibir();
            if (respuestaUsuarios.getAccion().equals("LISTA_USUARIOS")) {
                @SuppressWarnings("unchecked")
                List<Usuario> todosUsuarios = (List<Usuario>) respuestaUsuarios.getDatos();
                java.util.List<Usuario> amigosUsuarios = new java.util.ArrayList<>();
                for (Usuario usuario : todosUsuarios) {
                    if (idsAmigos.contains(usuario.getPk_usuario())) {
                        amigosUsuarios.add(usuario);
                    }
                }
                actualizarListaUsuarios(amigosUsuarios);
            }
        } catch (Exception ex) {}
    }
    private void actualizarListaGrupos(List<Grupo> grupos) {
        modeloGrupos.clear();
        for (Grupo grupo : grupos) {
            modeloGrupos.addElement(grupo.getPk_grupo() + " - " + grupo.getTitulo());
        }
    }
    private void actualizarListaUsuarios(List<Usuario> usuarios) {
        modeloUsuarios.clear();
        for (Usuario usuario : usuarios) {
            if (usuario.getPk_usuario() != usuarioActualId) {
                modeloUsuarios.addElement(
                    usuario.getPk_usuario() + " - " +
                    usuario.getNombre() + " (" + usuario.getUsername() + ")");
            }
        }
    }
}
