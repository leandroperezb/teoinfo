import numericfield.NumericTextField;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.DefaultIntervalXYDataset;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;


public class Main {
	private static JFrame frame;
    private final static int FRAME_WIDTH = 930;
    private final static int FRAME_HEIGHT = 700;
    private final static int FRAME_LOC_X = 250;
    private final static int FRAME_LOC_Y = 30;

    static NumericTextField epsilonEsperanza = null;
    static NumericTextField epsilonDesvio = null;
    static JLabel actualEsperanza;
    static JLabel actualDesvio;

    static JButton setearEpsilons = null;
    static JButton addImagen = null;
    static JButton guardarInfo = null;

    static JFreeChart generarHistograma(JFrame frame, DefaultIntervalXYDataset dataset, int i, boolean mostrar) {
        JFreeChart histograma = ChartFactory.createHistogram("Histograma "+i, "Intensidad de color:",
                "Repeticiones" , dataset);
        ChartPanel chartPanel = new ChartPanel( histograma );
        if (mostrar) {
            frame.getContentPane().add(chartPanel);
            frame.setVisible(true);
        }
        return histograma;
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
                	    frame.getContentPane().removeAll(); //Quitar botón de abrir imagen

                		Screen.sc = new Screen(imagen, frame);

                		GridBagConstraints constraints = new GridBagConstraints();
                		frame.getContentPane().setLayout(new GridBagLayout());

                		JPanel panelLateral = new JPanel();
                        crearPanelLateral(panelLateral);


                        constraints.weightx = 0d; constraints.fill = GridBagConstraints.BOTH;
                        constraints.weighty = 1d; constraints.gridx=1; constraints.gridy=0;
                		frame.getContentPane().add(panelLateral, constraints);

                        constraints.weightx = 1d; constraints.gridx=0;
                		frame.getContentPane().add(Screen.sc, constraints);
                	}
                    frame.repaint();
                    frame.setVisible(true);
                }


            } catch(IOException e){
                JOptionPane.showMessageDialog(null,
                        "Se ha producido un error. No es posible leer el archivo",
                        "Error al leer el archivo",
                        JOptionPane.WARNING_MESSAGE);
            }
            catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "El archivo ingresado no es una imagen válida",
                        "Archivo inválido",
                        JOptionPane.WARNING_MESSAGE);
            }

        }
    }

    private static void generarArchivos(JFrame frame){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccione una carpeta donde guardar los archivos");
        fileChooser.setApproveButtonText("Seleccionar");
        fileChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            new Thread( () -> {
                setearEpsilons.setEnabled(false); addImagen.setEnabled(false); guardarInfo.setEnabled(false);
                String directorio = fileChooser.getSelectedFile().getAbsolutePath();

                TrabajoPractico.incisoA(directorio);
                TrabajoPractico.incisoB(directorio);
                TrabajoPractico.incisoC(directorio);
                TrabajoPractico.incisoD(directorio);

                setearEpsilons.setEnabled(true); addImagen.setEnabled(true); guardarInfo.setEnabled(true);
                JOptionPane.showMessageDialog(null, "Archivos guardados correctamente", "", JOptionPane.INFORMATION_MESSAGE);
            }).start();
        }
    }

    private static void crearPanelLateral(JPanel panelLateral) {
        DecimalFormat format = new DecimalFormat("#,###.#######################################" +
                "#############################################################################" +
                "###################################################################################" +
                "######################################################################");
        format.setGroupingUsed(false);
        format.setParseIntegerOnly(false);


        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH; constraints.weighty = 0d; constraints.weightx = 0;

        panelLateral.setLayout(new GridBagLayout());


        JPanel panelEsperanza = new JPanel(); panelEsperanza.setLayout(new GridBagLayout());
            JLabel label = new JLabel("Épsilon para la media:");
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


        JPanel panelDesvio = new JPanel(); panelDesvio.setLayout(new GridBagLayout());
            label = new JLabel("Épsilon para el desvío:");
            epsilonDesvio = new NumericTextField(15, format);
            epsilonDesvio.setValue(FuenteMarkoviana.epsilonDesvio);

            constraints.gridy = 0; constraints.gridx = 0;
            panelDesvio.add(label, constraints);

            JPanel panelActualDesvio = new JPanel();
            actualDesvio = new JLabel("(Valor actual: " + FuenteMarkoviana.epsilonDesvio + ")");
            panelActualDesvio.add(actualDesvio);

            constraints.gridy = 1;
            panelDesvio.add(epsilonDesvio, constraints);
            constraints.gridy = 2;
            panelDesvio.add(panelActualDesvio, constraints);


        constraints.gridy = 0; constraints.gridx = 0; constraints.ipady = 80;
        panelLateral.add(panelEsperanza, constraints);
        constraints.gridy = 1;
        panelLateral.add(panelDesvio, constraints);

        JPanel panelBoton = new JPanel(); panelBoton.setLayout(new GridBagLayout());
            setearEpsilons = new JButton("Setear nuevos épsilons");
            constraints.gridy = 0; constraints.ipady = 0;
            panelBoton.add(setearEpsilons, constraints);

            JPanel nota = new JPanel();
            nota.add(new JLabel("<html><center><small>(Aborta cálculos en ejecución<br>y borra " +
                    "aquellos resultados<br>previamente calculados)</small></center></html>"));

            constraints.gridy = 1;
            panelBoton.add(nota, constraints);

            //Al clickear el botón, actualizar los épsilons
            setearEpsilons.addActionListener( (evt) -> {
                    try{
                        FuenteMarkoviana.epsilonDesvio = Math.abs(epsilonDesvio.getDoubleValue());
                    }catch (ParseException e) {
                        FuenteMarkoviana.epsilonDesvio = 0d;
                    }finally {
                        epsilonDesvio.setValue(FuenteMarkoviana.epsilonDesvio);
                        actualDesvio.setText("(Valor actual: " + FuenteMarkoviana.epsilonDesvio + ")");
                    }
                    try{
                        FuenteMarkoviana.epsilonEsperanza = Math.abs(epsilonEsperanza.getDoubleValue());
                    }catch (ParseException e){
                        FuenteMarkoviana.epsilonEsperanza = 0d;
                    }finally {
                        epsilonEsperanza.setValue(FuenteMarkoviana.epsilonEsperanza);
                        actualEsperanza.setText("(Valor actual: " + FuenteMarkoviana.epsilonEsperanza + ")");
                    }

                    panelLateral.repaint();
                    Screen.sc.onNuevosEpsilons();
                }
            );
        constraints.gridy = 2;
        panelLateral.add(panelBoton, constraints);


        //El último elemento es un panel vacío con "weighty" máximo, para que apile al resto de los
        //elementos en el tope del frame
        constraints.weighty = 1d; constraints.gridy = 3;
        panelLateral.add(new JPanel(), constraints);
        
		//boton de guardar datos
        JPanel panelGuardarDatos = new JPanel();
		guardarInfo = new JButton("<html><h1>Generar archivos</h1></html>");
        guardarInfo.addActionListener( (evt) -> generarArchivos(frame) );
		panelGuardarDatos.add(guardarInfo);
		constraints.gridy = 4;
		panelLateral.add(panelGuardarDatos, constraints);
        
       //boton de cargar imagen
        JPanel panelCargaImagen = new JPanel();
		addImagen = new JButton("<html><h1>Abrir imagen</h1></html>");
		addImagen.addActionListener( (evt) -> abrirArchivo(frame) );
		panelCargaImagen.add(addImagen);
		constraints.gridy = 5;
		panelLateral.add(panelCargaImagen, constraints);
		

		
		
    }

    public static void main(String[] args){
        frame = new JFrame("Teoría de la información");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        frame.setMinimumSize(new Dimension(930, 600));
        frame.setLocation(FRAME_LOC_X, FRAME_LOC_Y);


        JButton boton = new JButton("<html><h1>Abrir imagen</h1></html>");
        boton.setPreferredSize(new Dimension(300, 80));
        boton.addActionListener( (evt) -> abrirArchivo(frame) );

        frame.getContentPane().setLayout(new GridBagLayout());
        frame.getContentPane().add(boton);

        frame.setVisible(true);
    }
}
