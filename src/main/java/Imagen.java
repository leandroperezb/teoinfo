import org.jfree.data.xy.DefaultIntervalXYDataset;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Imagen extends JPanel{
    public BufferedImage imagen;
    public static final int TAMANIOBLOQUECUADRANTE = 500;
    private double x=0;
    private double y=0;
    private FuenteMarkoviana fuente;
    private double sprnz = Double.NEGATIVE_INFINITY;
    private double vrnz = Double.NEGATIVE_INFINITY;
    private double dvio = Double.NEGATIVE_INFINITY;
    private boolean resetSprnz = true;
    private boolean resetVrnz = true;
    private boolean resetDvio = true;
    private double entropiaConMemoria = Double.NEGATIVE_INFINITY;
    protected final int CANTIDADCOLORES = 256;

    //Usados sus monitores, no como locks, por si se frenan threads en medio de un lock/unlock
    private Lock mutSprnz = new ReentrantLock();
    private Lock mutDvio = new ReentrantLock();
    private Lock mutVrnz = new ReentrantLock();

    public Imagen(BufferedImage imagen){
        if (imagen == null) throw new IllegalArgumentException("No se permite un buffer nulo");
        this.imagen = imagen;

        fuente = new FuenteMarkoviana(probabilidadesCondicionales(), probabilidadesSimples());

    }

    DefaultIntervalXYDataset hacerDatasetRepeticiones(){
        DefaultIntervalXYDataset dataset = new DefaultIntervalXYDataset();

        Map<Integer, Integer> repeticiones = new HashMap<>();

        //Anotar colores existentes y contar las repeticiones (cantidad de apariciones)
        for (int y = 0; y < this.getHeight(); y++){
            for (int x = 0; x < this.getWidth(); x++){
                int color = this.getColor(x, y);
                if (repeticiones.containsKey(color)) {
                    repeticiones.put(color, repeticiones.get(color) + 1);
                }else{
                    repeticiones.put(color, 1);
                }
            }
        }

        //Cargar en el dataset los valores obtenidos
        for(Integer i: repeticiones.keySet()) {
            dataset.addSeries(i,
                    new double[][]{{i}, {i - 0.5d}, {i + 0.5d}, {repeticiones.get(i)}, {repeticiones.get(i)}, {repeticiones.get(i)}});
        }

        return dataset;
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
        double[][] probabilidades = new double[CANTIDADCOLORES][CANTIDADCOLORES];
        int[] totales = new int[CANTIDADCOLORES];

        //Inicializaciones
        for (int i = 0; i < CANTIDADCOLORES; i++) {
            for (int j = 0; j < CANTIDADCOLORES; j++) {
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
                    probabilidades[anterior][color]++;
                    totales[anterior]++;
                }
                anterior = color;
            }
        }

        //Calcular las probabilidades de transición condicionales
        for (int i = 0; i < CANTIDADCOLORES; i++) {
            for (int j = 0; j < CANTIDADCOLORES; j++) {
                if (totales[i] != 0)
                    probabilidades[i][j] /= totales[i];
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


    public double entropiaSinMemoria(){
        return fuente.entropiaSinMemoria();
    }

    public double entropiaConMemoria(){
        if (entropiaConMemoria == Double.NEGATIVE_INFINITY)
            entropiaConMemoria = fuente.entropiaConMemoria();
        return entropiaConMemoria;
    }


    public void guardarComoArchivo(String ruta) throws IOException {
        ImageIO.write(imagen, "bmp", new File(ruta));
    }
    
    public double esperanza() {
        synchronized (mutSprnz) {
            if (resetSprnz)
                sprnz = fuente.esperanza();
            resetSprnz = false;
            return sprnz;
        }
    }

    public void resetSprnz(){  resetSprnz = true;  }

    public void resetVrnz(){
        resetVrnz = true;
    }

    public void resetDvio(){
        resetDvio = true;
    }
    
    public double varianza() {
        synchronized (mutVrnz) {
            if (resetVrnz)
                vrnz = fuente.varianza();
            resetVrnz = false;
            return vrnz;
        }
    }

    public double desvio() {
        synchronized (mutDvio) {
            if (resetDvio)
                dvio = fuente.desvio();
            resetDvio = false;
            return dvio;
        }
    }
    
    public void setX(double x) {
    	this.x = x;
    }
    
    public void setY(double y) {
    	this.y = y;
    }

	public void paintComponent(Graphics g) {
		g.drawImage(imagen, (int)x, (int)y, (int)x+imagen.getWidth()/ Screen.ESCALA_IMAGEN, (int)y+imagen.getHeight()/ Screen.ESCALA_IMAGEN, 0, 0, imagen.getWidth(), imagen.getHeight(), Color.BLACK, null);
	}
}
