package ui.chat.components;

import models.Amigo;
import models.InvitacionGrupo;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class InvitacionesPanel extends JPanel {
    
    private DefaultListModel<String> modeloInvitacionesAmigos;
    private DefaultListModel<String> modeloInvitacionesGrupos;
    private JList<String> listaInvitacionesAmigos;
    private JList<String> listaInvitacionesGrupos;
    private JButton btnAceptarAmigo;
    private JButton btnRechazarAmigo;
    private JButton btnAceptarGrupo;
    private JButton btnRechazarGrupo;
    
    public InvitacionesPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("invitaciones pendientes"));
        setPreferredSize(new Dimension(250, 0));
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        JPanel panelAmigos = new JPanel(new BorderLayout());
        modeloInvitacionesAmigos = new DefaultListModel<>();
        listaInvitacionesAmigos = new JList<>(modeloInvitacionesAmigos);
        JScrollPane scrollAmigos = new JScrollPane(listaInvitacionesAmigos);
        panelAmigos.add(scrollAmigos, BorderLayout.CENTER);
        
        JPanel panelBotonesAmigos = new JPanel(new FlowLayout());
        btnAceptarAmigo = new JButton("aceptar");
        btnRechazarAmigo = new JButton("rechazar");
        panelBotonesAmigos.add(btnAceptarAmigo);
        panelBotonesAmigos.add(btnRechazarAmigo);
        panelAmigos.add(panelBotonesAmigos, BorderLayout.SOUTH);
        
        JPanel panelGrupos = new JPanel(new BorderLayout());
        modeloInvitacionesGrupos = new DefaultListModel<>();
        listaInvitacionesGrupos = new JList<>(modeloInvitacionesGrupos);
        JScrollPane scrollGrupos = new JScrollPane(listaInvitacionesGrupos);
        panelGrupos.add(scrollGrupos, BorderLayout.CENTER);
        
        JPanel panelBotonesGrupos = new JPanel(new FlowLayout());
        btnAceptarGrupo = new JButton("aceptar");
        btnRechazarGrupo = new JButton("rechazar");
        panelBotonesGrupos.add(btnAceptarGrupo);
        panelBotonesGrupos.add(btnRechazarGrupo);
        panelGrupos.add(panelBotonesGrupos, BorderLayout.SOUTH);
        
        tabbedPane.addTab("amigos", panelAmigos);
        tabbedPane.addTab("grupos", panelGrupos);
        
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
                if (invitacion.getEstado() == 0) {
                    modeloInvitacionesGrupos.addElement(
                        invitacion.getPk_invitacion() + " - " + 
                        invitacion.getTitulo_grupo() + " (de " + 
                        invitacion.getNombre_invitador() + ")");
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
}

