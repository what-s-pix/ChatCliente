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
        setPreferredSize(new Dimension(250, 0));
        JTabbedPane tabbedPane = new JTabbedPane();
        JPanel panelAmigos = new JPanel(new BorderLayout());
        modeloInvitacionesAmigos = new DefaultListModel<>();
        listaInvitacionesAmigos = new JList<>(modeloInvitacionesAmigos);
        JScrollPane scrollAmigos = new JScrollPane(listaInvitacionesAmigos);
        panelAmigos.add(scrollAmigos, BorderLayout.CENTER);
        JPanel panelBotonesAmigos = new JPanel(new FlowLayout());
        btnAceptarAmigo = new JButton("Aceptar");
        btnRechazarAmigo = new JButton("Rechazar");
        panelBotonesAmigos.add(btnAceptarAmigo);
        panelBotonesAmigos.add(btnRechazarAmigo);
        panelAmigos.add(panelBotonesAmigos, BorderLayout.SOUTH);
        JPanel panelGrupos = new JPanel(new BorderLayout());
        modeloInvitacionesGrupos = new DefaultListModel<>();
        listaInvitacionesGrupos = new JList<>(modeloInvitacionesGrupos);
        JScrollPane scrollGrupos = new JScrollPane(listaInvitacionesGrupos);
        panelGrupos.add(scrollGrupos, BorderLayout.CENTER);
        JPanel panelBotonesGrupos = new JPanel(new FlowLayout());
        btnAceptarGrupo = new JButton("Aceptar");
        btnRechazarGrupo = new JButton("Rechazar");
        panelBotonesGrupos.add(btnAceptarGrupo);
        panelBotonesGrupos.add(btnRechazarGrupo);
        panelGrupos.add(panelBotonesGrupos, BorderLayout.SOUTH);
        tabbedPane.addTab("Amigos", panelAmigos);
        tabbedPane.addTab("Grupos", panelGrupos);
        add(tabbedPane, BorderLayout.CENTER);
    }
    public void actualizarInvitacionesAmigos(List<Amigo> invitaciones) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            modeloInvitacionesAmigos.clear();
            for (Amigo invitacion : invitaciones) {
                if (invitacion.getEstado() == 0) {
                    String nombre = invitacion.getFk_usuario1() == invitacion.getFk_usuario2() ?
                        invitacion.getNombre_usuario1() : invitacion.getNombre_usuario2();
                    modeloInvitacionesAmigos.addElement(
                        invitacion.getPk_amigo() + " - " + nombre);
                }
            }
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
        });
    }
    public boolean tieneSeleccionAmigo() {
        return !listaInvitacionesAmigos.isSelectionEmpty();
    }
    public int getSeleccionAmigoId() {
        String seleccion = listaInvitacionesAmigos.getSelectedValue();
        if (seleccion != null) {
            return Integer.parseInt(seleccion.split(" - ")[0]);
        }
        return -1;
    }
    public boolean tieneSeleccionGrupo() {
        return !listaInvitacionesGrupos.isSelectionEmpty();
    }
    public int getSeleccionGrupoId() {
        String seleccion = listaInvitacionesGrupos.getSelectedValue();
        if (seleccion != null) {
            return Integer.parseInt(seleccion.split(" - ")[0]);
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
                String[] partes = solicitud.split(":");
                if (partes.length >= 2) {
                    String username = partes[0];
                    String pkAmistad = partes[1];
                    modeloInvitacionesAmigos.addElement(pkAmistad + " - " + username);
                }
            }
        });
    }
    public void actualizarGruposInvitados(List<models.Grupo> grupos) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            modeloInvitacionesGrupos.clear();
            for (models.Grupo grupo : grupos) {
                modeloInvitacionesGrupos.addElement(
                    grupo.getPk_grupo() + " - " + grupo.getTitulo());
            }
        });
    }
}
