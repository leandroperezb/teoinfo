import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class Imagen {
    protected BufferedImage imagen;
    public static final int TAMANIOBLOQUECUADRANTE = 500;

    public Imagen(BufferedImage imagen){
        if (imagen == null) throw new IllegalArgumentException("No se permite un buffer nulo");
        this.imagen = imagen;
    }

    public int getWidth(){
        return imagen.getWidth();
    }

    public int getHeight(){
        return imagen.getHeight();
    }

    public int getColor(int x, int y){
        return (new Color(imagen.getRGB(x, y), true)).getRed();
    }

    public void setColor(int x, int y, int color){
        imagen.setRGB(x, y, (new Color(color, color, color)).getRGB());
    }

    public List<Imagen> obtenerCuadrantes(){
        List<Imagen> lista = new ArrayList<>();

        for (int y = 0; y < this.getHeight(); y += Imagen.TAMANIOBLOQUECUADRANTE){
            for (int x = 0; x < this.getWidth(); x += Imagen.TAMANIOBLOQUECUADRANTE){
                int ancho = Math.min(Imagen.TAMANIOBLOQUECUADRANTE, this.getWidth() - x);
                int alto = Math.min(Imagen.TAMANIOBLOQUECUADRANTE, this.getHeight() - y);
                lista.add(new Imagen(imagen.getSubimage(x, y, ancho, alto)));
            }
        }

        return lista;
    }


    public double[][] probabilidadesCondicionales(){
        final int cantidadColores = 256; //Queda más lindo que el "256" metido en todos lados

        double[][] probabilidades = new double[cantidadColores][cantidadColores];
        int[] totales = new int[cantidadColores];

        //Inicializaciones
        for (int i = 0; i < cantidadColores; i++) {
            for (int j = 0; j < cantidadColores; j++) {
                probabilidades[i][j] = 0d;
            }
            totales[i] = 0;
        }

        //Símbolo anterior (para probabilidades condicionales)
        int anterior = -1;


        //Contabilizar cada transición entre símbolos
        for (int y = 0; y < this.getHeight(); y++){
            for (int x = 0; x < this.getWidth(); x++){
                int color = this.getColor(x, y);
                if (anterior >= 0) {
                    probabilidades[color][anterior]++;
                    totales[anterior]++;
                }
                anterior = color;
            }
        }

        //Calcular las probabilidades de transición condicionales
        for (int i = 0; i < cantidadColores; i++) {
            for (int j = 0; j < cantidadColores; j++) {
                if (totales[j] != 0)
                    probabilidades[i][j] /= totales[j];
            }
        }

        return probabilidades;
    }

    public void guardarComoArchivo(String ruta) throws IOException {
        ImageIO.write(imagen, "bmp", new File(ruta));
    }

}
