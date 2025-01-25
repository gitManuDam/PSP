import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TrojanServer {
    private static final int PORT = 12345;
    private static final ConcurrentMap<Socket, String> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("Servidor iniciado. Esperando conexiones...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress().getHostAddress());

                // Manejar al cliente en un nuevo hilo
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            System.out.println("llamado handleClient");
            // Leer información del cliente
            StringBuilder clientInfo = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                System.out.println("recibiendo informacion "+ line);
                clientInfo.append(line).append("\n");
            }


            // Almacenar la información del cliente
            clients.put(clientSocket, clientInfo.toString());
            System.out.println("Esperando......");
            // Esperar 30 segundos
            Thread.sleep(30000);

            System.out.println("Enviando datos.....");
            // Enviar la información al cliente con el mensaje de infección
            out.println(clientInfo.toString());
            out.println("Has sido infectado");

            System.out.println("Información enviada al cliente:");
            System.out.println(clientInfo);
            System.out.println("Mensaje de infección enviado.");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            clients.remove(clientSocket);
            System.out.println("Cliente desconectado.");
        }
    }
}
