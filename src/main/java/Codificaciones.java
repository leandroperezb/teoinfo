import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.util.*;

public class Codificaciones {

    public static char[] decodeSequence(String path) {
        char[] restoredSequence = null;
        try {
            byte[] inputSequence = Files.readAllBytes(new File(path).toPath());
            int globalIndex = 0;

            //Obtener tamaño del encabezado
            int tamanio = (((int) inputSequence[0]) << 24) | (((int) inputSequence[1]) << 16) | (((int) inputSequence[2]) << 8) | ((int) inputSequence[3]);


            byte mask = (byte) (1 << (8 - 1)); // mask: 10000000
            int bufferPos = 0;
            int i = 4;
            restoredSequence = new char[tamanio * 8];
            while (globalIndex < tamanio*8)
            {
                byte buffer = inputSequence[i];
                while (bufferPos < 8) {

                    if ((buffer & mask) == mask) {
                        restoredSequence[globalIndex] = '1';
                    } else {
                        restoredSequence[globalIndex] = '0';
                    }

                    buffer = (byte) (buffer << 1);
                    bufferPos++;
                    globalIndex++;

                    if (globalIndex == tamanio*8) {
                        break;
                    }
                }
                i++;
                bufferPos = 0;
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        return restoredSequence;
    }

    private static byte[] convertBooleanListToByteArray(List<Boolean> input) {
        List<Byte> lista = new ArrayList<>();
        byte buffer = 0;
        int bufferPos = 0;
        int i = 0;
        Iterator<Boolean> it = input.iterator();
        while (i < input.size()) {
            buffer = (byte) (buffer << 1);
            bufferPos++;
            if (it.next()) {
                buffer = (byte) (buffer | 1);
            }

            if (bufferPos == 8) {
                lista.add(buffer);
                buffer = 0;
                bufferPos = 0;
            }
            i++;
        }

        if ((bufferPos < 8) && (bufferPos != 0)) {
            buffer = (byte) (buffer << (8 - bufferPos));
            lista.add(buffer);
        }


        byte[] ret = new byte[lista.size()];
        for (int j = 0; j < ret.length; j++) {
            ret[j] = lista.get(j);
        }

        return ret;
    }

    public static void guardarEnArchivo(String path, List<Boolean> mensajeCodificado){
        try {
            byte[] byteArray = convertBooleanListToByteArray(mensajeCodificado);
            int tamanioEntero = (int) Math.ceil(mensajeCodificado.size() / 8d);
            byte[] tamanio = new byte[4];
            tamanio[0] = (byte) (tamanioEntero >> 24);
            tamanio[1] = (byte) ((tamanioEntero << 8) >> 24);
            tamanio[2] = (byte) ((tamanioEntero << 16) >> 24);
            tamanio[3] = (byte) ((tamanioEntero << 24) >> 24);

            FileOutputStream fos = new FileOutputStream(path);
            fos.write(tamanio);
            fos.write(byteArray);
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Boolean> codificarConHuffman(Imagen img){
        List<Boolean> salida = new LinkedList<>();
        Map<Integer, boolean[]> codificaciones = codificacionHuffman(img.probabilidadesSimples()).codificaciones;

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int color = img.getColor(x, y);
                boolean[] codificacion = codificaciones.get(color);
                for (int i = 0; i < codificacion.length; i++){
                    salida.add(codificacion[i]);
                }
            }
        }

        return salida;
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

    private static void armarCodificacion(Map<Integer, boolean[]> codificaciones, NodoHuffman nodo, List<Boolean> codificacion){
        if (nodo.simbolo != null){
            boolean[] cod = new boolean[codificacion.size()];
            int i = 0;
            for (Boolean b: codificacion){
                cod[i] = b;
                i++;
            }
            codificaciones.put(nodo.simbolo, cod);
        }else{
            codificacion.add(false);
            armarCodificacion(codificaciones, nodo.h1, codificacion);
            codificacion.remove(codificacion.size() - 1);
            codificacion.add(true);
            armarCodificacion(codificaciones, nodo.h2, codificacion);
            codificacion.remove(codificacion.size() - 1);
        }
    }

    private static class ResultadoCodificacion {
        Map<Integer, boolean[]> codificaciones;
        NodoHuffman raiz;
    }

    public static ResultadoCodificacion codificacionHuffman(double frecuencias[]){
        Map<Integer, boolean[]> codificacion = new HashMap<>();
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
        armarCodificacion(codificacion, raiz, new ArrayList<Boolean>());

        ResultadoCodificacion resultado = new ResultadoCodificacion();
        resultado.codificaciones = codificacion; resultado.raiz = raiz;
        return resultado;
    }
}
