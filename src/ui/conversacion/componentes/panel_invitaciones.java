package ui.conversacion.componentes;
import models.Amigo;
import models.InvitacionGrupo;
import javax.swing.*;
import java.awt.*;
import java.util.List;
public class panel_invitaciones extends JPanel {
    private DefaultListModel<String> modeloInvitacionesAmigos;
    private DefaultListModel<String> modeloInvitacionesGrupos;
    private JList<String> listaInvitacionesAmigos;
    private JList<String> listaInvitacionesGrupos;
    private JButton btnAceptarAmigo;
    private JButton btnRechazarAmigo;
    private JButton btnAceptarGrupo;
    private JButton btnRechazarGrupo;
    public panel_invitaciones() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Invitaciones Pendientes"));
        setPreferredSize(new Dimension(300, 0));
        JTabbedPane tabbedPane = new JTabbedPane();
        JPanel panelAmigos = crearPanelAmigos();
        JPanel panelGrupos = crearPanelGrupos();
        tabbedPane.addTab("Amigos", panelAmigos);
        tabbedPane.addTab("Grupos", panelGrupos);
        add(tabbedPane, BorderLayout.CENTER);
    }
    private JPanel crearPanelAmigos() {
        JPanel panelAmigos = new JPanel(new BorderLayout(5, 5));
        panelAmigos.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        modeloInvitacionesAmigos = new DefaultListModel<>();
        listaInvitacionesAmigos = new JList<>(modeloInvitacionesAmigos);
        listaInvitacionesAmigos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaInvitacionesAmigos.setCellRenderer(new InvitacionListCellRenderer());
        JScrollPane scrollAmigos = new JScrollPane(listaInvitacionesAmigos);
        scrollAmigos.setBorder(BorderFactory.createTitledBorder("Solicitudes de Amistad"));
        panelAmigos.add(scrollAmigos, BorderLayout.CENTER);
        JPanel panelBotonesAmigos = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        btnAceptarAmigo = new JButton("Aceptar");
        btnAceptarAmigo.setEnabled(false);
        btnRechazarAmigo = new JButton("Rechazar");
        btnRechazarAmigo.setEnabled(false);
        listaInvitacionesAmigos.addListSelectionListener(e -> {
            boolean tieneSeleccion = !listaInvitacionesAmigos.isSelectionEmpty();
            btnAceptarAmigo.setEnabled(tieneSeleccion);
            btnRechazarAmigo.setEnabled(tieneSeleccion);
        });
        panelBotonesAmigos.add(btnAceptarAmigo);
        panelBotonesAmigos.add(btnRechazarAmigo);
        panelAmigos.add(panelBotonesAmigos, BorderLayout.SOUTH);
        return panelAmigos;
    }
    private JPanel crearPanelGrupos() {
        JPanel panelGrupos = new JPanel(new BorderLayout(5, 5));
        panelGrupos.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        modeloInvitacionesGrupos = new DefaultListModel<>();
        listaInvitacionesGrupos = new JList<>(modeloInvitacionesGrupos);
        listaInvitacionesGrupos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaInvitacionesGrupos.setCellRenderer(new InvitacionListCellRenderer());
        JScrollPane scrollGrupos = new JScrollPane(listaInvitacionesGrupos);
        scrollGrupos.setBorder(BorderFactory.createTitledBorder("Invitaciones a Grupos"));
        panelGrupos.add(scrollGrupos, BorderLayout.CENTER);
        JPanel panelBotonesGrupos = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        btnAceptarGrupo = new JButton("Aceptar");
        btnAceptarGrupo.setEnabled(false);
        btnRechazarGrupo = new JButton("Rechazar");
        btnRechazarGrupo.setEnabled(false);
        listaInvitacionesGrupos.addListSelectionListener(e -> {
            boolean tieneSeleccion = !listaInvitacionesGrupos.isSelectionEmpty();
            btnAceptarGrupo.setEnabled(tieneSeleccion);
            btnRechazarGrupo.setEnabled(tieneSeleccion);
        });
        panelBotonesGrupos.add(btnAceptarGrupo);
        panelBotonesGrupos.add(btnRechazarGrupo);
        panelGrupos.add(panelBotonesGrupos, BorderLayout.SOUTH);
        return panelGrupos;
    }
    public void actualizarInvitacionesAmigos(List<Amigo> invitaciones) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            modeloInvitacionesAmigos.clear();
            for (Amigo invitacion : invitaciones) {
                if (invitacion.getEstado() == 0) {
                    String nombre = invitacion.getFk_usuario1() == invitacion.getFk_usuario2() ?
                        invitacion.getNombre_usuario1() : invitacion.getNombre_usuario2();
                    if (nombre == null || nombre.trim().isEmpty()) {
                        nombre = "Usuario " + (invitacion.getFk_usuario1() == invitacion.getFk_usuario2() ?
                            invitacion.getFk_usuario1() : invitacion.getFk_usuario2());
                    }
                    modeloInvitacionesAmigos.addElement(
                        invitacion.getPk_amigo() + " - " + nombre);
                }
            }
            actualizarEstadoBotones();
        });
    }
    public void actualizarInvitacionesGrupos(List<InvitacionGrupo> invitaciones) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            modeloInvitacionesGrupos.clear();
            for (InvitacionGrupo invitacion : invitaciones) {
                String estado = invitacion.getEstado();
                if (estado != null && (estado.equals("pendiente") || estado.equals("0"))) {
                    String titulo = invitacion.getTitulo_grupo() != null ?
                        invitacion.getTitulo_grupo() :
                        (invitacion.getTituloGrupo() != null ? invitacion.getTituloGrupo() : "Sin título");
                    String invitador = invitacion.getNombre_invitador() != null ?
                        invitacion.getNombre_invitador() :
                        (invitacion.getNombreInvitador() != null ? invitacion.getNombreInvitador() : "Desconocido");
                    modeloInvitacionesGrupos.addElement(
                        invitacion.getPk_invitacion() + " - " +
                        titulo + " (de " + invitador + ")");
                }
            }
            actualizarEstadoBotones();
        });
    }
    private void actualizarEstadoBotones() {
        btnAceptarAmigo.setEnabled(!listaInvitacionesAmigos.isSelectionEmpty());
        btnRechazarAmigo.setEnabled(!listaInvitacionesAmigos.isSelectionEmpty());
        btnAceptarGrupo.setEnabled(!listaInvitacionesGrupos.isSelectionEmpty());
        btnRechazarGrupo.setEnabled(!listaInvitacionesGrupos.isSelectionEmpty());
    }
    public boolean tieneSeleccionAmigo() {
        return !listaInvitacionesAmigos.isSelectionEmpty();
    }
    public int getSeleccionAmigoId() {
        String seleccion = listaInvitacionesAmigos.getSelectedValue();
        if (seleccion == null || seleccion.trim().isEmpty()) {
            return -1;
        }
        try {
            String[] partes = seleccion.split(" - ", 2);
            if (partes.length > 0) {
                return Integer.parseInt(partes[0].trim());
            }
        } catch (NumberFormatException e) {
            return -1;
        }
        return -1;
    }
    public boolean tieneSeleccionGrupo() {
        return !listaInvitacionesGrupos.isSelectionEmpty();
    }
    public int getSeleccionGrupoId() {
        String seleccion = listaInvitacionesGrupos.getSelectedValue();
        if (seleccion == null || seleccion.trim().isEmpty()) {
            return -1;
        }
        try {
            String[] partes = seleccion.split(" - ", 2);
            if (partes.length > 0) {
                return Integer.parseInt(partes[0].trim());
            }
        } catch (NumberFormatException e) {
            return -1;
        }
        return -1;
    }
    public JButton getBtnAceptarAmigo() {
        return btnAceptarAmigo;
    }
    public JButton getBtnRechazarAmigo() {
        return btnRechazarAmigo;
    }
    public JButton getBtnAceptarGrupo() {
        return btnAceptarGrupo;
    }
    public JButton getBtnRechazarGrupo() {
        return btnRechazarGrupo;
    }
    public void actualizarSolicitudesTexto(List<String> solicitudes) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            modeloInvitacionesAmigos.clear();
            for (String solicitud : solicitudes) {
                if (solicitud == null || solicitud.trim().isEmpty()) {
                    continue;
                }
                String[] partes = solicitud.split(":");
                if (partes.length >= 2) {
                    String username = partes[0].trim();
                    String pkAmistad = partes[1].trim();
                    if (!username.isEmpty() && !pkAmistad.isEmpty()) {
                        modeloInvitacionesAmigos.addElement(pkAmistad + " - " + username);
                    }
                }
            }
            actualizarEstadoBotones();
        });
    }
    public void actualizarGruposInvitados(List<models.Grupo> grupos) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            modeloInvitacionesGrupos.clear();
            for (models.Grupo grupo : grupos) {
                if (grupo != null && grupo.getTitulo() != null) {
                    modeloInvitacionesGrupos.addElement(
                        grupo.getPk_grupo() + " - " + grupo.getTitulo());
                }
            }
            actualizarEstadoBotones();
        });
    }
    private static class InvitacionListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value != null) {
                String texto = value.toString();
                if (texto.contains(" - ")) {
                    String[] partes = texto.split(" - ", 2);
                    if (partes.length == 2) {
                        setText(partes[1]);
                        setToolTipText("ID: " + partes[0] + " - " + partes[1]);
                    } else {
                        setText(texto);
                    }
                } else {
                    setText(texto);
                }
            }
            if (isSelected) {
                setBackground(UIManager.getColor("List.selectionBackground"));
                setForeground(UIManager.getColor("List.selectionForeground"));
            } else {
                setBackground(UIManager.getColor("List.background"));
                setForeground(UIManager.getColor("List.foreground"));
            }
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(2, 5, 2, 5),
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Separator.foreground"))
            ));
            return this;
        }
    }
}
