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
        panelContenedor.setBorder(BorderFactory.createEmptyBorder(0, esMio ? 50 : 0, 0, esMio ? 0 : 50));
        
        JPanel panelHorizontal = new JPanel(new FlowLayout(esMio ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        panelHorizontal.setOpaque(false);
        
        if (!esMio) {
            JPanel avatar = new JPanel();
            avatar.setPreferredSize(new Dimension(35, 35));
            avatar.setMinimumSize(new Dimension(35, 35));
            avatar.setMaximumSize(new Dimension(35, 35));
            avatar.setOpaque(true);
            avatar.setBackground(new Color(37, 211, 102));
            avatar.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
            panelHorizontal.add(avatar);
            panelHorizontal.add(Box.createHorizontalStrut(8));
        }
        
        JPanel panelBurbuja = new JPanel();
        panelBurbuja.setLayout(new BorderLayout(8, 4));
        panelBurbuja.setOpaque(true);
        
        Color colorFondo = esMio ? new Color(220, 248, 198) : Color.WHITE;
        panelBurbuja.setBackground(colorFondo);
        panelBurbuja.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200, 50), 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        int maxWidth = 350;
        
        if (esGrupo && !esMio) {
            JLabel labelNombre = new JLabel(nombre);
            labelNombre.setFont(new Font("Arial", Font.BOLD, 12));
            labelNombre.setForeground(new Color(37, 211, 102));
            labelNombre.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
            panelBurbuja.add(labelNombre, BorderLayout.NORTH);
        }
        
        JPanel panelTextoHora = new JPanel(new BorderLayout(8, 0));
        panelTextoHora.setOpaque(false);
        
        JLabel texto = new JLabel("<html><body style='width: " + (maxWidth - 80) + "px;'>" + 
            escapeHtml(contenido) + "</body></html>");
        texto.setFont(new Font("Arial", Font.PLAIN, 14));
        texto.setForeground(new Color(17, 27, 33));
        texto.setVerticalAlignment(SwingConstants.TOP);
        panelTextoHora.add(texto, BorderLayout.CENTER);
        
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String timestamp = sdf.format(new Date());
        JLabel labelHora = new JLabel(timestamp);
        labelHora.setFont(new Font("Arial", Font.PLAIN, 11));
        labelHora.setForeground(new Color(102, 119, 129));
        labelHora.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        panelTextoHora.add(labelHora, BorderLayout.EAST);
        
        panelBurbuja.add(panelTextoHora, BorderLayout.CENTER);
        panelBurbuja.setMaximumSize(new Dimension(maxWidth, Integer.MAX_VALUE));
        
        panelHorizontal.add(panelBurbuja);
        panelContenedor.add(panelHorizontal, BorderLayout.CENTER);
        
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

