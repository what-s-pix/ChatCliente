package ui.chat.components;

import models.Grupo;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class GruposPanel extends JPanel {
    
    private DefaultListModel<String> modeloGrupos;
    private JList<String> listaGrupos;
    private Map<String, Integer> mapaGrupos;
    
    public GruposPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("grupos"));
        setPreferredSize(new Dimension(200, 0));
        
        modeloGrupos = new DefaultListModel<>();
        listaGrupos = new JList<>(modeloGrupos);
        listaGrupos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollGrupos = new JScrollPane(listaGrupos);
        scrollGrupos.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        add(scrollGrupos, BorderLayout.CENTER);
        mapaGrupos = new HashMap<>();
    }
    
    public void actualizarGrupos(List<Grupo> grupos) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            modeloGrupos.clear();
            mapaGrupos.clear();
            
            for (Grupo grupo : grupos) {
                String nombre = "[grupo] " + grupo.getTitulo();
                modeloGrupos.addElement(nombre);
                mapaGrupos.put(nombre, grupo.getPk_grupo());
            }
        });
    }
    
    public boolean tieneSeleccion() {
        return !listaGrupos.isSelectionEmpty();
    }
    
    public int getSeleccionId() {
        String seleccion = listaGrupos.getSelectedValue();
        return mapaGrupos.getOrDefault(seleccion, -1);
    }
    
    public String getSeleccion() {
        return listaGrupos.getSelectedValue();
    }
    
    public void limpiar() {
        modeloGrupos.clear();
        mapaGrupos.clear();
    }
    
    public void addListSelectionListener(javax.swing.event.ListSelectionListener listener) {
        listaGrupos.addListSelectionListener(listener);
    }
    
    public void addMouseListener(java.awt.event.MouseListener listener) {
        listaGrupos.addMouseListener(listener);
    }
}

