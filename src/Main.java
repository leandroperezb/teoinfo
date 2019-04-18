import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class Main {
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
                
                
                 ///////CONTAR COLORES/////
                /*int colores[] = new int[256];
                for (int i=0; i<256; i++) 
                	colores[i]= 0;
                
                for(int i=0;i<imagen.getHeight();i++)
                	for(int j=0;j<imagen.getWidth();j++) 
                		colores[imagen.getColor(j, i)]++;
               
                int cantidadColores = 0;
                for (int i=0;i<256;i++)
                	if(colores[i]!=0)
                		cantidadColores++;
                
                System.out.println("Cantidad de colores: "+ cantidadColores);*/
                
                ///FIN CONTAR COLORES //////////////


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
