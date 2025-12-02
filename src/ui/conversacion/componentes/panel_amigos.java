package ui.conversacion.componentes;
import models.Amigo;
import models.Usuario;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
public class panel_amigos extends JPanel {
    private DefaultListModel<String> modeloAmigos;
    private JList<String> listaAmigos;
    private Map<String, Integer> mapaAmigos;
    private int usuarioActualId;
    public panel_amigos() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Amigos"));
        setPreferredSize(new Dimension(200, 0));
        modeloAmigos = new DefaultListModel<>();
        listaAmigos = new JList<>(modeloAmigos);
        listaAmigos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollAmigos = new JScrollPane(listaAmigos);
        scrollAmigos.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollAmigos, BorderLayout.CENTER);
        mapaAmigos = new HashMap<>();
    }
    public void setUsuarioActualId(int id) {
        this.usuarioActualId = id;
    }
    public int getUsuarioActualId() {
        return usuarioActualId;
    }
    public void actualizarAmigos(List<Amigo> amigos, List<Usuario> usuarios) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            modeloAmigos.clear();
            mapaAmigos.clear();
            Map<Integer, Usuario> mapaUsuarios = new HashMap<>();
            for (Usuario u : usuarios) {
                mapaUsuarios.put(u.getPk_usuario(), u);
            }
            for (Amigo amigo : amigos) {
                if (amigo.getEstado() == 1) {
                    int otroUsuarioId = amigo.getFk_usuario1() == usuarioActualId ?
                        amigo.getFk_usuario2() : amigo.getFk_usuario1();
                    Usuario otroUsuario = mapaUsuarios.get(otroUsuarioId);
                    if (otroUsuario != null) {
                        String estado = otroUsuario.getEstado() == 1 ? "[*]" : "[ ]";
                        String nombre = estado + " " + otroUsuario.getNombre() +
                            " (" + otroUsuario.getUsername() + ")";
                        modeloAmigos.addElement(nombre);
                        mapaAmigos.put(nombre, otroUsuarioId);
                    }
                }
            }
        });
    }
    public boolean tieneSeleccion() {
        return !listaAmigos.isSelectionEmpty();
    }
    public int getSeleccionId() {
        String seleccion = listaAmigos.getSelectedValue();
        return mapaAmigos.getOrDefault(seleccion, -1);
    }
    public String getSeleccion() {
        return listaAmigos.getSelectedValue();
    }
    public void limpiar() {
        modeloAmigos.clear();
        mapaAmigos.clear();
    }
    public void addListSelectionListener(javax.swing.event.ListSelectionListener listener) {
        listaAmigos.addListSelectionListener(listener);
    }
    public void addMouseListener(java.awt.event.MouseListener listener) {
        listaAmigos.addMouseListener(listener);
    }
    public void actualizarAmistades(List<models.Amistad> amistades) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            modeloAmigos.clear();
            mapaAmigos.clear();
            for (models.Amistad amistad : amistades) {
                if ("aceptada".equals(amistad.getEstado())) {
                    int otroUsuarioId = amistad.getFk_usuario1() == usuarioActualId ?
                        amistad.getFk_usuario2() : amistad.getFk_usuario1();
                    String nombre = amistad.getNombreAmigo() != null ?
                        amistad.getNombreAmigo() : "Usuario " + otroUsuarioId;
                    String display = "[ ] " + nombre;
                    modeloAmigos.addElement(display);
                    mapaAmigos.put(display, otroUsuarioId);
                }
            }
        });
    }
}
