import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class OpcionSeleccionada implements ActionListener {
    private final int respuesta;
    private final ClienteTrivial cliente;

    public OpcionSeleccionada(int respuesta, ClienteTrivial cliente) {
        this.respuesta = respuesta;
        this.cliente = cliente;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        cliente.enviarRespuesta(respuesta);
    }
}