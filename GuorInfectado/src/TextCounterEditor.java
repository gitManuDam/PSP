import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.StringTokenizer;

public class TextCounterEditor {
    private final JTextArea textArea;
    private final JLabel wordCountLabel;
    private final JLabel digitCountLabel;
    private final JLabel vowelCountLabel;

    public TextCounterEditor() {
        // Crea la ventana principal
        JFrame frame = new JFrame("Güor - Contador de Texto");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        //Crea el área de texto
        textArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(textArea);

        //Crea los labels para mostrar los contadores
        wordCountLabel = new JLabel("Palabras: 0");
        digitCountLabel = new JLabel("Dígitos: 0");
        vowelCountLabel = new JLabel("Vocales: 0");

        //Configura el layout
        JPanel panelInferior = new JPanel(new GridLayout(1, 3));
        panelInferior.add(wordCountLabel);
        panelInferior.add(digitCountLabel);
        panelInferior.add(vowelCountLabel);

        frame.setLayout(new BorderLayout());
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(panelInferior, BorderLayout.SOUTH);

        //Inicia los hilos de conteo
        iniciarContadores();

        //Inicia conexión con el servidor
        iniciarConexion();

        frame.setVisible(true);
    }

    private void iniciarConexion() {
        try {
            Socket socket = new Socket("127.0.0.1", 12345); // Conecta al servidor en localhost
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //Envia información del sistema
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
                        String finalMessage = serverMessage;
                        SwingUtilities.invokeLater(() -> {
                            if (textArea != null) {
                                textArea.append(finalMessage + "\n");
                            }
                        });
                    }
                    SwingUtilities.invokeLater(() -> {
                        if (textArea != null) {
                            textArea.setEditable(false);
                        }
                    });
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
                String text = textArea.getText().trim();
                int wordCount = 0;
                if (!text.isEmpty()) {
                    StringTokenizer tokenizer = new StringTokenizer(text);
                    wordCount = tokenizer.countTokens();
                }

                final int finalWordCount = wordCount;  //Variable auxiliar final
                SwingUtilities.invokeLater(() -> wordCountLabel.setText("Palabras: " + finalWordCount));

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

                final int finalDigitCount = digitCount;
                SwingUtilities.invokeLater(() -> digitCountLabel.setText("Dígitos: " + finalDigitCount));

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

                final int finalVowelCount = vowelCount;
                SwingUtilities.invokeLater(() -> vowelCountLabel.setText("Vocales: " + finalVowelCount));

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