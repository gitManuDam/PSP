import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class ClienteGUI {
    private final JFrame frame;
    private final JTextArea textArea;
    private final JTextField textField;
    private final JButton sendButton;

    private PrintWriter fsalida;
    private BufferedReader fentrada;

    public ClienteGUI() {
        //Ventana Principal
        frame = new JFrame("Cliente - Chat");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);

        //Area de texto para mostrar mensajes
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Arial" , Font.PLAIN, 14));
        textArea.setBackground(new Color(240, 240, 240));
        textArea.setForeground(new Color(33, 33, 33));
        JScrollPane scrollPane = new JScrollPane(textArea);

        //Campo de entrada
        textField = new JTextField();
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setBackground(new Color(255, 255, 255));
        textField.setForeground(new Color(33, 33, 33));


        //Botón de enviar
        sendButton = new JButton("Enviar");
        sendButton.setBackground(new Color(76, 175, 80));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFont(new Font("Arial" , Font.BOLD, 14));

        //Panel inferior con el campo de texto y botón
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(textField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);


        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.getContentPane().add(inputPanel, BorderLayout.SOUTH);

        frame.setVisible(true);

        conectarAlServidor();
        configurarBotonEnviar();
    }

    private void conectarAlServidor() {
        try {
            Socket socket = new Socket("localhost", 6000);
            fsalida = new PrintWriter(socket.getOutputStream(), true);
            fentrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Hilo para recibir mensajes
            new Thread(() -> {
                try {
                    String mensaje;
                    while ((mensaje = fentrada.readLine()) != null) {
                        textArea.append("Servidor: " + mensaje + "\n");
                    }
                } catch (IOException e) {
                    textArea.append("Error recibiendo mensajes:  " + e.getMessage() + "\n");
                }
            }).start();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error conectando al servidor: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void configurarBotonEnviar() {
        // Listener para el botón
        sendButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                enviarMensaje();
            }
        });

        // Listener para el campo de texto (Enter)
        textField.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                enviarMensaje();
            }
        });
    }

    private void enviarMensaje(){
        String mensaje = textField.getText();
        if (!mensaje.isEmpty()) {
            fsalida.println(mensaje);
            textArea.append("Tú: " + mensaje + "\n");
            textField.setText("");

            if ("salir".equalsIgnoreCase(mensaje)) {
                System.exit(0);
            }
        }
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(ClienteGUI::new);
    }
}
