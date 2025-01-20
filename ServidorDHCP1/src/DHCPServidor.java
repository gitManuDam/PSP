import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class DHCPServidor {
    private static final List<String> ipPool = new ArrayList<>();
    private static final Map<String, String> assignedIPs = new ConcurrentHashMap<>();
    private static final int PORT = 8080;
    private static final int LEASE_TIME = 30000; // Tiempo de uso en milisegundos

    public static void main(String[] args) throws IOException {
        // Inicializamos el pool de direcciones IP
        for (int i = 1; i <= 10; i++) {
            ipPool.add("192.168.1." + i);
        }

        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Servidor DHCP iniciado en el puerto " + PORT);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(new ClientHandler(clientSocket)).start();
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String request = in.readLine();
                if ("REQUEST_IP".equals(request)) {
                    synchronized (ipPool) {
                        if (!ipPool.isEmpty()) {
                            String assignedIP = ipPool.remove(0);
                            assignedIPs.put(assignedIP, clientSocket.getInetAddress().toString());
                            out.println("IP_ASSIGNED:" + assignedIP);

                            // Temporizador para liberar la IP después del tiempo configurado
                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    releaseIP(assignedIP);
                                }
                            }, LEASE_TIME);

                            System.out.println("Asignada IP " + assignedIP + " a " + clientSocket.getInetAddress());
                        } else {
                            out.println("NO_IP_AVAILABLE");
                            System.out.println("No hay direcciones IP disponibles para " + clientSocket.getInetAddress());
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void releaseIP(String ip) {
            synchronized (ipPool) {
                if (assignedIPs.remove(ip) != null) {
                    ipPool.add(ip);
                    System.out.println("Liberada IP " + ip);
                }
            }
        }
    }
}
