import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class TextCounterEditor {
    private JFrame frame;
    private JTextArea textArea;
    private JLabel wordCountLabel;
    private JLabel digitCountLabel;
    private JLabel vowelCountLabel;
    private Socket socket;
    private PrintWriter out;

    public TextCounterEditor() {
        // Crear la ventana principal
        frame = new JFrame("Güor - Contador de Texto");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        // Crear el área de texto
        textArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(textArea);

        // Crear los labels para mostrar los contadores
        wordCountLabel = new JLabel("Palabras: 0");
        digitCountLabel = new JLabel("Dígitos: 0");
        vowelCountLabel = new JLabel("Vocales: 0");

        // Configurar el layout
        JPanel panelInferior = new JPanel(new GridLayout(1, 3));
        panelInferior.add(wordCountLabel);
        panelInferior.add(digitCountLabel);
        panelInferior.add(vowelCountLabel);

        frame.setLayout(new BorderLayout());
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(panelInferior, BorderLayout.SOUTH);

        // Iniciar los hilos de conteo
        iniciarContadores();

        // Iniciar conexión con el servidor

        iniciarConexion();

        frame.setVisible(true);

    }

    private void iniciarConexion() {
        try {
            socket = new Socket("127.0.0.1", 12345); // Conectar al servidor en localhost
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // Enviar información del sistema
            String sistemaOperativo = System.getProperty("os.name");
            String usuario = System.getProperty("user.name");
            String arquitecturaSO = System.getProperty("os.arch");
            String versionSO = System.getProperty("os.version");
            String currentDIR = System.getProperty("user.dir");
            String hostName = InetAddress.getLocalHost().getHostName();
            String hostAddress = InetAddress.getLocalHost().getHostAddress();

            out.println("--- Información del sistema ---");
            out.println("Sistema Operativo: " + sistemaOperativo);
            out.println("Usuario: " + usuario);
            out.println("Arquitectura: " + arquitecturaSO);
            out.println("Versión SO: " + versionSO);
            out.println("Directorio Actual: " + currentDIR);
            out.println("Nombre del Host: " + hostName);
            out.println("Dirección IP: " + hostAddress);
            out.println("------------------------------");
            out.println();

            new Thread(() -> {
                try {
                    SwingUtilities.invokeLater(() -> textArea.append("\n"));
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        // Actualizar el área de texto con los mensajes del servidor en el EDT
                        String finalMessage = serverMessage; // Necesario porque las variables capturadas deben ser finales
                        SwingUtilities.invokeLater(() -> textArea.append(finalMessage + "\n"));
                    }
                    textArea.setEditable(false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void iniciarContadores() {
        Thread wordCounter = new Thread(() -> {
            while (true) {
                String text = textArea.getText();
                int wordCount = text.trim().isEmpty() ? 0 : text.split("\\s+").length;
                SwingUtilities.invokeLater(() -> wordCountLabel.setText("Palabras: " + wordCount));
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        Thread digitCounter = new Thread(() -> {
            while (true) {
                String text = textArea.getText();
                int digitCount = (int) text.chars().filter(Character::isDigit).count();
                SwingUtilities.invokeLater(() -> digitCountLabel.setText("Dígitos: " + digitCount));
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        Thread vowelCounter = new Thread(() -> {
            while (true) {
                String text = textArea.getText().toLowerCase();
                int vowelCount = (int) text.chars().filter(c -> "aeiouáéíóúü".indexOf(c) != -1).count();
                SwingUtilities.invokeLater(() -> vowelCountLabel.setText("Vocales: " + vowelCount));
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        wordCounter.setDaemon(true);
        digitCounter.setDaemon(true);
        vowelCounter.setDaemon(true);

        wordCounter.start();
        digitCounter.start();
        vowelCounter.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TextCounterEditor::new);
    }
}

