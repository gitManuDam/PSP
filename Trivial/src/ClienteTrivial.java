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
            e.printStackTrace();
        }
    }

    private void crearInterfaz() {
        frame = new JFrame("Trivial Cliente");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        preguntaArea = new JTextArea();
        preguntaArea.setEditable(false);
        frame.add(preguntaArea, BorderLayout.NORTH);

        JPanel panelOpciones = new JPanel(new GridLayout(2, 2));
        opcionesBotones = new JButton[4];
        for (int i = 0; i < 4; i++) {
            opcionesBotones[i] = new JButton();
            opcionesBotones[i].addActionListener(new OpcionSeleccionada(i + 1, this));
            panelOpciones.add(opcionesBotones[i]);
        }
        frame.add(panelOpciones, BorderLayout.CENTER);

        etiquetaPuntuacion = new JLabel("Puntuación: 0");
        frame.add(etiquetaPuntuacion, BorderLayout.SOUTH);

        panelQuesitos = new JPanel();
        frame.add(panelQuesitos, BorderLayout.WEST);

        frame.setVisible(true);
    }

    public void enviarRespuesta(int respuesta) {
        salida.println(respuesta);
    }

    private void escucharMensajes() {
        try {
            String mensaje;
            while ((mensaje = entrada.readLine()) != null) {
                System.out.println("Servidor: " + mensaje);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
