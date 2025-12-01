package chatcliente;
import common.Peticion;
import javax.swing.*;
import java.awt.*;
public class CrearGrupoUI extends JFrame {
    private JTextField txtTitulo;
    private JTextField txtInvitados;
    private JButton btnCrear;
    public CrearGrupoUI() {
        super("Crear Nuevo Grupo");
        configurarVentana();
        inicializarComponentes();
    }
    private void configurarVentana() {
        setSize(350, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(3, 1, 10, 10));
    }
    private void inicializarComponentes() {
        JPanel pnlTitulo = new JPanel(new BorderLayout());
        pnlTitulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        pnlTitulo.add(new JLabel("Título del Grupo:"), BorderLayout.NORTH);
        txtTitulo = new JTextField();
        pnlTitulo.add(txtTitulo, BorderLayout.CENTER);
        add(pnlTitulo);
        JPanel pnlInv = new JPanel(new BorderLayout());
        pnlInv.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        pnlInv.add(new JLabel("Invitar usuarios (separar por comas):"), BorderLayout.NORTH);
        txtInvitados = new JTextField();
        pnlInv.add(txtInvitados, BorderLayout.CENTER);
        add(pnlInv);
        JPanel pnlBtn = new JPanel(new FlowLayout());
        btnCrear = new JButton("Crear Grupo");
        pnlBtn.add(btnCrear);
        add(pnlBtn);
        btnCrear.addActionListener(e -> accionCrear());
    }
    private void accionCrear() {
        String titulo = txtTitulo.getText().trim();
        String invitados = txtInvitados.getText().trim();
        if (titulo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El grupo debe tener un título.");
            return;
        }
        String payload = titulo + ":" + invitados;
        try {
            Cliente.getInstance().enviar(new Peticion("CREAR_GRUPO", payload));
            this.dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}