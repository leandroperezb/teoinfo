import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class Codificaciones {

    public static String codificarConHuffman(Imagen img){
        StringBuilder salida = new StringBuilder();
        Map<Integer, String> codificaciones = codificacionHuffman(img.probabilidadesSimples()).codificaciones;

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int color = img.getColor(x, y);
                salida.append(codificaciones.get(color));
            }
        }

        return salida.toString();
    }


    public static String codificarConRLC(Imagen img, int minimoDeCorrida) {
        StringBuilder salida = new StringBuilder();

        int colorAnterior = -1;
        int contador = 1;
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int color = img.getColor(x, y);
                if (color == colorAnterior) {
                    contador++; //Contar cuántas apariciones consecutivas del color hay
                } else {
                    //Si vino un color nuevo, codificar la "corrida" del anterior y resetear contadores
                    if (colorAnterior > -1) {
                        if (contador < minimoDeCorrida) {
                            salida.append("0:"); //Indica que no se codifica con un número de longitud
                            for (int k = 0; k < contador; k++) {
                                //Si son pocas apariciones, directamente escribir el símbolo tantas veces como aparezca
                                salida.append(String.valueOf(colorAnterior) + "|");
                            }
                        } else {
                            //Si son varias las apariciones, codificar como par <símbolo, longitud>
                            salida.append("1:" + String.valueOf(colorAnterior) + "|" + String.valueOf(contador) + "|");
                        }
                    }
                    contador = 1;
                    colorAnterior = color;
                }
            }
        }

        //La codificación para el último color
        if (contador < minimoDeCorrida) {
            salida.append("0:");
            for (int k = 0; k < contador; k++) {
                salida.append(String.valueOf(colorAnterior) + "|");
            }
        } else {
            salida.append("1:" + String.valueOf(colorAnterior) + "|" + String.valueOf(contador) + "|");
        }

        return salida.toString();
    }




    private static class NodoHuffman implements Comparable{
        Double frecuencia; Integer simbolo; NodoHuffman h1; NodoHuffman h2;

        public NodoHuffman(double frecuencia, Integer simbolo){
            this.frecuencia = frecuencia; this.simbolo = simbolo;
            this.h1 = null; this.h2 = null;
        }

        public NodoHuffman(double frecuencia, NodoHuffman h1, NodoHuffman h2){
            this.frecuencia = frecuencia; this.simbolo = null;
            this.h1 = h1; this.h2 = h2;
        }

        @Override
        public int compareTo(Object o) {
            NodoHuffman otro = (NodoHuffman) o;
            return this.frecuencia.compareTo(otro.frecuencia);
        }
    }

    private static void armarCodificacion(Map<Integer, String> codificaciones, NodoHuffman nodo, String codificacion){
        if (nodo.simbolo != null){
            codificaciones.put(nodo.simbolo, codificacion);
        }else{
            armarCodificacion(codificaciones, nodo.h1, codificacion + "0");
            armarCodificacion(codificaciones, nodo.h2, codificacion + "1");
        }
    }

    private static class ResultadoCodificacion {
        Map<Integer, String> codificaciones;
        NodoHuffman raiz;
    }

    public static ResultadoCodificacion codificacionHuffman(double frecuencias[]){
        Map<Integer, String> codificacion = new HashMap<>();
        PriorityQueue<NodoHuffman> colaDeNodosPadres = new PriorityQueue<>();

        //Agregar símbolos existentes con sus frecuencias
        for (int i = 0; i < frecuencias.length; i++){
            if (frecuencias[i] > 0d){
                colaDeNodosPadres.add(new NodoHuffman(frecuencias[i], i));
            }
        }

        //Mientras haya más de un nodo padre, agarrar los dos más pequeños y hacerlos hojas
        while (colaDeNodosPadres.size() > 1){
            NodoHuffman hijo1 = colaDeNodosPadres.poll(); NodoHuffman hijo2 = colaDeNodosPadres.poll();
            NodoHuffman padre = new NodoHuffman(hijo1.frecuencia + hijo2.frecuencia, hijo1, hijo2);
            colaDeNodosPadres.add(padre);
        }

        NodoHuffman raiz = colaDeNodosPadres.peek();
        armarCodificacion(codificacion, raiz, "");

        ResultadoCodificacion resultado = new ResultadoCodificacion();
        resultado.codificaciones = codificacion; resultado.raiz = raiz;
        return resultado;
    }
}
