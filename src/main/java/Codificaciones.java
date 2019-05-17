import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.util.*;

public class Codificaciones {

    private static byte[] intToBytes(int entero){
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (entero >> 24);
        bytes[1] = (byte) ((entero << 8) >> 24);
        bytes[2] = (byte) ((entero << 16) >> 24);
        bytes[3] = (byte) entero;
        return bytes;
    }

    //SIN USO DE MOMENTO. CÓDIGO DE EJEMPLO
    /*public static char[] decodeSequence(String path) {
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
    }*/

    private static byte[] convertBooleanListToByteArray(List<Boolean> input) {
        byte buffer = 0;
        int bufferPos = 0;
        int i = 0;
        Iterator<Boolean> it = input.iterator();
        byte[] resultado = new byte[(int) Math.ceil(input.size() / 8d)];
        int resultadoPos = 0;

        while (i < input.size()) {
            buffer = (byte) (buffer << 1);
            bufferPos++;
            if (it.next()) {
                buffer = (byte) (buffer | 1);
            }

            if (bufferPos == 8) {
                resultado[resultadoPos] = buffer;
                resultadoPos++;
                buffer = 0;
                bufferPos = 0;
            }
            i++;
        }

        if ((bufferPos < 8) && (bufferPos != 0)) {
            buffer = (byte) (buffer << (8 - bufferPos));
            resultado[resultadoPos] = buffer;
        }

        return resultado;
    }

    private static byte[] convertByteListToPrimitives(List<Byte> input) {
        byte[] ret = new byte[input.size()];
        int i = 0;
        for (Byte b: input){
            ret[i] = b;
            i++;
        }
        return ret;
    }

    private static byte[] frecuenciasCodificadas(Imagen img){
        List<Byte> lista = new ArrayList<>(255*5);
        Map<Byte, Integer> frecuencias = new HashMap<>();

        for (int y = 0; y < img.getHeight(); y++){
            for (int x = 0; x < img.getWidth(); x++){
                byte color = (byte) img.getColor(x, y);
                if (frecuencias.containsKey(color)){
                    frecuencias.put(color, frecuencias.get(color) + 1);
                }else{
                    frecuencias.put(color, 1);
                }
            }
        }

        lista.add((byte) frecuencias.size()); //Anotar la cantidad de pares <Símbolo, Frecuencia> que hay

        for (Map.Entry<Byte, Integer> entry: frecuencias.entrySet()){
            lista.add(entry.getKey());
            for (byte valor: intToBytes(entry.getValue())){
                lista.add(valor);
            }
        }

        return convertByteListToPrimitives(lista);
    }

    public static void guardarEnArchivo(String path, Imagen img){
        List<Boolean> codificado = codificarConHuffman(img);
        byte[] imagenCodificada = convertBooleanListToByteArray(codificado);
        byte[] frecuencias = frecuenciasCodificadas(img);

        try(FileOutputStream fos = new FileOutputStream(path)) {
            fos.write(intToBytes(img.getWidth()));
            fos.write(intToBytes(img.getHeight()));
            fos.write(frecuencias);
            fos.write(imagenCodificada);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static List<Boolean> codificarConHuffman(Imagen img){
        List<Boolean> salida = new ArrayList<>(img.getWidth() * img.getHeight()*2);
        Map<Integer, boolean[]> codificaciones = codificacionHuffman(img.probabilidadesSimples()).codificaciones;

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int color = img.getColor(x, y);
                boolean[] codificacion = codificaciones.get(color);
                for (boolean b: codificacion){
                    salida.add(b);
                }
            }
        }

        return salida;
    }


    public static byte[] codificarConRLC(Imagen img) {
        List<Byte> salida = new ArrayList<>(img.getWidth() * img.getHeight() * 2);

        int colorAnterior = -1;
        int contador = 1;
        int bufferPos = 0;
        byte buffer = 0;
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int color = img.getColor(x, y);
                if (color == colorAnterior) {
                    contador++; //Contar cuántas apariciones consecutivas del color hay
                } else {
                    //Si vino un color nuevo, codificar la "corrida" del anterior y resetear contadores
                    if (colorAnterior > -1) {
                        if (contador == 1) {
                            buffer = (byte) (buffer << 1); //Indicar que no se codifica con un número de longitud
                            bufferPos++;

                            if (bufferPos == 8) { //Si se llenó el buffer, guardar
                                salida.add(buffer);
                                buffer = 0;
                                bufferPos = 0;
                            }

                            if (bufferPos != 0) { //Si el color se escribe "a medias" en dos bytes
                                buffer = (byte) (buffer << (8 - bufferPos));
                                buffer = (byte) (buffer | (colorAnterior >> bufferPos)); //Guardar al final la primera parte del color
                                salida.add(buffer);

                                buffer = (byte) ( (colorAnterior << (32 - bufferPos)) >> (32 - bufferPos) ); //Guardar al principio la última parte del color
                            }else{ //Si el color entra entero en un nuevo byte
                                salida.add((byte) colorAnterior);
                            }

                        } else {
                            buffer = (byte) ((buffer << 1) | 1); //Indicar que se codifica con un número de longitud
                            bufferPos++;

                            if (bufferPos == 8) { //Si se llenó el buffer, guardar
                                salida.add(buffer);
                                buffer = 0;
                                bufferPos = 0;
                            }

                            if (bufferPos != 0) { //Si el color se escribe "a medias" en dos bytes
                                buffer = (byte) (buffer << (8 - bufferPos));
                                buffer = (byte) (buffer | (colorAnterior >> bufferPos)); //Guardar al final la primera parte del color
                                salida.add(buffer);

                                buffer = (byte) ( (colorAnterior << (32 - bufferPos)) >> (32 - bufferPos) ); //Guardar al principio la última parte del color
                                buffer = (byte) (buffer << (8 - bufferPos));
                                buffer = (byte) (buffer | (contador >> bufferPos)); //Guardar al final la primera parte del contador
                                salida.add(buffer);

                                buffer = (byte) ( (contador << (32 - bufferPos)) >> (32 - bufferPos) ); //Guardar al principio la última parte del contador
                            }else{
                                //Si el color entra entero en un nuevo byte
                                salida.add((byte) colorAnterior);
                                salida.add((byte) contador);
                            }
                        }
                    }
                    contador = 1;
                    colorAnterior = color;
                }
            }
        }

        //La codificación para el último color (sí, hay DUP de código)
        if (contador == 1) {
            buffer = (byte) (buffer << 1); //Indicar que no se codifica con un número de longitud
            bufferPos++;

            if (bufferPos == 8) { //Si se llenó el buffer, guardar
                salida.add(buffer);
                buffer = 0;
                bufferPos = 0;
            }

            if (bufferPos != 0) { //Si el color se escribe "a medias" en dos bytes
                buffer = (byte) (buffer << (8 - bufferPos));
                buffer = (byte) (buffer | (colorAnterior >> bufferPos)); //Guardar al final la primera parte del color
                salida.add(buffer);

                buffer = (byte) ( (colorAnterior << (32 - bufferPos)) >> (32 - bufferPos) ); //Guardar al principio la última parte del color
            }else{ //Si el color entra entero en un nuevo byte
                salida.add((byte) colorAnterior);
            }

        } else {
            buffer = (byte) ((buffer << 1) | 1); //Indicar que se codifica con un número de longitud
            bufferPos++;

            if (bufferPos == 8) { //Si se llenó el buffer, guardar
                salida.add(buffer);
                buffer = 0;
                bufferPos = 0;
            }

            if (bufferPos != 0) { //Si el color se escribe "a medias" en dos bytes
                buffer = (byte) (buffer << (8 - bufferPos));
                buffer = (byte) (buffer | (colorAnterior >> bufferPos)); //Guardar al final la primera parte del color
                salida.add(buffer);

                buffer = (byte) ( (colorAnterior << (32 - bufferPos)) >> (32 - bufferPos) ); //Guardar al principio la última parte del color
                buffer = (byte) (buffer << (8 - bufferPos));
                buffer = (byte) (buffer | (contador >> bufferPos)); //Guardar al final la primera parte del contador
                salida.add(buffer);

                buffer = (byte) ( (contador << (32 - bufferPos)) >> (32 - bufferPos) ); //Guardar al principio la última parte del contador
            }else{
                //Si el color entra entero en un nuevo byte
                salida.add((byte) colorAnterior);
                salida.add((byte) contador);
            }
        }

        if ((bufferPos < 8) && (bufferPos != 0)) { //Si no se llenó el buffer, rellenar
            buffer = (byte) (buffer << (8 - bufferPos));
        }
        salida.add(buffer);

        return convertByteListToPrimitives(salida);
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
