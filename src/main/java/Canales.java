public class Canales {
    private static double[][] matrizCondicional(Imagen independiente, Imagen condicionada){
        double[][] matriz = new double[256][256];
        double[] sumatorias = new double[256];

        //Inicializar en cero
        for (int i = 0; i < 256; i++){
            sumatorias[i] = 0d;
            for (int j = 0; j < 256; j++){
                matriz[i][j] = 0d;
            }
        }

        //Contar cantidad de dependencias de un color a otro (en las distintas fuentes/imágenes)
        for (int y = 0; y < independiente.getAlto(); y++){
            for (int x = 0; x < independiente.getAncho(); x++){
                int colorIndependiente = independiente.getColor(x, y);
                int colorCondicionado = condicionada.getColor(x, y);
                sumatorias[colorIndependiente]++;
                matriz[colorIndependiente][colorCondicionado]++;
            }
        }

        //Normalizar / hacer probabilidades de las sumatorias
        for (int i = 0; i < 256; i++){
            for (int j = 0; j < 256; j++){
                if (sumatorias[i] != 0d)
                    matriz[i][j] /= sumatorias[i];
            }
        }

        return matriz;
    }

    public static double ruidoCanal(Imagen entrada, Imagen salida){
        double[][] matrizCondicional = matrizCondicional(entrada, salida);
        double ruido = 0d;

        double[] probabilidadesEntrada = entrada.probabilidadesSimples();
        //Sumatoria de P(i) * hi
        for (int i = 0; i < probabilidadesEntrada.length; i++){
            ruido += probabilidadesEntrada[i] * FuenteMarkoviana.entropiaSinMemoria(matrizCondicional[i]);
        }

        return ruido;
    }
}
