import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.DefaultIntervalXYDataset;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public final static int FrameWidth = 800;
    public final static int FrameHeight = 700;
    public final static int FrameLocX = 250;
    public final static int FrameLocY = 80;
    
    public static DefaultIntervalXYDataset hacerDataset(Imagen imagen){
        DefaultIntervalXYDataset dataset = new DefaultIntervalXYDataset();

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
            dataset.addSeries(i,
                    new double[][]{{i}, {i - 0.5d}, {i + 0.5d}, {repeticiones.get(i)}, {repeticiones.get(i)}, {repeticiones.get(i)}});
        }

        return dataset;
    }

    public static void mostrarHistograma(JFrame frame, DefaultIntervalXYDataset  dataset, int i) {
        JFreeChart histograma = ChartFactory.createHistogram("Histograma "+i, "Intensidad de color:",
                "Repeticiones" , dataset);
        ChartPanel chartPanel = new ChartPanel( histograma );
        frame.getContentPane().add(chartPanel);
        frame.setVisible(true);
    }
    
    public static void abrirArchivo(JFrame frame) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                Imagen imagen = new Imagen(ImageIO.read(selectedFile));

                {
                    frame.getContentPane().add(new Screen(imagen, frame));
                    frame.repaint();
                    frame.setVisible(true);
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Selected file: " + selectedFile.getAbsolutePath());
        }
    }

    public static void main(String[] args){
        JFrame frame = new JFrame("Teoría de la información");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(FrameWidth,FrameHeight);
        frame.setLocation(FrameLocX, FrameLocY);

        frame.setVisible(true);
        
        abrirArchivo(frame);

    }
}
