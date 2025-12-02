package ui.conversacion;

import chatcliente.Cliente;
import common.Peticion;
import models.Mensaje;
import models.Usuario;
import ui.conversacion.componentes.panel_envio;
import ui.conversacion.componentes.panel_mensajes;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.List;

public class ventana_conversacion extends JFrame {
    
    private Usuario usuarioActual;
    private Usuario destinatario;
    private int grupoId;
    private boolean esGrupo;
    private panel_mensajes mensajesPanel;
    private panel_envio envioPanel;
    private receptor_mensajes receptor;
    private boolean activo;
    
    public ventana_conversacion(Usuario usuarioActual, Usuario destinatario) {
        super("Chat con " + destinatario.getNombre());
        this.usuarioActual = usuarioActual;
        this.destinatario = destinatario;
        this.esGrupo = false;
        this.activo = true;
        
        configurarVentana();
        inicializarComponentes();
        iniciarReceptorMensajes();
        cargarHistorial();
    }
    
    public ventana_conversacion(Usuario usuarioActual, int grupoId, String tituloGrupo) {
        super("Grupo: " + tituloGrupo);
        this.usuarioActual = usuarioActual;
        this.grupoId = grupoId;
        this.esGrupo = true;
        this.activo = true;
        
        configurarVentana();
        inicializarComponentes();
        iniciarReceptorMensajes();
        cargarHistorial();
    }
    
    private void configurarVentana() {
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                activo = false;
                if (receptor != null) {
                    receptor.detener();
                }
            }
        });
    }
    
    private void inicializarComponentes() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(5, 5));
        panelPrincipal.setBorder(new EmptyBorder(10, 10, 10, 10));
        panelPrincipal.setBackground(new Color(240, 240, 240));
        
        mensajesPanel = new panel_mensajes();
        mensajesPanel.setUsuarioActualId(usuarioActual.getPk_usuario());
        envioPanel = new panel_envio(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enviarMensaje();
            }
        });
        
        panelPrincipal.add(mensajesPanel, BorderLayout.CENTER);
        panelPrincipal.add(envioPanel, BorderLayout.SOUTH);
        
        add(panelPrincipal, BorderLayout.CENTER);
        
        String mensajeBienvenida = esGrupo ? 
            "Bienvenido al grupo!" : 
            "Conversación con " + destinatario.getNombre();
        mensajesPanel.agregarMensajeSistema(mensajeBienvenida);
    }
    
    private void cargarHistorial() {
        if (!Cliente.getInstance().estaConectado()) {
            return;
        }
        try {
            Peticion p;
            if (esGrupo) {
                p = new Peticion("OBTENER_HISTORIAL_GRUPO", 
                    new Object[] {usuarioActual.getPk_usuario(), grupoId});
            } else {
                p = new Peticion("OBTENER_HISTORIAL", 
                    new Object[] {usuarioActual.getPk_usuario(), destinatario.getPk_usuario()});
            }
            
            Cliente.getInstance().enviar(p);
            Peticion respuesta = Cliente.getInstance().recibir();
            
            if (respuesta.getAccion().equals("HISTORIAL_OBTENIDO") || 
                respuesta.getAccion().equals("HISTORIAL_GRUPO_OBTENIDO")) {
                @SuppressWarnings("unchecked")
                List<Mensaje> mensajes = (List<Mensaje>) respuesta.getDatos();
                mostrarHistorial(mensajes);
            }
        } catch (Exception ex) {
        }
    }
    
    private void mostrarHistorial(List<Mensaje> mensajes) {
        for (Mensaje msg : mensajes) {
            String nombreRemitente = msg.getFk_remitente() == usuarioActual.getPk_usuario() ? 
                usuarioActual.getNombre() : 
                (destinatario != null ? destinatario.getNombre() : "Otro");
            mensajesPanel.agregarMensaje(
                msg.getContenido(), 
                nombreRemitente, 
                msg.getFk_remitente(), 
                esGrupo);
        }
    }
    
    private void enviarMensaje() {
        String texto = envioPanel.getTexto();
        if (texto.isEmpty()) {
            return;
        }
        
        mensajesPanel.agregarMensaje(
            texto, 
            usuarioActual.getNombre(), 
            usuarioActual.getPk_usuario(), 
            esGrupo);
        
        envioPanel.limpiar();
        envioPanel.focus();
        
        if (!Cliente.getInstance().estaConectado()) {
            return;
        }
        
        try {
            Mensaje mensaje;
            if (esGrupo) {
                mensaje = new Mensaje(usuarioActual.getPk_usuario(), grupoId, texto);
            } else {
                mensaje = new Mensaje(usuarioActual.getPk_usuario(), 
                    destinatario.getPk_usuario(), texto);
            }
            
            String accion = esGrupo ? "MENSAJE_GRUPO" : "MENSAJE_AMIGO";
            Peticion p = new Peticion(accion, mensaje);
            Cliente.getInstance().enviar(p);
            
        } catch (IOException ex) {
        }
    }
    
    private void iniciarReceptorMensajes() {
        if (Cliente.getInstance().estaConectado()) {
            receptor = new receptor_mensajes(null, null);
            receptor.start();
        }
    }
    
    public void recibirMensaje(Mensaje mensaje) {
        String nombreRemitente = mensaje.getFk_remitente() == usuarioActual.getPk_usuario() ? 
            usuarioActual.getNombre() : 
            (destinatario != null ? destinatario.getNombre() : "Otro");
        mensajesPanel.agregarMensaje(
            mensaje.getContenido(), 
            nombreRemitente, 
            mensaje.getFk_remitente(), 
            esGrupo);
    }
    
    public boolean esChatCon(int id, boolean esGrupo) {
        if (this.esGrupo != esGrupo) {
            return false;
        }
        if (esGrupo) {
            return this.grupoId == id;
        } else {
            return destinatario != null && destinatario.getPk_usuario() == id;
        }
    }
    
    public void recibirMensajeEjemplo(String mensaje) {
        String nombre = esGrupo ? "Usuario Grupo" : 
            (destinatario != null ? destinatario.getNombre() : "Contacto Ejemplo");
        int remitenteId = destinatario != null ? destinatario.getPk_usuario() : 2;
        mensajesPanel.agregarMensaje(mensaje, nombre, remitenteId, esGrupo);
    }
}

