import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;

public class ManejadorCliente implements Runnable {
    private final Socket socket;
    private PrintWriter salida;
    private int puntuacion = 0;
    private final Set<String> quesitos = new HashSet<>();

    public ManejadorCliente(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            salida = new PrintWriter(socket.getOutputStream(), true);

            salida.println("Bienvenido al Trivial! Responde correctamente las preguntas");
            System.out.println("Un nuevo jugador se ha conectado");

            for (Pregunta p : ServidorTrivial.preguntas) {
                String preguntaTexto = "CATEGORIA: " + p.categoria + "||" +
                        "PREGUNTA: " + p.pregunta + "||" +
                        "1) " + p.opciones[0] + "||" +
                        "2) " + p.opciones[1] + "||" +
                        "3) " + p.opciones[2] + "||" +
                        "4) " + p.opciones[3] + "||";

                salida.println(preguntaTexto);
                salida.flush();

                String respuesta;
                try {
                    respuesta = entrada.readLine();
                    if (respuesta == null) {
                        System.out.println("El cliente cerró la conexión de manera normal");
                        break;
                    }
                } catch (SocketException ex) {
                    System.out.println("Cliente desconectado inesperadamente: " + ex.getMessage());
                    break;
                } catch (IOException ex) {
                    System.out.println("Error al leer la respuesta del cliente: " + ex.getMessage());
                    break;
                }

                try {
                    int respuestaInt = Integer.parseInt(respuesta);
                    if (respuestaInt == p.opcionCorrecta) {
                        puntuacion++;
                        salida.println("Correcto! Puntuación: " + puntuacion);
                        ServidorTrivial.enviarMensajeATodos("Un jugador ha acertado una pregunta!");

                        if (!quesitos.contains(p.categoria)) {
                            quesitos.add(p.categoria);
                            salida.println("¡Has ganado el quesito de " + p.categoria + "!");
                        }
                    } else {
                        salida.println("Incorrecto! La respuesta correcta era: " + p.opcionCorrecta);
                        ServidorTrivial.enviarMensajeATodos("Un jugador falló una pregunta.");
                    }
                } catch (NumberFormatException e) {
                    salida.println("Error: Debes ingresar un número del 1 al 4.");
                }

                if (quesitos.size() == 5) {
                    salida.println("¡Felicidades! Has ganado el juego al obtener todos los quesitos.");
                    salida.println("FIN DEL JUEGO");
                    break;
                }
            }

            salida.println("Juego terminado. Tu puntuación final: " + puntuacion);
            salida.println("FIN DEL JUEGO");

        } catch (SocketException e) {
            System.out.println("Cliente desconectado inesperadamente: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error en la comunicación con el cliente: " + e.getMessage());
        } finally {
            try {
                if (!socket.isClosed()) {
                    socket.close();
                }
                System.out.println("Conexión cerrada con un cliente.");
            } catch (IOException e) {
                System.out.println("Error al cerrar el socket: " + e.getMessage());
            }
        }
    }

    public void enviarMensaje(String mensaje) {
        salida.println(mensaje);
    }
}