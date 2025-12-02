package ui.gestion_amigos;

import chatcliente.Cliente;
import common.Peticion;
import models.Amigo;
import models.Usuario;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ventana_gestion_amigos extends JDialog {
    
    private int usuarioActualId;
    private DefaultListModel<String> modeloAmigos;
    private JList<String> listaAmigos;
    private JTextField txtBuscarUsuario;
    
    public ventana_gestion_amigos(JFrame parent, int usuarioActualId) {
        super(parent, "Gestión de Amigos", true);
        this.usuarioActualId = usuarioActualId;
        configurarVentana();
        inicializarComponentes();
        cargarAmigos();
    }
    
    private void configurarVentana() {
        setSize(400, 400);
        setLocationRelativeTo(getParent());
    }
    
    private void inicializarComponentes() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel panelBuscar = new JPanel(new BorderLayout(5, 5));
        panelBuscar.setBorder(BorderFactory.createTitledBorder("Buscar Usuario"));
        txtBuscarUsuario = new JTextField();
        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.addActionListener(e -> buscarUsuario());
        panelBuscar.add(new JLabel("Usuario:"), BorderLayout.WEST);
        panelBuscar.add(txtBuscarUsuario, BorderLayout.CENTER);
        panelBuscar.add(btnBuscar, BorderLayout.EAST);
        
        JPanel panelAmigos = new JPanel(new BorderLayout());
        panelAmigos.setBorder(BorderFactory.createTitledBorder("Mis Amigos"));
        modeloAmigos = new DefaultListModel<>();
        listaAmigos = new JList<>(modeloAmigos);
        JScrollPane scrollAmigos = new JScrollPane(listaAmigos);
        panelAmigos.add(scrollAmigos, BorderLayout.CENTER);
        
        JPanel panelBotones = new JPanel(new FlowLayout());
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());
        panelBotones.add(btnCerrar);
        
        panelPrincipal.add(panelBuscar, BorderLayout.NORTH);
        panelPrincipal.add(panelAmigos, BorderLayout.CENTER);
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);
        
        add(panelPrincipal);
    }
    
    private void buscarUsuario() {
        String username = txtBuscarUsuario.getText().trim();
        if (username.isEmpty()) {
            return;
        }
        
        try {
            Peticion p = new Peticion("BUSCAR_USUARIO", username);
            Cliente.getInstance().enviar(p);
            Peticion respuesta = Cliente.getInstance().recibir();
            
            if (respuesta.getAccion().equals("USUARIO_ENCONTRADO")) {
                Usuario encontrado = (Usuario) respuesta.getDatos();
                int opcion = JOptionPane.showConfirmDialog(this,
                    "Enviar solicitud de amistad a " + encontrado.getNombre() + "?",
                    "Enviar Solicitud",
                    JOptionPane.YES_NO_OPTION);
                
                if (opcion == JOptionPane.YES_OPTION) {
                    enviarInvitacion(encontrado.getPk_usuario());
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "Usuario no encontrado.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error: " + ex.getMessage());
        }
    }
    
    private void enviarInvitacion(int usuarioId) {
        try {
            Amigo invitacion = new Amigo(usuarioActualId, usuarioId, 0);
            Peticion p = new Peticion("ENVIAR_INVITACION_AMIGO", invitacion);
            Cliente.getInstance().enviar(p);
            Peticion respuesta = Cliente.getInstance().recibir();
            
            if (respuesta.getAccion().equals("INVITACION_ENVIADA")) {
                JOptionPane.showMessageDialog(this,
                    "Invitación enviada exitosamente.");
            } else {
                JOptionPane.showMessageDialog(this,
                    "Error: " + respuesta.getDatos());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error: " + ex.getMessage());
        }
    }
    
    private void cargarAmigos() {
        try {
            Peticion p = new Peticion("OBTENER_AMIGOS", usuarioActualId);
            Cliente.getInstance().enviar(p);
            Peticion respuesta = Cliente.getInstance().recibir();
            
            if (respuesta.getAccion().equals("AMIGOS_OBTENIDOS")) {
                @SuppressWarnings("unchecked")
                List<Amigo> amigos = (List<Amigo>) respuesta.getDatos();
                actualizarListaAmigos(amigos);
            }
        } catch (Exception ex) {
        }
    }
    
    private void actualizarListaAmigos(List<Amigo> amigos) {
        modeloAmigos.clear();
        for (Amigo amigo : amigos) {
            if (amigo.getEstado() == 1) {
                String nombre = amigo.getFk_usuario1() == usuarioActualId ?
                    amigo.getNombre_usuario2() : amigo.getNombre_usuario1();
                modeloAmigos.addElement(nombre);
            }
        }
    }
}

