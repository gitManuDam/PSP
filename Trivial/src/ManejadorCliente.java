import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class ManejadorCliente implements Runnable{
    private Socket socket;
    private PrintWriter salida;
    private BufferedReader entrada;
    private int puntuacion = 0;
    private Set<String> quesitos = new HashSet<>();

    public ManejadorCliente(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            salida = new PrintWriter(socket.getOutputStream(), true);

            salida.println("Bienvenido al Trivial! Responde correctamente las preguntas.");
            for (Pregunta p : ServidorTrivial.preguntas) {
                salida.println(p);
                String respuesta = entrada.readLine();
                if (Integer.parseInt(respuesta) == p.opcionCorrecta) {
                    puntuacion++;
                    salida.println("Correcto! Puntuación: " + puntuacion);
                    if (!quesitos.contains(p.categoria)) {
                        quesitos.add(p.categoria);
                        salida.println("¡Has ganado el quesito de " + p.categoria + "!");
                    }
                } else {
                    salida.println("Incorrecto! Respuesta correcta: " + p.opcionCorrecta);
                }
                if (quesitos.size() == 5) {
                    salida.println("¡Felicidades! Has ganado el juego al obtener todos los quesitos.");
                    break;
                }
            }
            salida.println("Juego terminado. Tu puntuación: " + puntuacion);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
