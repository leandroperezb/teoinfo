import numericfield.NumericTextField;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.DefaultIntervalXYDataset;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.*;
import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private final static int FRAME_WIDTH = 930;
    private final static int FRAME_HEIGHT = 700;
    private final static int FRAME_LOC_X = 250;
    private final static int FRAME_LOC_Y = 30;

    static NumericTextField epsilonEsperanza = null;
    static NumericTextField epsilonVarianza = null;
    static JLabel actualEsperanza;
    static JLabel actualVarianza;
    
    static DefaultIntervalXYDataset hacerDataset(Imagen imagen){
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

    static void mostrarHistograma(JFrame frame, DefaultIntervalXYDataset dataset, int i) {
        JFreeChart histograma = ChartFactory.createHistogram("Histograma "+i, "Intensidad de color:",
                "Repeticiones" , dataset);
        ChartPanel chartPanel = new ChartPanel( histograma );
        frame.getContentPane().add(chartPanel);
        frame.setVisible(true);
    }
    
    static void abrirArchivo(JFrame frame) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                Imagen imagen = new Imagen(ImageIO.read(selectedFile));

                {
                	//si ya esta creado el screen cambio la imagen y recalculo
                	if(Screen.sc != null) {
                		Screen.sc.reset(imagen);
                	}
                	else {
                		Screen.sc = new Screen(imagen, frame);

                		GridBagConstraints constraints = new GridBagConstraints();
                		frame.getContentPane().setLayout(new GridBagLayout());

                		JPanel panelEpsilons = new JPanel();
                        crearPanelEpsilons(panelEpsilons);


                        constraints.weightx = 0d; constraints.fill = GridBagConstraints.BOTH;
                        constraints.weighty = 1d; constraints.gridx=1; constraints.gridy=0;
                		frame.getContentPane().add(panelEpsilons, constraints);

                        constraints.weightx = 1d; constraints.gridx=0;
                		frame.getContentPane().add(Screen.sc, constraints);
                	}
                    frame.repaint();
                    frame.setVisible(true);
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Selected file: " + selectedFile.getAbsolutePath());
        }
    }

    private static void crearPanelEpsilons(JPanel panelEpsilons) {
        DecimalFormat format = new DecimalFormat("#,###.#######################################" +
                "#############################################################################" +
                "###################################################################################" +
                "######################################################################");
        format.setGroupingUsed(false);
        format.setParseIntegerOnly(false);


        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH; constraints.weighty = 0d; constraints.weightx = 0;

        panelEpsilons.setLayout(new GridBagLayout());


        JPanel panelEsperanza = new JPanel(); panelEsperanza.setLayout(new GridBagLayout());
            JLabel label = new JLabel("Épsilon para la esperanza:");
            epsilonEsperanza = new NumericTextField(15, format);
            epsilonEsperanza.setValue(FuenteMarkoviana.epsilonEsperanza);

            constraints.gridy = 0; constraints.gridx = 0;
            panelEsperanza.add(label, constraints);

            JPanel panelActualEsperanza = new JPanel();
            actualEsperanza = new JLabel("(Valor actual: " + FuenteMarkoviana.epsilonEsperanza + ")");
            panelActualEsperanza.add(actualEsperanza);

            constraints.gridy = 1;
            panelEsperanza.add(epsilonEsperanza, constraints);
            constraints.gridy = 2;
            panelEsperanza.add(panelActualEsperanza, constraints);


        JPanel panelVarianza = new JPanel(); panelVarianza.setLayout(new GridBagLayout());
            label = new JLabel("Épsilon para la varianza:");
            epsilonVarianza = new NumericTextField(15, format);
            epsilonVarianza.setValue(FuenteMarkoviana.epsilonVarianza);

            constraints.gridy = 0; constraints.gridx = 0;
            panelVarianza.add(label, constraints);

            JPanel panelActualVarianza = new JPanel();
            actualVarianza = new JLabel("(Valor actual: " + FuenteMarkoviana.epsilonVarianza + ")");
            panelActualVarianza.add(actualVarianza);

            constraints.gridy = 1;
            panelVarianza.add(epsilonVarianza, constraints);
            constraints.gridy = 2;
            panelVarianza.add(panelActualVarianza, constraints);


        constraints.gridy = 0; constraints.gridx = 0; constraints.ipady = 80;
        panelEpsilons.add(panelEsperanza, constraints);
        constraints.gridy = 1;
        panelEpsilons.add(panelVarianza, constraints);

        JPanel panelBoton = new JPanel(); panelBoton.setLayout(new GridBagLayout());
            JButton boton = new JButton("Setear nuevos épsilons");
            constraints.gridy = 0; constraints.ipady = 0;
            panelBoton.add(boton, constraints);

            JPanel nota = new JPanel();
            nota.add(new JLabel("<html><center><small>(Aborta cálculos en ejecución<br>y borra " +
                    "aquellos resultados<br>previamente calculados)</small></center></html>"));

            constraints.gridy = 1;
            panelBoton.add(nota, constraints);

            //Al clickear el botón, actualizar los épsilons
            boton.addActionListener( (evt) -> {
                    try{
                        FuenteMarkoviana.epsilonVarianza = epsilonVarianza.getDoubleValue();
                        FuenteMarkoviana.epsilonEsperanza = epsilonEsperanza.getDoubleValue();

                        actualEsperanza.setText("(Valor actual: " + FuenteMarkoviana.epsilonEsperanza + ")");
                        actualVarianza.setText("(Valor actual: " + FuenteMarkoviana.epsilonVarianza + ")");

                        panelEpsilons.repaint();

                        Screen.sc.onNuevosEpsilons();
                    }catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            );
        constraints.gridy = 2;
        panelEpsilons.add(panelBoton, constraints);


        //El último elemento es un panel vacío con "weighty" máximo, para que apile al resto de los
        //elementos en el tope del frame
        constraints.weighty = 1d; constraints.gridy = 3;
        panelEpsilons.add(new JPanel(), constraints);
    }

    public static void main(String[] args){
        JFrame frame = new JFrame("Teoría de la información");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        frame.setMinimumSize(new Dimension(930, 600));
        frame.setLocation(FRAME_LOC_X, FRAME_LOC_Y);

        frame.setVisible(true);
        
        abrirArchivo(frame);

    }
}
