package ui.chat;

import chatcliente.Cliente;
import common.Peticion;
import models.Mensaje;
import models.Usuario;
import ui.chat.components.EnvioPanel;
import ui.chat.components.MensajesPanel;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class VentanaChat extends JFrame {
    
    private Usuario usuarioActual;
    private Usuario destinatario;
    private int grupoId;
    private boolean esGrupo;
    private MensajesPanel mensajesPanel;
    private EnvioPanel envioPanel;
    private ReceptorMensajes receptor;
    private boolean activo;
    
    public VentanaChat(Usuario usuarioActual, Usuario destinatario) {
        super("chat con " + destinatario.getNombre());
        this.usuarioActual = usuarioActual;
        this.destinatario = destinatario;
        this.esGrupo = false;
        this.activo = true;
        
        configurarVentana();
        inicializarComponentes();
        iniciarReceptorMensajes();
        cargarHistorial();
    }
    
    public VentanaChat(Usuario usuarioActual, int grupoId, String tituloGrupo) {
        super("grupo: " + tituloGrupo);
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
        
        mensajesPanel = new MensajesPanel();
        mensajesPanel.setUsuarioActualId(usuarioActual.getPk_usuario());
        envioPanel = new EnvioPanel(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enviarMensaje();
            }
        });
        
        panelPrincipal.add(mensajesPanel, BorderLayout.CENTER);
        panelPrincipal.add(envioPanel, BorderLayout.SOUTH);
        
        add(panelPrincipal, BorderLayout.CENTER);
        
        String mensajeBienvenida = esGrupo ? 
            "bienvenido al grupo!" : 
            "conversacion con " + destinatario.getNombre();
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
            // ignorar errores
        }
    }
    
    private void mostrarHistorial(List<Mensaje> mensajes) {
        for (Mensaje msg : mensajes) {
            String nombreRemitente = msg.getFk_remitente() == usuarioActual.getPk_usuario() ? 
                usuarioActual.getNombre() : 
                (destinatario != null ? destinatario.getNombre() : "otro");
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
            // ignorar errores en modo prueba
        }
    }
    
    private void iniciarReceptorMensajes() {
        if (Cliente.getInstance().estaConectado()) {
            receptor = new ReceptorMensajes(null, null);
            receptor.start();
        }
    }
    
    public void recibirMensaje(Mensaje mensaje) {
        String nombreRemitente = mensaje.getFk_remitente() == usuarioActual.getPk_usuario() ? 
            usuarioActual.getNombre() : 
            (destinatario != null ? destinatario.getNombre() : "otro");
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
        String nombre = esGrupo ? "usuario grupo" : 
            (destinatario != null ? destinatario.getNombre() : "contacto ejemplo");
        int remitenteId = destinatario != null ? destinatario.getPk_usuario() : 2;
        mensajesPanel.agregarMensaje(mensaje, nombre, remitenteId, esGrupo);
    }
}

