import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServidorTrivial {
    private static final int PUERTO = 5000;
    private static final List<ManejadorCliente> clientes = new ArrayList<>();
    static List<Pregunta> preguntas = new ArrayList<>();

    public static void main(String[] args) {
        cargarPreguntas();

        try (ServerSocket servidorSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor Trivial iniciado en el puerto " + PUERTO);

            while (true) { // Mantener el servidor en ejecución continuamente
                Socket socketCliente = servidorSocket.accept();
                System.out.println("Nuevo jugador conectado");
                ManejadorCliente manejador = new ManejadorCliente(socketCliente);
                clientes.add(manejador);
                new Thread(manejador).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void cargarPreguntas() {
        preguntas.add(new Pregunta("Historia", "¿Quién descubrió América?", "Colón", "Magallanes", "Vespucci", "Cook", 1));
        preguntas.add(new Pregunta("Ciencia", "¿Cuál es el elemento químico más abundante en la Tierra?", "Oxígeno", "Hierro", "Silicio", "Carbono", 1));
        preguntas.add(new Pregunta("Música", "¿Quién es el rey del pop?", "Elvis Presley", "Michael Jackson", "Freddie Mercury", "Madonna", 2));
        preguntas.add(new Pregunta("Cine", "¿Cuál es la película más taquillera de la historia?", "Avatar", "Avengers: Endgame", "Titanic", "Star Wars", 1));
        preguntas.add(new Pregunta("Deporte", "¿Cuántos jugadores tiene un equipo de fútbol en el campo?", "9", "10", "11", "12", 3));
    }

    public static void enviarMensajeATodos(String mensaje) {
        for (ManejadorCliente cliente : clientes) {
            cliente.enviarMensaje(mensaje);
        }
    }
}