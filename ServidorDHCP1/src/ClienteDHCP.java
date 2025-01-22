import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.Scanner;

public class ClienteDHCP {
    private static final String SERVIDOR = "localhost";
    private static final int PUERTO_SERVIDOR = 5000;
    private static final int TIEMPO_USO = new Random().nextInt(5000) + 5000; // 5-10 segundos

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVIDOR, PUERTO_SERVIDOR);
             BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            //Solicitar IP al servidor
            System.out.println("Solicitando una dirección IP al servidor...");
            salida.println("SOLICITAR_IP");
            String ipAsignada = entrada.readLine();

            if ("NO_HAY_IP".equals(ipAsignada)) {
                System.out.println("No hay direcciones IP disponibles. Cliente cerrando...");
                return;
            }

            System.out.println("Cliente recibió la IP: " + ipAsignada);
            System.out.println("Usando IP por " + TIEMPO_USO / 1000 + " segundos...");


            //Simular el uso del cliente por cierto tiempo
            Thread.sleep(TIEMPO_USO);

            //Preguntar si el cliente quiere renovar la IP
            System.out.println("¿Quieres renovar la IP? (s/n)");
            String respuesta = scanner.nextLine();

            if (respuesta.equalsIgnoreCase("s")) {
                salida.println("RENOVAR_IP");
                System.out.println("Enviando solicitud de renovación...");
                String respuestaServidor = entrada.readLine();

                if ("IP_RENOVADA".equals(respuestaServidor)) {
                    System.out.println("IP renovada con éxito. Usándola por más tiempo...");
                    Thread.sleep(TIEMPO_USO);
                } else {
                    System.out.println("No se pudo renovar la IP. Procediendo a liberarla...");
                }
            }

            //Avisar que la IP se libera
            salida.println("LIBERAR_IP");
            System.out.println("Cliente liberando IP: " + ipAsignada);
            System.out.println("Cliente desconectado.");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
