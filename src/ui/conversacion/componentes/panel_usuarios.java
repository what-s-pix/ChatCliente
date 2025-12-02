package ui.conversacion.componentes;
import models.Usuario;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
public class panel_usuarios extends JPanel {
    private DefaultListModel<String> modeloUsuarios;
    private JList<String> listaUsuarios;
    private int usuarioActualId;
    private Map<String, Integer> mapaUsuarios;
    public panel_usuarios() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Usuarios"));
        setPreferredSize(new Dimension(200, 0));
        modeloUsuarios = new DefaultListModel<>();
        listaUsuarios = new JList<>(modeloUsuarios);
        listaUsuarios.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollUsuarios = new JScrollPane(listaUsuarios);
        scrollUsuarios.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollUsuarios, BorderLayout.CENTER);
        mapaUsuarios = new HashMap<>();
    }
    public void setUsuarioActualId(int id) {
        this.usuarioActualId = id;
    }
    public void actualizarUsuarios(List<Usuario> usuarios) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            modeloUsuarios.clear();
            mapaUsuarios.clear();
            for (Usuario u : usuarios) {
                if (u.getPk_usuario() != usuarioActualId) {
                    String estado = u.getEstado() == 1 ? "[Online]" : "[Offline]";
                    String nombre = estado + " " + u.getNombre() +
                        " (" + u.getUsername() + ")";
                    modeloUsuarios.addElement(nombre);
                    mapaUsuarios.put(nombre, u.getPk_usuario());
                }
            }
        });
    }
    public int getSeleccionId() {
        String seleccion = listaUsuarios.getSelectedValue();
        return mapaUsuarios.getOrDefault(seleccion, -1);
    }
    public boolean tieneSeleccion() {
        return !listaUsuarios.isSelectionEmpty();
    }
    public String getSeleccion() {
        return listaUsuarios.getSelectedValue();
    }
    public void limpiar() {
        modeloUsuarios.clear();
    }
    public void addListSelectionListener(javax.swing.event.ListSelectionListener listener) {
        listaUsuarios.addListSelectionListener(listener);
    }
    public void addMouseListener(java.awt.event.MouseListener listener) {
        listaUsuarios.addMouseListener(listener);
    }
}
