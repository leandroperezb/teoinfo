import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;

import java.awt.*;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TrabajoPractico {
    public static void incisoA(String directorio){
        BufferedWriter output = null;
        try {
            output = new BufferedWriter(new FileWriter(directorio + "/iniciso A.txt"));

            for (int i = 0; i < Screen.imagenes.size(); i++){ //Por cada bloque de la imagen
                output.write("Bloque " + (i+1) + "\n");
                output.write("Entropía sin memoria: " + Screen.imagenes.get(i).entropiaSinMemoria() + "\n");
                output.write("Entropía con memoria: " + Screen.imagenes.get(i).entropiaConMemoria() + "\n");
                output.write("\n\n");
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void incisoB(String directorio) {
        JFreeChart histogramaMayorEntropia = Main.generarHistograma(null,
                Screen.imagenes.get(Screen.posEntropiaMayor).hacerDatasetRepeticiones(),
                Screen.posEntropiaMayor + 1, false);
        JFreeChart histogramaMenorEntropia = Main.generarHistograma(null,
                Screen.imagenes.get(Screen.posEntropiaMenor).hacerDatasetRepeticiones(),
                Screen.posEntropiaMenor + 1, false);
        JFreeChart histogramaPromedioEntropia = Main.generarHistograma(null,
                Screen.imagenes.get(Screen.posEntropiaPromedio).hacerDatasetRepeticiones(),
                Screen.posEntropiaPromedio + 1, false);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) screenSize.getWidth();
        int height = (int) screenSize.getHeight();

        try {
            ChartUtils.saveChartAsPNG(new File(directorio + "/inciso B - Histograma bloque mayor entropía.png"),
                  histogramaMayorEntropia, width, height);
            ChartUtils.saveChartAsPNG(new File(directorio + "/inciso B - Histograma bloque menor entropía.png"),
                    histogramaMenorEntropia, width, height);
            ChartUtils.saveChartAsPNG(new File(directorio + "/inciso B - Histograma bloque de entropía más cercana al promedio.png"),
                    histogramaPromedioEntropia, width, height);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void incisoC(String directorio){
        directorio = directorio + "/inciso C - ";
        cargarMatrizEnArchivo(directorio + "Bloque de mayor entropía.txt", Screen.posEntropiaMayor);
        cargarMatrizEnArchivo(directorio + "Bloque de menor entropía.txt", Screen.posEntropiaMenor);
    }


    public static void incisoD(String directorio){
        BufferedWriter output = null;
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        try {
            output = new BufferedWriter(new FileWriter(directorio + "/salida iniciso D.txt"));

            @SuppressWarnings("unchecked")
            Future<Double>[] desvios = (Future<Double>[]) new Future<?>[Screen.imagenes.size()];
            @SuppressWarnings("unchecked")
            Future<Double>[] medias = (Future<Double>[]) new Future<?>[Screen.imagenes.size()];


            //Mandar a ejecutar concurrentemente todos los cálculo necesarios
            for (int i = 0; i < Screen.imagenes.size(); i++){
                final int im = i;
                desvios[i] = executor.submit( () -> Screen.imagenes.get(im).desvio());
                medias[i] = executor.submit( () -> Screen.imagenes.get(im).esperanza());
            }


            //Escribir resultados en el archivo
            for (int i = 0; i < Screen.imagenes.size(); i++){ //Por cada bloque de la imagen
                output.write("Bloque " + (i+1) + ":\n");
                output.write("Desvío: " + desvios[i].get() + "\n");
                output.write("Valor medio: " + medias[i].get() + "\n");
                output.write("\n\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        executor.shutdown();
    }



    private static void cargarMatrizEnArchivo(String ruta, int posicionBloque){
        BufferedWriter output = null;
        try {
            output = new BufferedWriter(new FileWriter(ruta));
            double[][] probabilidades = Screen.imagenes.get(posicionBloque).probabilidadesCondicionales();

            for (int i = 0; i < probabilidades.length; i++){
                for (int j = 0; j < probabilidades.length; j++){
                    output.write(Double.toString(probabilidades[j][i]) + " ");
                }
                output.write("\n");
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
