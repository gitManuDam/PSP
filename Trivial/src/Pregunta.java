public class Pregunta {
    String categoria;
    String pregunta;
    String[] opciones;
    int opcionCorrecta;

    public Pregunta(String categoria, String pregunta, String o1, String o2, String o3, String o4, int correcta) {
        this.categoria = categoria;
        this.pregunta = pregunta;
        this.opciones = new String[]{o1, o2, o3, o4};
        this.opcionCorrecta = correcta;
    }

    @Override
    public String toString() {
        return "Categoría: " + categoria + "\n" + pregunta + "\n1) " + opciones[0] + "\n2) " + opciones[1] + "\n3) " + opciones[2] + "\n4) " + opciones[3];
    }
}
