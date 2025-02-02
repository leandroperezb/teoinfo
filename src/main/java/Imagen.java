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

    private boolean recalcularEscala = false;
    private double ESCALA_IMAGEN = 1;

    public Imagen(BufferedImage imagen){
        if (imagen == null) throw new IllegalArgumentException("No se permite un buffer nulo");
        this.imagen = imagen;

        fuente = new FuenteMarkoviana(probabilidadesCondicionales(), probabilidadesSimples());

    }

    public Imagen(BufferedImage imagen, boolean recalcular){
        this(imagen);
        recalcularEscala = recalcular;
    }

    DefaultIntervalXYDataset hacerDatasetRepeticiones(){
        DefaultIntervalXYDataset dataset = new DefaultIntervalXYDataset();

        Map<Integer, Integer> repeticiones = new HashMap<>();

        //Anotar colores existentes y contar las repeticiones (cantidad de apariciones)
        for (int y = 0; y < this.getAlto(); y++){
            for (int x = 0; x < this.getAncho(); x++){
                int color = this.getColor(x, y);
                if (repeticiones.containsKey(color)) {
                    repeticiones.put(color, repeticiones.get(color) + 1);
                }else{
                    repeticiones.put(color, 1);
                }
            }
        }

        //Para agregar un 0 o un 255 si faltan
        int tamanio = repeticiones.size();
        if (!repeticiones.containsKey(0))
            tamanio++;
        if (!repeticiones.containsKey(255))
            tamanio++;

        //Cargar en el dataset los valores obtenidos
        double[] y = new double[tamanio];
        double[] x = new double[tamanio];
        double[] minX = new double[tamanio];
        double [] maxX = new double[tamanio];
        int contador = 0;
        for(Integer i: repeticiones.keySet()) {
            y[contador] = repeticiones.get(i);
            x[contador] = i; minX[contador] = i - 0.5d; maxX[contador] = i + 0.5d;
            contador++;
        }

        //Agregar los extremos del rango del histograma si es que faltan
        if (!repeticiones.containsKey(0)) {
            tamanio--;
            y[tamanio] = 0;
            x[tamanio] = 0;
            minX[tamanio] = 0;
            maxX[tamanio] = 0.5d;
        }
        if (!repeticiones.containsKey(255)) {
            tamanio--;
            y[tamanio] = 0;
            x[tamanio] = 255;
            minX[tamanio] = 255 - 0.5d;
            maxX[tamanio] = 255;
        }

        dataset.addSeries("Intensidades de color",
                new double[][]{x, minX, maxX, y, y, y});

        return dataset;
    }

    public int getAncho(){
        return imagen.getWidth();
    }

    public int getAlto(){
        return imagen.getHeight();
    }

    public int getColor(int x, int y){
        return (new Color(imagen.getRGB(x, y), true)).getRed();
    }

    public void setColor(int x, int y, int color){
        imagen.setRGB(x, y, (new Color(color, color, color).getRGB()));
    }

    public List<Imagen> obtenerCuadrantes(){
        List<Imagen> lista = new ArrayList<>();

        for (int y = 0; y < this.getAlto(); y += Imagen.TAMANIOBLOQUECUADRANTE){
            for (int x = 0; x < this.getAncho(); x += Imagen.TAMANIOBLOQUECUADRANTE){
                int ancho = Math.min(Imagen.TAMANIOBLOQUECUADRANTE, this.getAncho() - x);
                int alto = Math.min(Imagen.TAMANIOBLOQUECUADRANTE, this.getAlto() - y);
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
        for (int y = 0; y < this.getAlto(); y++){
            for (int x = 0; x < this.getAncho(); x++){
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
        for (int y = 0; y < this.getAlto(); y++){
            for (int x = 0; x < this.getAncho(); x++){
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


    private void recalcularEscala(){
        double heightLibre = this.getHeight(); double widthLibre = this.getWidth();
        double width = imagen.getWidth(); double height = imagen.getHeight();
        double factor = 0d;
        if (width > widthLibre){
            factor = widthLibre / width;
            height = height * factor;
            if (height > heightLibre){
                factor *= heightLibre / height;
            }
        }else{
            factor = heightLibre / height;
            width = width * factor;
            if (width > widthLibre){
                factor *= widthLibre / width;
            }
        }


        ESCALA_IMAGEN = 1/factor;
        if (ESCALA_IMAGEN < 1d)
            ESCALA_IMAGEN = 1d;
    }

	public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (recalcularEscala) {
            recalcularEscala();
            int widthLibre = (this.getWidth() - (int) (imagen.getWidth() / ESCALA_IMAGEN))/2;
            g.drawImage(imagen, widthLibre, 0, (int) (imagen.getWidth() / ESCALA_IMAGEN) + widthLibre, (int) (imagen.getHeight() / ESCALA_IMAGEN), 0, 0, imagen.getWidth(), imagen.getHeight(), Color.BLACK, null);
        }else {
            g.drawImage(imagen, (int) x, (int) y, (int) x + imagen.getWidth() / Screen.ESCALA_IMAGEN, (int) y + imagen.getHeight() / Screen.ESCALA_IMAGEN, 0, 0, imagen.getWidth(), imagen.getHeight(), Color.BLACK, null);
        }
	}
}
