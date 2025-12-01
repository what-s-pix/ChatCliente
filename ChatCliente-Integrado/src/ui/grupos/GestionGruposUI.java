package ui.grupos;

import chatcliente.Cliente;
import common.Peticion;
import models.Grupo;
import models.InvitacionGrupo;
import models.Usuario;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GestionGruposUI extends JDialog {
    
    private int usuarioActualId;
    private DefaultListModel<String> modeloGrupos;
    private JList<String> listaGrupos;
    private JTextField txtTituloGrupo;
    private JList<String> listaUsuariosDisponibles;
    private DefaultListModel<String> modeloUsuarios;
    
    public GestionGruposUI(JFrame parent, int usuarioActualId) {
        super(parent, "gestion de grupos", true);
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
        panelCrear.setBorder(BorderFactory.createTitledBorder("crear nuevo grupo"));
        txtTituloGrupo = new JTextField();
        JButton btnCrear = new JButton("crear grupo");
        btnCrear.addActionListener(e -> crearGrupo());
        panelCrear.add(new JLabel("titulo:"), BorderLayout.WEST);
        panelCrear.add(txtTituloGrupo, BorderLayout.CENTER);
        panelCrear.add(btnCrear, BorderLayout.EAST);
        
        JPanel panelGrupos = new JPanel(new BorderLayout());
        panelGrupos.setBorder(BorderFactory.createTitledBorder("mis grupos"));
        modeloGrupos = new DefaultListModel<>();
        listaGrupos = new JList<>(modeloGrupos);
        JScrollPane scrollGrupos = new JScrollPane(listaGrupos);
        panelGrupos.add(scrollGrupos, BorderLayout.CENTER);
        
        JPanel panelBotonesGrupos = new JPanel(new FlowLayout());
        JButton btnAgregarMiembro = new JButton("agregar miembro");
        JButton btnEliminarMiembro = new JButton("eliminar miembro");
        JButton btnSalirGrupo = new JButton("salir del grupo");
        btnAgregarMiembro.addActionListener(e -> agregarMiembro());
        btnEliminarMiembro.addActionListener(e -> eliminarMiembro());
        btnSalirGrupo.addActionListener(e -> salirGrupo());
        panelBotonesGrupos.add(btnAgregarMiembro);
        panelBotonesGrupos.add(btnEliminarMiembro);
        panelBotonesGrupos.add(btnSalirGrupo);
        panelGrupos.add(panelBotonesGrupos, BorderLayout.SOUTH);
        
        JPanel panelUsuarios = new JPanel(new BorderLayout());
        panelUsuarios.setBorder(BorderFactory.createTitledBorder("usuarios disponibles"));
        modeloUsuarios = new DefaultListModel<>();
        listaUsuariosDisponibles = new JList<>(modeloUsuarios);
        listaUsuariosDisponibles.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollUsuarios = new JScrollPane(listaUsuariosDisponibles);
        panelUsuarios.add(scrollUsuarios, BorderLayout.CENTER);
        
        JPanel panelBotones = new JPanel(new FlowLayout());
        JButton btnCerrar = new JButton("cerrar");
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
            JOptionPane.showMessageDialog(this, "el titulo es obligatorio.");
            return;
        }
        
        int[] seleccionados = listaUsuariosDisponibles.getSelectedIndices();
        if (seleccionados.length < 2) {
            JOptionPane.showMessageDialog(this, 
                "se necesitan al menos 2 usuarios para crear un grupo (minimo 3 personas).");
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
                JOptionPane.showMessageDialog(this, "grupo creado exitosamente.");
                txtTituloGrupo.setText("");
                cargarGrupos();
            } else {
                JOptionPane.showMessageDialog(this, "error: " + respuesta.getDatos());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "error: " + ex.getMessage());
        }
    }
    
    private void agregarMiembro() {
        if (listaGrupos.isSelectionEmpty() || listaUsuariosDisponibles.isSelectionEmpty()) {
            JOptionPane.showMessageDialog(this, "selecciona un grupo y usuarios.");
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
            JOptionPane.showMessageDialog(this, "invitaciones enviadas.");
            cargarGrupos();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "error: " + ex.getMessage());
        }
    }
    
    private void eliminarMiembro() {
        if (listaGrupos.isSelectionEmpty()) {
            return;
        }
        
        String grupoStr = listaGrupos.getSelectedValue();
        int grupoId = Integer.parseInt(grupoStr.split(" - ")[0]);
        
        String usuarioStr = JOptionPane.showInputDialog(this, 
            "ingresa el id del usuario a eliminar:");
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
                JOptionPane.showMessageDialog(this, "miembro eliminado.");
                cargarGrupos();
            } else {
                JOptionPane.showMessageDialog(this, "error: " + respuesta.getDatos());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "error: " + ex.getMessage());
        }
    }
    
    private void salirGrupo() {
        if (listaGrupos.isSelectionEmpty()) {
            return;
        }
        
        String grupoStr = listaGrupos.getSelectedValue();
        int grupoId = Integer.parseInt(grupoStr.split(" - ")[0]);
        
        int opcion = JOptionPane.showConfirmDialog(this,
            "estas seguro de que deseas salir del grupo?",
            "confirmar",
            JOptionPane.YES_NO_OPTION);
        
        if (opcion == JOptionPane.YES_OPTION) {
            try {
                Peticion p = new Peticion("SALIR_GRUPO", 
                    new Object[] {grupoId, usuarioActualId});
                Cliente.getInstance().enviar(p);
                Cliente.getInstance().recibir();
                cargarGrupos();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "error: " + ex.getMessage());
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
        } catch (Exception ex) {
            // ignorar errores
        }
    }
    
    private void cargarUsuarios() {
        try {
            Peticion p = new Peticion("OBTENER_USUARIOS_DISPONIBLES", usuarioActualId);
            Cliente.getInstance().enviar(p);
            Peticion respuesta = Cliente.getInstance().recibir();
            
            if (respuesta.getAccion().equals("USUARIOS_DISPONIBLES_OBTENIDOS")) {
                @SuppressWarnings("unchecked")
                List<Usuario> usuarios = (List<Usuario>) respuesta.getDatos();
                actualizarListaUsuarios(usuarios);
            }
        } catch (Exception ex) {
            // ignorar errores
        }
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
