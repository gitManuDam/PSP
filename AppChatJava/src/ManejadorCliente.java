import java.io.*;
import java.net.Socket;

public class ManejadorCliente implements Runnable{
    private final Socket socket;
    private final ServidorGUI servidorGUI; // GUI del servidor

    public ManejadorCliente(Socket socket, ServidorGUI servidorGUI)  {
        this.socket = socket;
        this.servidorGUI = servidorGUI;
    }

    @Override
    public void run(){
        try (
                PrintWriter fsalida = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader fentrada = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            String mensaje;
            fsalida.println("Conexión establecida. Escribe 'salir' para finalizar.");
            while ((mensaje = fentrada.readLine()) != null) {
                servidorGUI.agregarLog("Cliente: " + mensaje); // Log en la GUI
                if ("salir".equalsIgnoreCase(mensaje)) {
                    fsalida.println("Conexión finalizada por el cliente.");
                    break;
                }
                fsalida.println("Eco: " + mensaje);
            }
        } catch (IOException e){
            servidorGUI.agregarLog("Error con cliente: " + e.getMessage());
        } finally {
            cerrarSocket();
        }
    }

    private void cerrarSocket(){
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                servidorGUI.agregarLog("Conexión con cliente cerrada. ");
            }
        } catch (IOException e) {
            servidorGUI.agregarLog("Error al cerrar el socket:  " + e.getMessage());
        }
    }
}
