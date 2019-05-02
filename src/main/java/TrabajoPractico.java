import java.io.*;

public class TrabajoPractico {
    public static void incisoA(String directorio){
        BufferedWriter output = null;
        try {
            output = new BufferedWriter(new FileWriter(directorio + "/salida iniciso A.txt"));

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


    public static void incisoC(String directorio){
        directorio = directorio + "/inciso C - ";
        cargarMatrizEnArchivo(directorio + "Bloque de mayor entropía.txt", Screen.posEntropiaMayor);
        cargarMatrizEnArchivo(directorio + "Bloque de menor entropía.txt", Screen.posEntropiaMenor);
    }


    public static void incisoD(String directorio){
        BufferedWriter output = null;
        try {
            output = new BufferedWriter(new FileWriter(directorio + "/salida iniciso D.txt"));
            double[] desvios = new double[Screen.imagenes.size()];
            double[] esperanzas = new double[Screen.imagenes.size()];

            int cantCores = Runtime.getRuntime().availableProcessors();

            int cantPorThread = Screen.imagenes.size() / cantCores;

            Thread[] threads = new Thread[cantCores];

            int init = 0;
            for (int i = 0; i < cantCores - 1; i++){
                final int inicio = init;
                threads[i] = new Thread( () -> {
                    for (int j = inicio; j < inicio + cantPorThread; j++){
                        desvios[j] = Screen.imagenes.get(j).desvio();
                        esperanzas[j] = Screen.imagenes.get(j).esperanza();
                    }
                });
                threads[i].start();
                init += cantPorThread;
            }
            final int inicio = init;
            threads[cantCores - 1] = new Thread( () -> {
                for (int i = inicio; i < Screen.imagenes.size(); i++){
                    desvios[i] = Screen.imagenes.get(i).desvio();
                    esperanzas[i] = Screen.imagenes.get(i).esperanza();
                }
            });
            threads[cantCores - 1].start();

            for (int i = 0; i < threads.length; i++){
                threads[i].join();
            }


            //Lo que realmente había que hacer
            for (int i = 0; i < Screen.imagenes.size(); i++){ //Por cada bloque de la imagen
                output.write("Bloque " + (i+1) + "\n");
                output.write("Desvío: " + Screen.imagenes.get(i).desvio() + "\n");
                output.write("Valor medio: " + Screen.imagenes.get(i).esperanza() + "\n");
                output.write("\n\n");
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
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
    }
}
