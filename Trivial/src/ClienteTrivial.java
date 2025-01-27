import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

class ClienteTrivial {
    private JFrame frame;
    private JTextArea preguntaArea;
    private JButton[] opcionesBotones;
    private JLabel etiquetaPuntuacion;
    private JPanel panelQuesitos;
    private Set<String> quesitos;
    private int puntuacion;
    private Socket socket;
    private BufferedReader entrada;
    private PrintWriter salida;

    public static void main(String[] args) {
        new ClienteTrivial("localhost", 5000);
    }

    public ClienteTrivial(String servidor, int puerto) {
        try {
            socket = new Socket(servidor, puerto);
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            salida = new PrintWriter(socket.getOutputStream(), true);
            quesitos = new HashSet<>();
            puntuacion = 0;
            crearInterfaz();
            new Thread(this::escucharMensajes).start();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error al conectar con el servidor: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void crearInterfaz() {
        frame = new JFrame("Trivial Cliente");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new BorderLayout());

        // Panel para mostrar la pregunta
        JPanel panelPregunta = new JPanel();
        panelPregunta.setLayout(new BorderLayout());

        JLabel preguntaTitulo = new JLabel("Pregunta Actual:", SwingConstants.CENTER);
        panelPregunta.add(preguntaTitulo, BorderLayout.NORTH);

        preguntaArea = new JTextArea(4, 40);
        preguntaArea.setEditable(false);
        preguntaArea.setLineWrap(true);
        preguntaArea.setWrapStyleWord(true);
        panelPregunta.add(new JScrollPane(preguntaArea), BorderLayout.CENTER);

        frame.add(panelPregunta, BorderLayout.NORTH);

        // Panel para los botones de respuesta
        JPanel panelOpciones = new JPanel(new GridLayout(2, 2, 5, 5));
        opcionesBotones = new JButton[4];

        for (int i = 0; i < 4; i++) {
            opcionesBotones[i] = new JButton("Opción " + (i + 1));
            opcionesBotones[i].setEnabled(false);  // Deshabilitar hasta recibir una pregunta
            opcionesBotones[i].addActionListener(new OpcionSeleccionada(i + 1, this));
            panelOpciones.add(opcionesBotones[i]);
        }
        frame.add(panelOpciones, BorderLayout.CENTER);

        // Panel de puntuación
        JPanel panelInferior = new JPanel(new BorderLayout());
        etiquetaPuntuacion = new JLabel("Puntuación: 0", SwingConstants.CENTER);
        panelInferior.add(etiquetaPuntuacion, BorderLayout.NORTH);

        panelQuesitos = new JPanel();
        panelInferior.add(panelQuesitos, BorderLayout.SOUTH);

        frame.add(panelInferior, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    public void enviarRespuesta(int respuesta) {
        salida.println(respuesta);
    }

    private void escucharMensajes() {
        try {
            String mensaje;
            while ((mensaje = entrada.readLine()) != null) {
                if (socket.isClosed()) {
                    System.out.println("El socket está cerrado. Saliendo de escucharMensajes().");
                    break;
                }


                final String msg = mensaje;
                try {
                    if (msg.startsWith("CATEGORIA:")) {
                        //Separa la pregunta usando ||
                        String[] partes = msg.split("\\|\\|");

                        if (partes.length >= 6) {
                            SwingUtilities.invokeAndWait(() -> {
                                preguntaArea.setText(partes[0] + "\n" + partes[1]);

                                //Asignar opciones a los botones
                                for (int i = 0; i < 4; i++) {
                                    opcionesBotones[i].setText(partes[i + 2]);
                                    opcionesBotones[i].setEnabled(true);
                                }
                            });
                        } else {
                            System.err.println("Error: No se recibieron suficientes datos para la pregunta");
                            System.err.println("Datos recibidos: " + msg);
                            SwingUtilities.invokeAndWait(() -> preguntaArea.setText("Error al recibir la pregunta"));
                        }
                    } else if (msg.startsWith("Correcto! Puntuación:")) {
                        SwingUtilities.invokeAndWait(() -> {
                            JOptionPane.showMessageDialog(frame, msg);
                            actualizarPuntuacion(msg);
                        });
                    } else if (msg.startsWith("Incorrecto!")) {
                        SwingUtilities.invokeAndWait(() -> JOptionPane.showMessageDialog(frame, msg));
                    } else if (msg.startsWith("Puntuación:")) {
                        SwingUtilities.invokeAndWait(() -> actualizarPuntuacion(msg));
                    } else if (msg.startsWith("¡Felicidades!") || msg.startsWith("Juego terminado")) {
                        SwingUtilities.invokeAndWait(() -> {
                            JOptionPane.showMessageDialog(frame, msg);

                            //Para mostrar opción de jugar otra partida
                            int opcion = JOptionPane.showOptionDialog(
                                    frame,
                                    "¿Quieres jugar otra partida?",
                                    "Fin del juego",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.QUESTION_MESSAGE,
                                    null,
                                    new String[]{"Jugar otra vez", "Salir"},
                                    "Jugar otra vez"
                            );

                            if (opcion == JOptionPane.YES_OPTION) {
                                reiniciarJuego();
                            } else {
                                cerrarCliente();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.out.println("Error en la comunicación con el servidor o el socket se cerró: " + e.getMessage());
        }
    }

    private void actualizarPuntuacion(String msg) {
        try {
            String[] partes = msg.split(": ");
            if (partes.length > 1) {
                puntuacion = Integer.parseInt(partes[1].trim());
                etiquetaPuntuacion.setText("Puntuación: " + puntuacion);
            }
        } catch (NumberFormatException e) {
            System.err.println("Error al convertir la puntuación: " + msg);
        }
    }




    private void reiniciarJuego() {
        try {
            System.out.println("Reiniciando el juego...");

            if (!socket.isClosed()) {
                socket.close();
            }

            frame.dispose();

            puntuacion = 0;
            quesitos.clear();

            Thread.sleep(1000);

            //Crea una nueva conexión con el servidor
            new ClienteTrivial("localhost", 5000);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void cerrarCliente() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        frame.dispose();
        System.exit(0);
    }
}