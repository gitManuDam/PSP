import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorGUI {
    private final JFrame frame;
    private final JTextArea textArea;
    private static final int PUERTO = 6000;
    private static final int TIMEOUT=300000;

    public ServidorGUI() {
        //Ventana Principal
        frame = new JFrame("Servidor - Chat");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);

        //Area de texto para mostrar logs
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Courier New", Font.PLAIN, 14));
        textArea.setBackground(new Color(33, 33, 33));
        textArea.setForeground(new Color(139, 195, 74));
        JScrollPane scrollPane = new JScrollPane(textArea);

        //Añadir el área de texto al marco
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

        frame.setVisible(true);

        iniciarServidor();
    }

    private void iniciarServidor() {
        new Thread(() -> {
            try (ServerSocket servidor = new ServerSocket(PUERTO)) {
                agregarLog("Servidor escuchando en el puerto " + PUERTO);
                    servidor.setSoTimeout(TIMEOUT);
                while (true) {
                    Socket cliente = servidor.accept();
                    agregarLog("Cliente conectado: " + cliente.getInetAddress());
                    new Thread(new ManejadorCliente(cliente, this)).start();
                }
            } catch (IOException e) {
                agregarLog("Error en el servidor: " + e.getMessage());
            }
        }).start();
    }

    public void agregarLog(String mensaje) {
        SwingUtilities.invokeLater(() -> textArea.append(mensaje + "\n"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ServidorGUI::new);
    }
}
