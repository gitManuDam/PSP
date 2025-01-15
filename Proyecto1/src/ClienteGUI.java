import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class ClienteGUI {
    private JFrame frame;
    private JTextArea textArea;
    private JTextField textField;
    private JButton sendButton;

    private PrintWriter fsalida;
    private BufferedReader fentrada;
    private Socket socket;
    private Timer inactivityTimer;

    public ClienteGUI() {
        // Crear la ventana principal
        frame = new JFrame("Cliente - Chat");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);

        // Configurar el área de texto para mostrar mensajes
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Arial", Font.PLAIN, 14));
        textArea.setBackground(new Color(240, 240, 240));
        textArea.setForeground(new Color(33, 33, 33));
        JScrollPane scrollPane = new JScrollPane(textArea);

        // Configurar el campo de entrada
        textField = new JTextField();
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setBackground(new Color(255, 255, 255));
        textField.setForeground(new Color(33, 33, 33));
        textField.addActionListener(e -> enviarMensaje()); // Permitir enviar con Enter

        // Configurar el botón de enviar
        sendButton = new JButton("Enviar");
        sendButton.setBackground(new Color(76, 175, 80));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFont(new Font("Arial", Font.BOLD, 14));
        sendButton.addActionListener(e -> enviarMensaje());

        // Crear el panel inferior con el campo de texto y botón
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(textField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        // Añadir componentes al marco
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.getContentPane().add(inputPanel, BorderLayout.SOUTH);

        frame.setVisible(true);

        conectarAlServidor();
        iniciarTemporizador();
    }

    private void conectarAlServidor() {
        try {
            socket = new Socket("localhost", 6000);
            fsalida = new PrintWriter(socket.getOutputStream(), true);
            fentrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Hilo para recibir mensajes
            new Thread(() -> {
                try {
                    String mensaje;
                    while ((mensaje = fentrada.readLine()) != null) {
                        if ("__SERVIDOR_CERRADO__".equals(mensaje)) {
                            manejarDesconexion();
                            break;
                        }
                        textArea.append("Servidor: " + mensaje + "\n");
                    }
                } catch (IOException e) {
                    manejarDesconexion();
                }
            }).start();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error conectando al servidor: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            deshabilitarEntrada();
        }
    }

    private void enviarMensaje() {
        if (socket == null || socket.isClosed()) {
            manejarDesconexion();
            return;
        }

        String mensaje = textField.getText();
        if (!mensaje.isEmpty()) {
            fsalida.println(mensaje);
            textArea.append("Tú: " + mensaje + "\n");
            textField.setText("");

            resetTemporizador(); // Reinicia el temporizador de inactividad

            if ("salir".equalsIgnoreCase(mensaje)) {
                System.exit(0);
            }
        }
    }

    private void manejarDesconexion() {
        textArea.append("El servidor se ha cerrado. Cerrando cliente...\n");
        deshabilitarEntrada();
        new Timer(3000, e -> System.exit(0)).start(); // Cierra el cliente después de 3 segundos
    }

    private void deshabilitarEntrada() {
        textField.setEnabled(false);
        sendButton.setEnabled(false);
    }

    private void iniciarTemporizador() {
        inactivityTimer = new Timer(30000, e -> {
            JOptionPane.showMessageDialog(frame, "No se envió ningún mensaje en 30 segundos. Cerrando el cliente...");
            System.exit(0); // Cierra la aplicación
        });
        inactivityTimer.setRepeats(false);
        inactivityTimer.start();
    }

    private void resetTemporizador() {
        if (inactivityTimer != null) {
            inactivityTimer.restart();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClienteGUI::new);
    }
}