import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.Timer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServidorGUI {
    private final JTextArea textArea;
    private final JTextField textField;
    private final JLabel clockLabel;
    private static final int PUERTO = 6000;
    private ServerSocket servidor;
    private Socket cliente;
    private PrintWriter fsalida;
    private Timer timeoutTimer;

    public ServidorGUI() {
        // Crear la ventana principal
        JFrame frame = new JFrame("Servidor - Chat");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);

        // Configurar el área de texto para mostrar logs
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Courier New", Font.PLAIN, 14));
        textArea.setBackground(new Color(33, 33, 33));
        textArea.setForeground(new Color(139, 195, 74));
        JScrollPane scrollPane = new JScrollPane(textArea);

        // Configurar el campo de texto para enviar mensajes
        textField = new JTextField();
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.addActionListener(e -> enviarMensaje());

        // Configurar el botón de enviar
        JButton sendButton = new JButton("Enviar");
        sendButton.setFont(new Font("Arial", Font.BOLD, 14));
        sendButton.setBackground(new Color(76, 175, 80));
        sendButton.setForeground(Color.WHITE);
        sendButton.addActionListener(e -> enviarMensaje());

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(textField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        // Configurar el botón de cerrar
        JButton closeButton = new JButton("Cerrar Servidor");
        closeButton.setFont(new Font("Arial", Font.BOLD, 14));
        closeButton.setBackground(new Color(244, 67, 54));
        closeButton.setForeground(Color.WHITE);
        closeButton.addActionListener(e -> cerrarServidor());

        // Configurar el reloj
        clockLabel = new JLabel();
        clockLabel.setFont(new Font("Arial", Font.BOLD, 14));
        clockLabel.setForeground(new Color(255, 255, 255));
        actualizarReloj();

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(33, 33, 33));
        topPanel.add(clockLabel, BorderLayout.EAST);
        topPanel.add(closeButton, BorderLayout.WEST);
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10)); // Añadir padding

        // Añadir componentes al marco
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.getContentPane().add(inputPanel, BorderLayout.SOUTH);
        frame.getContentPane().add(topPanel, BorderLayout.NORTH);

        frame.setVisible(true);

        iniciarServidor();
    }

    private void iniciarServidor() {
        new Thread(() -> {
            try {
                servidor = new ServerSocket(PUERTO);
                agregarLog("Servidor escuchando en el puerto :" + PUERTO);
                cliente = servidor.accept();
                agregarLog("Cliente conectado: " + cliente.getInetAddress());
                fsalida = new PrintWriter(cliente.getOutputStream(), true);

                BufferedReader fentrada = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
                iniciarTimeout(); // Inicia el temporizador de inactividad
                String mensaje;
                while ((mensaje = fentrada.readLine()) != null) {
                    agregarLog("Cliente: " + mensaje);
                    resetTimeout(); // Reinicia el temporizador de inactividad
                    if ("salir".equalsIgnoreCase(mensaje)) {
                        agregarLog("El cliente ha finalizado la conexión.");
                        break;
                    }
                }
            } catch (IOException e) {
                agregarLog("Cerrando servidor. Tiempo máximo de espera del cliente alcanzado.");
            } finally {
                cerrarServidor();
            }
        }).start();
    }

    private void enviarMensaje() {
        String mensaje = textField.getText();
        if (!mensaje.isEmpty() && fsalida != null) {
            fsalida.println(mensaje);
            agregarLog("Tú:  " + mensaje);
            textField.setText("");
        }
    }

    private void cerrarServidor() {
        try {
            if (servidor != null && !servidor.isClosed()) {
                servidor.close();
                agregarLog("Servidor cerrado manualmente.");
            }
            if (cliente != null && !cliente.isClosed()) {
                cliente.close();
            }
            new javax.swing.Timer(5000, e -> System.exit(0)).start(); // Cierra la aplicación después de 3 segundos
        } catch (IOException e) {
            agregarLog("Error al cerrar el servidor:  " + e.getMessage());
        }
    }

    private void iniciarTimeout() {
        timeoutTimer = new Timer(30000, e -> {
            agregarLog("No se recibió ningún mensaje en 30 segundos. Cerrando el servidor....");
            cerrarServidor();
        });
        timeoutTimer.setRepeats(false);
        timeoutTimer.start();
    }

    private void resetTimeout() {
        if (timeoutTimer != null) {
            timeoutTimer.restart(); // Reinicia el temporizador.
        }
    }

    private void actualizarReloj() {
        Timer relojTimer = new Timer(1000, e -> {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            clockLabel.setText("Hora actual:  " + sdf.format(new Date()));
        });
        relojTimer.start();
    }

    public void agregarLog(String mensaje) {
        SwingUtilities.invokeLater(() -> textArea.append(mensaje + "\n"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ServidorGUI::new);
    }
}