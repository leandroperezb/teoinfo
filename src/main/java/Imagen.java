import javax.imageio.ImageIO;
import javax.swing.JPanel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class Imagen extends JPanel{
    public BufferedImage imagen;
    public static final int TAMANIOBLOQUECUADRANTE = 500;
    private double x=0;
    private double y=0;
    private FuenteMarkoviana fuente;
    private double sprnz = Double.NEGATIVE_INFINITY;
    private double vrnz = Double.NEGATIVE_INFINITY;
    protected final int CANTIDADCOLORES = 256;

    public Imagen(BufferedImage imagen){
        if (imagen == null) throw new IllegalArgumentException("No se permite un buffer nulo");
        this.imagen = imagen;
		
		double[][] probabilidades = probabilidadesCondicionales();
        fuente = new FuenteMarkoviana(probabilidades);

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
    
    public double[] probabilidadesSimples(){
        double[] probabilidades = new double[CANTIDADCOLORES];
        int total = 0;

        //Inicializaciones
        for (int i = 0; i < CANTIDADCOLORES; i++) {
                probabilidades[i] = 0d;
        }


        //Contabilizar cada transición entre símbolos
        for (int y = 0; y < this.getHeight(); y++){
            for (int x = 0; x < this.getWidth(); x++){
                int color = this.getColor(x, y);
                probabilidades[color]++;
                total++;
            }
        }

        //Calcular las probabilidades de transición condicionales
        for (int i = 0; i < CANTIDADCOLORES; i++) {
                probabilidades[i] /= total;
        }

        return probabilidades;
    }


    public double entropiaSimple(){
        double[] probabilidades = this.probabilidadesSimples();

        double entropia = 0d;

        for (int i = 0; i < probabilidades.length; i++){
            if (probabilidades[i] != 0)
                entropia += probabilidades[i] * Math.log(probabilidades[i]) / Math.log(2d);
        }

        return -entropia;
    }


    public void guardarComoArchivo(String ruta) throws IOException {
        ImageIO.write(imagen, "bmp", new File(ruta));
    }
    
    public double esperanza() {
    	if (sprnz == Double.NEGATIVE_INFINITY)
    		sprnz = fuente.esperanza(getColor(0, 0));
    	return sprnz;
    }
    
    public double varianza() {
    	if (vrnz == Double.NEGATIVE_INFINITY)
    		vrnz = fuente.varianza(getColor(0, 0));
    	return vrnz;
    }
    
    public void setX(double x) {
    	this.x = x;
    }
    
    public void setY(double y) {
    	this.y = y;
    }

	public void paintComponent(Graphics g) {
		g.drawImage(imagen, (int)x, (int)y, (int)x+imagen.getWidth()/4, (int)y+imagen.getHeight()/4, 0, 0, imagen.getWidth(), imagen.getHeight(), Color.BLACK, null);
	}
}
