package ui.conversacion.componentes;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class panel_mensajes extends JPanel {
    
    private JPanel panelMensajes;
    private int usuarioActualId;
    
    public panel_mensajes() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(new Color(240, 240, 240));
        
        panelMensajes = new JPanel();
        panelMensajes.setLayout(new BoxLayout(panelMensajes, BoxLayout.Y_AXIS));
        panelMensajes.setBackground(new Color(240, 240, 240));
        
        JScrollPane scrollMensajes = new JScrollPane(panelMensajes);
        scrollMensajes.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollMensajes.setBorder(null);
        scrollMensajes.getViewport().setBackground(new Color(240, 240, 240));
        scrollMensajes.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        add(scrollMensajes, BorderLayout.CENTER);
    }
    
    public void setUsuarioActualId(int id) {
        this.usuarioActualId = id;
    }
    
    public void agregarMensaje(String mensaje, String nombreRemitente, int remitenteId, boolean esGrupo) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            boolean esMio = remitenteId == usuarioActualId;
            JPanel panelMensaje = crearPanelMensaje(mensaje, nombreRemitente, esMio, esGrupo);
            panelMensajes.add(panelMensaje);
            panelMensajes.add(Box.createVerticalStrut(4));
            panelMensajes.revalidate();
            panelMensajes.repaint();
            
            scrollToBottom();
        });
    }
    
    public void agregarMensaje(String mensaje) {
        agregarMensaje(mensaje, "sistema", -1, false);
    }
    
    public void agregarMensajeSistema(String mensaje) {
        JLabel labelSistema = new JLabel("[Sistema] " + mensaje);
        labelSistema.setForeground(new Color(100, 100, 100));
        labelSistema.setFont(new Font("Arial", Font.ITALIC, 11));
        labelSistema.setAlignmentX(Component.CENTER_ALIGNMENT);
        labelSistema.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            panelMensajes.add(labelSistema);
            panelMensajes.add(Box.createVerticalStrut(4));
            panelMensajes.revalidate();
            panelMensajes.repaint();
        });
    }
    
    private JPanel crearPanelMensaje(String contenido, String nombre, boolean esMio, boolean esGrupo) {
        JPanel panelContenedor = new JPanel(new BorderLayout());
        panelContenedor.setOpaque(false);
        panelContenedor.setBorder(BorderFactory.createEmptyBorder(2, esMio ? 50 : 0, 2, esMio ? 0 : 50));
        
        JPanel panelMensaje = new JPanel();
        panelMensaje.setLayout(new BorderLayout(5, 5));
        panelMensaje.setOpaque(true);
        
        // Usar colores estándar de Swing
        Color colorFondo = esMio ? 
            UIManager.getColor("Panel.background").darker() : 
            UIManager.getColor("Panel.background");
        panelMensaje.setBackground(colorFondo);
        
        // Usar borde estándar de Swing
        panelMensaje.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEtchedBorder(),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        
        int maxWidth = 350;
        panelMensaje.setMaximumSize(new Dimension(maxWidth, Integer.MAX_VALUE));
        
        if (esGrupo && !esMio) {
            JLabel labelNombre = new JLabel(nombre);
            labelNombre.setFont(UIManager.getFont("Label.font"));
            labelNombre.setForeground(UIManager.getColor("Label.foreground"));
            panelMensaje.add(labelNombre, BorderLayout.NORTH);
        }
        
        JPanel panelTextoHora = new JPanel(new BorderLayout(5, 0));
        panelTextoHora.setOpaque(false);
        
        JLabel texto = new JLabel("<html><body style='width: " + (maxWidth - 80) + "px;'>" + 
            escapeHtml(contenido) + "</body></html>");
        texto.setFont(UIManager.getFont("Label.font"));
        texto.setForeground(UIManager.getColor("Label.foreground"));
        texto.setVerticalAlignment(SwingConstants.TOP);
        panelTextoHora.add(texto, BorderLayout.CENTER);
        
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String timestamp = sdf.format(new Date());
        JLabel labelHora = new JLabel(timestamp);
        labelHora.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 10f));
        labelHora.setForeground(UIManager.getColor("Label.disabledForeground"));
        panelTextoHora.add(labelHora, BorderLayout.EAST);
        
        panelMensaje.add(panelTextoHora, BorderLayout.CENTER);
        
        panelContenedor.add(panelMensaje, esMio ? BorderLayout.EAST : BorderLayout.WEST);
        
        return panelContenedor;
    }
    
    private String escapeHtml(String texto) {
        return texto.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;")
                   .replace("\n", "<br>");
    }
    
    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollPane scrollPane = (JScrollPane) getComponent(0);
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }
    
    public void limpiar() {
        panelMensajes.removeAll();
        panelMensajes.revalidate();
        panelMensajes.repaint();
    }
}

