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
        // Historia
        preguntas.add(new Pregunta("Historia", "¿Quién descubrió América?", "Colón", "Magallanes", "Vespucci", "Cook", 1));
        preguntas.add(new Pregunta("Historia", "¿En qué año cayó el Imperio Romano de Occidente?", "395 d.C.", "476 d.C.", "1453 d.C.", "1492 d.C.", 2));
        preguntas.add(new Pregunta("Historia", "¿Qué evento marcó el inicio de la Segunda Guerra Mundial?", "La invasión de Polonia", "El ataque a Pearl Harbor", "La Revolución Rusa", "El tratado de Versalles", 1));
        preguntas.add(new Pregunta("Historia", "¿Quién fue el primer presidente de los Estados Unidos?", "George Washington", "Abraham Lincoln", "Thomas Jefferson", "John Adams", 1));

        // Ciencia
        preguntas.add(new Pregunta("Ciencia", "¿Cuál es el elemento químico más abundante en la Tierra?", "Oxígeno", "Hierro", "Silicio", "Carbono", 1));
        preguntas.add(new Pregunta("Ciencia", "¿Cuál es el planeta más grande del sistema solar?", "Tierra", "Marte", "Júpiter", "Saturno", 3));
        preguntas.add(new Pregunta("Ciencia", "¿Qué órgano del cuerpo humano consume más oxígeno?", "Cerebro", "Pulmones", "Corazón", "Hígado", 1));
        preguntas.add(new Pregunta("Ciencia", "¿Qué unidad se usa para medir la resistencia eléctrica?", "Voltio", "Ohmio", "Amperio", "Watt", 2));

        // Música
        preguntas.add(new Pregunta("Música", "¿Quién es el rey del pop?", "Elvis Presley", "Michael Jackson", "Freddie Mercury", "Madonna", 2));
        preguntas.add(new Pregunta("Música", "¿De qué país es originaria la música Reggae?", "Jamaica", "Brasil", "Cuba", "Estados Unidos", 1));
        preguntas.add(new Pregunta("Música", "¿Cuál de estas bandas es británica?", "The Beatles", "Metallica", "AC/DC", "Nirvana", 1));
        preguntas.add(new Pregunta("Música", "¿Quién es la voz principal de la banda Queen?", "Freddie Mercury", "David Bowie", "Paul McCartney", "Mick Jagger", 1));

        // Cine
        preguntas.add(new Pregunta("Cine", "¿Cuál es la película más taquillera de la historia?", "Avatar", "Avengers: Endgame", "Titanic", "Star Wars", 1));
        preguntas.add(new Pregunta("Cine", "¿Quién dirigió la trilogía de 'El Señor de los Anillos'?", "Peter Jackson", "James Cameron", "Steven Spielberg", "Christopher Nolan", 1));
        preguntas.add(new Pregunta("Cine", "¿En qué año se estrenó la primera película de 'Harry Potter'?", "1999", "2001", "2003", "2005", 2));
        preguntas.add(new Pregunta("Cine", "¿Quién interpreta a Jack en 'Titanic'?", "Brad Pitt", "Johnny Depp", "Leonardo DiCaprio", "Tom Cruise", 3));

        // Deporte
        preguntas.add(new Pregunta("Deporte", "¿Cuántos jugadores tiene un equipo de fútbol en el campo?", "9", "10", "11", "12", 3));
        preguntas.add(new Pregunta("Deporte", "¿En qué país se originó el baloncesto?", "Estados Unidos", "España", "Inglaterra", "Francia", 1));
        preguntas.add(new Pregunta("Deporte", "¿Cuántos Grand Slam de tenis existen?", "2", "3", "4", "5", 3));
        preguntas.add(new Pregunta("Deporte", "¿Cuántos puntos vale un triple en baloncesto?", "1", "2", "3", "4", 3));
    }


    public static void enviarMensajeATodos(String mensaje) {
        for (ManejadorCliente cliente : clientes) {
            cliente.enviarMensaje(mensaje);
        }
    }
}