import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static DefaultCategoryDataset hacerDataset(Imagen imagen){
        DefaultCategoryDataset dataset =  new DefaultCategoryDataset();

        Map<Integer, Integer> repeticiones = new HashMap<>();

        //Anotar colores existentes y contar las repeticiones (cantidad de apariciones)
        for (int y = 0; y < imagen.getHeight(); y++){
            for (int x = 0; x < imagen.getWidth(); x++){
                int color = imagen.getColor(x, y);
                if (repeticiones.containsKey(color)) {
                    repeticiones.put(color, repeticiones.get(color) + 1);
                }else{
                    repeticiones.put(color, 1);
                }
            }
        }

        //Cargar en el dataset los valores obtenidos
        for(Integer i: repeticiones.keySet()) {
            dataset.addValue(repeticiones.get(i), i, "");
        }

        return dataset;
    }

    public static void mostrarHistograma(JFrame frame, DefaultCategoryDataset dataset) {
        JFreeChart histograma = ChartFactory.createBarChart("Histograma", "Intensidad de color:",
                "Repeticiones" , dataset);
        ChartPanel chartPanel = new ChartPanel( histograma );
        chartPanel.setPreferredSize(new java.awt.Dimension( 800 , 600 ) );

        frame.add(chartPanel);
        frame.pack();
    }

    public static void main(String[] args){
        JFrame frame = new JFrame("Teoría de la información");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800,600);
        frame.setLocation(300,80);

        frame.setVisible(true);


        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                Imagen imagen = new Imagen(ImageIO.read(selectedFile));

                //Main.mostrarHistograma(frame, Main.hacerDataset(imagen.obtenerCuadrantes().get(0)));
                Main.mostrarHistograma(frame, Main.hacerDataset(imagen));

                {
                    JFrame frame2 = new JFrame("Imagen analizada");
                    frame2.setSize(800,600);
                    frame2.setLocation(300,80);

                    ImagePanel im = new ImagePanel();
                    im.setBackground(imagen.imagen);
                    frame2.getContentPane().add(im);

                    frame2.setVisible(true);
                }


                //imagen = imagen.obtenerCuadrantes().get(2);

                double[][] probabilidades = imagen.probabilidadesCondicionales();
                int simboloInicial = imagen.getColor(0, 0);

                /*
                //Imprimir las probabilidades en un archivo
                File file = new File(System.getProperty("user.dir") + "/salida.txt");
                final PrintStream fileout = new PrintStream(file);
                for (int i = 0; i < probabilidades.length; i++){
                    String salida = "";
                    for (int j = 0; j < probabilidades.length; j++){
                        salida += " " + probabilidades[i][j];
                    }
                    salida += "\n";
                    fileout.print(salida);
                }*/


                FuenteMarkoviana fuente = new FuenteMarkoviana(probabilidades);
                System.out.println("Esperanza: " + fuente.esperanza(simboloInicial));
                double varianza = fuente.varianza(simboloInicial);
                System.out.println("Varianza: " + varianza);
                System.out.println("Desvío: " + Math.sqrt(varianza));

                double[] vectorEstacionario = fuente.vectorEstacionario(simboloInicial);

            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Selected file: " + selectedFile.getAbsolutePath());
        }
    }
}
