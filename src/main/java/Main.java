import numericfield.NumericTextField;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.DefaultIntervalXYDataset;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

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
    static NumericTextField ht = null;
    static JLabel actualEsperanza;
    static JLabel actualDesvio;
    static JLabel actualHt;

    static JButton setearEpsilons = null;
    static JButton setearHt = null;
    static JButton addImagen = null;
    static JButton guardarInfo = null;
    
    static JButton botonCod = null;
    static JButton StCod = new JButton("<html><h2>Guardar imagen codificada</h2></html>");

    static JFreeChart generarHistograma(JFrame frame, DefaultIntervalXYDataset dataset, int i, boolean mostrar) {
        JFreeChart histograma = ChartFactory.createHistogram("Histograma del bloque N°"+i, "Intensidades",
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
        if (botonCod.isSelected()) {
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Imagen codificada (.cod)", "cod"));
        }
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
        	Imagen imagen = null;
           	if (botonCod.isSelected()) {
        		imagen =Codificaciones.levantarArchivo(selectedFile.toString());
        	}
           	else { try {
            	imagen = new Imagen(ImageIO.read(selectedFile));
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
            }}

        	//si ya esta creado el screen cambio la imagen y recalculo
        	if(Screen.sc != null) {
        		Screen.sc.reset(imagen);
        	}
        	else {
        	    frame.getContentPane().removeAll(); //Quitar botones de primer menú

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
		botonCod.setSelected(false);
    }
    
    private static JFileChooser getFileChooser(String dialogTitle, int mode) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        fileChooser.setDialogTitle(dialogTitle);
        fileChooser.setApproveButtonText("Seleccionar");
        fileChooser.setFileSelectionMode( mode);
        return fileChooser;
    }
    
    public static void guardarCod() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        fileChooser.setDialogTitle("Guardar imagen codificada");
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Imagen codificada (.cod)", "cod"));
        fileChooser.setApproveButtonText("Guardar");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            String ruta = fileChooser.getSelectedFile().getAbsolutePath();
            if (ruta.length() > 4 && !ruta.substring(ruta.length() - 4).equalsIgnoreCase(".cod")){
                ruta = ruta + ".cod";
            }
            Codificaciones.guardarEnArchivo(ruta, Screen.getImagen());

            JOptionPane.showMessageDialog(null, "Imagen codificada correctamente", "", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    public static void abrirCod() {
        botonCod.setSelected(true);
        abrirArchivo(frame);
    }

    private static void generarArchivos(JFrame frame, JLabel texto){
        JFileChooser fileChooser = getFileChooser("Seleccione una carpeta donde guardar los archivos", JFileChooser.DIRECTORIES_ONLY );
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            new Thread( () -> {

                texto.setVisible(true);
                setearEpsilons.setEnabled(false); addImagen.setEnabled(false); guardarInfo.setEnabled(false);
                String directorio = fileChooser.getSelectedFile().getAbsolutePath();

                TrabajoPractico.incisoA(directorio);
                TrabajoPractico.incisoB(directorio);
                TrabajoPractico.incisoC(directorio);
                TrabajoPractico.incisoD(directorio);

                texto.setVisible(false);
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
        
        JPanel panelHt = new JPanel(); panelHt.setLayout(new GridBagLayout());
		    actualHt = new JLabel("Valor de Ht:  (Valor actual: " + Codificaciones.UMBRAL + ")");
		    ht = new NumericTextField(15, format);
            ht.setValue(Codificaciones.UMBRAL);

            constraints.gridy = 0; constraints.gridx = 0;
            panelHt.add(actualHt, constraints);

            constraints.gridy = 1;
            panelHt.add(ht, constraints);

        constraints.gridy = 0;
	    panelLateral.add(panelHt, constraints);
	        
	    JPanel panelBotonHt = new JPanel(); panelBotonHt.setLayout(new GridBagLayout());
            setearHt = new JButton("Establecer nuevo Ht");
            constraints.gridy = 0;
            panelBotonHt.add(setearHt, constraints);

            //Al clickear el botón, actualizar el valor de ht
            setearHt.addActionListener( (evt) -> {
                    try{
                        Codificaciones.UMBRAL = Math.abs(ht.getDoubleValue());
                    }catch (ParseException e) {
                        Codificaciones.UMBRAL = 4d;
                    }finally {
                        ht.setValue(Codificaciones.UMBRAL);
                        actualHt.setText("Valor de Ht:  (Valor actual: " + Codificaciones.UMBRAL + ")");
                    }

                    panelLateral.repaint();
                }
            );
        constraints.gridy = 1;
        panelLateral.add(panelBotonHt, constraints);


        JPanel panelEsperanza = new JPanel(); panelEsperanza.setLayout(new GridBagLayout());
            JLabel label = new JLabel("Épsilon para la media:");
            epsilonEsperanza = new NumericTextField(15, format);
            epsilonEsperanza.setValue(FuenteMarkoviana.epsilonEsperanza);

            constraints.gridy = 0;
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


        constraints.gridy = 2; constraints.gridx = 0; constraints.ipady = 20;
        panelLateral.add(panelEsperanza, constraints);
        constraints.gridy = 3;
        panelLateral.add(panelDesvio, constraints);

        JPanel panelBoton = new JPanel(); panelBoton.setLayout(new GridBagLayout());
            setearEpsilons = new JButton("Establecer nuevos épsilons");
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
        constraints.gridy = 4;
        panelLateral.add(panelBoton, constraints);

        //Panel vacío para "espaciar"
        constraints.gridy = 5; constraints.weighty = 0.2d;
        panelLateral.add(new JPanel(), constraints);

        
		//boton de guardar datos
        JPanel panelGuardarDatos = new JPanel(); panelGuardarDatos.setLayout(new GridBagLayout());
            JPanel panelGuardandoDatos = new JPanel();
            JLabel textoGuardando = new JLabel("Generando archivos. Espere por favor..."); textoGuardando.setVisible(false);
            panelGuardandoDatos.add(textoGuardando);
            guardarInfo = new JButton("<html><h1>Generar archivos</h1></html>");
            guardarInfo.addActionListener( (evt) -> generarArchivos(frame, textoGuardando) );
            constraints.gridy = 0; constraints.weighty = 0d;
            panelGuardarDatos.add(guardarInfo, constraints);
            constraints.gridy = 1; constraints.weightx = 1d;
            panelGuardarDatos.add(panelGuardandoDatos, constraints);

		constraints.gridy = 6; constraints.weighty = 0.2d; constraints.weightx = 0d;
		panelLateral.add(panelGuardarDatos, constraints);


        //boton de cargar imagen
        JPanel panelCargaImagen = new JPanel(); panelCargaImagen.setLayout(new GridBagLayout());
            addImagen = new JButton("<html><h1>Abrir imagen</h1></html>");
            addImagen.addActionListener( (evt) -> abrirArchivo(frame) );
            constraints.gridy = 0; constraints.weightx = 1d; constraints.weighty = 0d;
            panelCargaImagen.add(addImagen, constraints);

        constraints.gridy = 7; constraints.weighty = 0.2d; constraints.weightx = 0d;
        panelLateral.add(panelCargaImagen, constraints);

        
       //boton de abrir codificación
        JPanel panelCargaCod = new JPanel(); panelCargaCod.setLayout(new GridBagLayout());
			botonCod = new JButton("<html><h2>Abrir imagen codificada</h2></html>");
			botonCod.addActionListener( (evt) -> abrirCod() );
            constraints.gridy = 0; constraints.weightx = 1d; constraints.weighty = 0d;
			panelCargaCod.add(botonCod, constraints);
		
		constraints.gridy = 8; constraints.weighty = 0.2d; constraints.weightx = 0d;
		panelLateral.add(panelCargaCod, constraints);


        //boton de codificar imagen
        JPanel panelCodImagen = new JPanel(); panelCodImagen.setLayout(new GridBagLayout());
        StCod.addActionListener( (evt) -> guardarCod());
        constraints.gridy = 0; constraints.weightx = 1d; constraints.weighty = 0d;
        panelCodImagen.add(StCod, constraints);

        constraints.gridy = 9; constraints.weighty = 0.2d; constraints.weightx = 0d;
        panelLateral.add(panelCodImagen, constraints);

    }

    public static void calcularRuido(){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccione la imagen de entrada al canal (la original)");
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            try {
                Imagen imagenEntrada = new Imagen(ImageIO.read(selectedFile));
                JFileChooser fileChooser2 = new JFileChooser();
                fileChooser2.setDialogTitle("Seleccione la imagen de salida del canal");
                fileChooser2.setCurrentDirectory(new File(System.getProperty("user.dir")));

                if (fileChooser2.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION){
                    Imagen imagenSalida = new Imagen(ImageIO.read(fileChooser2.getSelectedFile()));

                    JOptionPane.showMessageDialog(null, "Ruido del canal: " + Canales.ruidoCanal(imagenEntrada, imagenSalida), "", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args){
        frame = new JFrame("Teoría de la información");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        frame.setMinimumSize(new Dimension(930, 600));
        frame.setLocation(FRAME_LOC_X, FRAME_LOC_Y);
        
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH; constraints.weighty = 0d; constraints.weightx = 0d;
        
        JButton boton = new JButton("<html><h1>Abrir imagen</h1></html>");
        boton.setPreferredSize(new Dimension(300, 80));
        boton.addActionListener( (evt) -> abrirArchivo(frame) );
        
        botonCod = new JButton("<html><center><h1>Abrir imagen codificada</h1></center></html>");
        botonCod.setPreferredSize(new Dimension(300, 80));
        botonCod.addActionListener( (evt) -> abrirCod());

        
        frame.getContentPane().setLayout(new GridBagLayout());
        constraints.gridy = 0; constraints.gridx = 0;
        frame.getContentPane().add(boton,constraints);
        
        //Panel vacío para "espaciar"
        constraints.gridy = 1; constraints.ipady = 50;
        frame.getContentPane().add(new JPanel(), constraints);
        
        constraints.gridy = 2; constraints.ipady = 0;
        frame.getContentPane().add(botonCod, constraints);

        boton = new JButton("<html><center><h1>Calcular ruido<br>de un canal</h1></center></html>");
        boton.setPreferredSize(new Dimension(300, 80));
        boton.addActionListener( (evt) -> calcularRuido() );

        //Panel vacío para "espaciar"
        constraints.gridy = 3; constraints.ipady = 50;
        frame.getContentPane().add(new JPanel(), constraints);


        constraints.gridy = 4; constraints.ipady = 0;
        frame.getContentPane().add(boton,constraints);

        
        frame.setVisible(true);
    }
}
