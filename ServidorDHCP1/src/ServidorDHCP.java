import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

public class ServidorDHCP {
    private static final int PUERTO = 5000;
    private static final int MAX_IPS = 10;
    private static final String BASE_IP = "192.168.1.";

    private final Set<String> ips_disponibles = ConcurrentHashMap.newKeySet();
    private final Map<String, String> ips_asignadas = new ConcurrentHashMap<>();
    private final Map<String, Long> tiempos_expiracion = new ConcurrentHashMap<>();

    private final DefaultListModel<String> modelo_clientes = new DefaultListModel<>();
    private final DefaultListModel<String> modelo_ips_disponibles = new DefaultListModel<>();
    private JList<String> lista_clientes;

    public ServidorDHCP(int maxIps) {
        for (int i = 2; i < 2 + maxIps; i++) {
            ips_disponibles.add(BASE_IP + i);
            modelo_ips_disponibles.addElement(BASE_IP + i);
        }
        iniciarGUI();
    }

    public void iniciar() {
        try (ServerSocket servidor = new ServerSocket(PUERTO)) {
            log("🔵 Servidor DHCP iniciado en el puerto " + PUERTO);

            while (true) {
                Socket socketCliente = servidor.accept();
                new Thread(new ManejadorCliente(socketCliente)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ManejadorCliente implements Runnable {
        private final Socket socketCliente;
        private String ipAsignada = null;

        public ManejadorCliente(Socket socket) {
            this.socketCliente = socket;
        }

        @Override
        public void run() {
            try (BufferedReader entrada = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
                 PrintWriter salida = new PrintWriter(socketCliente.getOutputStream(), true)) {

                String solicitud = entrada.readLine();
                if ("SOLICITAR_IP".equals(solicitud)) {
                    synchronized (ips_disponibles) {
                        if (!ips_disponibles.isEmpty()) {
                            ipAsignada = ips_disponibles.iterator().next();
                            ips_disponibles.remove(ipAsignada);
                            ips_asignadas.put(ipAsignada, socketCliente.getInetAddress().toString());
                            tiempos_expiracion.put(ipAsignada, System.currentTimeMillis() + 10000); // Expira en 10s

                            modelo_ips_disponibles.removeElement(ipAsignada);
                            modelo_clientes.addElement(socketCliente.getInetAddress() + " → " + ipAsignada);

                            salida.println(ipAsignada);
                            log("✅ IP asignada: " + ipAsignada + " a " + socketCliente.getInetAddress());
                        } else {
                            salida.println("NO_HAY_IP");
                            log("⚠️ No hay direcciones IP disponibles.");
                            return;
                        }
                    }

                    while (true) {
                        String mensaje = entrada.readLine();
                        if ("LIBERAR_IP".equals(mensaje)) {
                            liberarIP(ipAsignada);
                            break;
                        } else if ("RENOVAR_IP".equals(mensaje)) {
                            if (tiempos_expiracion.containsKey(ipAsignada)) {
                                tiempos_expiracion.put(ipAsignada, System.currentTimeMillis() + 10000); // Renueva por 10s más
                                salida.println("IP_RENOVADA");
                                log("🔄 IP renovada: " + ipAsignada + " por " + socketCliente.getInetAddress());
                            } else {
                                salida.println("RENOVACION_DENEGADA");
                                log("❌ Renovación denegada: IP " + ipAsignada + " ya no está disponible.");
                            }
                        }
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (ipAsignada != null) {
                    liberarIP(ipAsignada);
                }
                try {
                    socketCliente.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void liberarIP(String ip) {
        synchronized (ips_disponibles) {
            ips_disponibles.add(ip);
            ips_asignadas.remove(ip);
            tiempos_expiracion.remove(ip);
            modelo_ips_disponibles.addElement(ip);

            for (int i = 0; i < modelo_clientes.size(); i++) {
                if (modelo_clientes.getElementAt(i).contains(ip)) {
                    modelo_clientes.remove(i);
                    break;
                }
            }

            log("🔄 IP liberada: " + ip);
        }
    }

    private void log(String mensaje) {
        System.out.println(mensaje);
        try (FileWriter writer = new FileWriter("registro_ips.log", true)) {
            writer.write(LocalDateTime.now() + " - " + mensaje + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void iniciarGUI() {
        JFrame frame = new JFrame("Servidor DHCP");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel panelCentral = new JPanel(new GridLayout(1, 2));

        lista_clientes = new JList<>(modelo_clientes);
        JList<String> listaIps = new JList<>(modelo_ips_disponibles);

        panelCentral.add(new JScrollPane(lista_clientes));
        panelCentral.add(new JScrollPane(listaIps));

        JButton btnLiberar = new JButton("Liberar IP Seleccionada");
        btnLiberar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String seleccion = lista_clientes.getSelectedValue();
                if (seleccion != null) {
                    String[] partes = seleccion.split(" → ");
                    liberarIP(partes[1]);
                }
            }
        });

        frame.add(panelCentral, BorderLayout.CENTER);
        frame.add(btnLiberar, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        ServidorDHCP servidor = new ServidorDHCP(MAX_IPS);
        servidor.iniciar();
    }
}
