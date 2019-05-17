import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.List;

public class Codificaciones {

    public static byte[] intToBytes(int entero){
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (entero >> 24);
        bytes[1] = (byte) ((entero << 8) >> 24);
        bytes[2] = (byte) ((entero << 16) >> 24);
        bytes[3] = (byte) entero;
        return bytes;
    }

    private static byte[] convertBooleanListToByteArray(List<Boolean> input) {
        BufferWriterArrayBytes buffer = new BufferWriterArrayBytes(new ArrayList<>(input.size() / 8));
        for (Boolean b: input){
            buffer.agregarBoolean(b);
        }
        buffer.finalizarEscritura();
        return buffer.getBytes();
    }

    public static byte[] convertByteListToPrimitives(List<Byte> input) {
        byte[] ret = new byte[input.size()];
        int i = 0;
        for (Byte b: input){
            ret[i] = b;
            i++;
        }
        return ret;
    }

    private static double[] getFrecuencias(Imagen img){
        double[] resultado = new double[255];
        for (int i = 0; i < 255; i++){resultado[i] = 0d;}

        Map<Byte, Integer> frecuencias = new TreeMap<>();

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

        for (Map.Entry<Byte, Integer> entry: frecuencias.entrySet()){
            resultado[entry.getKey() & 255] = entry.getValue();
        }

        return resultado;
    }

    private static byte[] frecuenciasCodificadas(Imagen img){
        List<Byte> lista = new ArrayList<>(255*5);
        Map<Byte, Integer> frecuencias = new TreeMap<>();

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

    private static double[] calcularFrecuencias(BufferReaderArrayBytes buffer){
        double[] frecuencias = new double[255];
        for (int i = 0; i < 255; i++){frecuencias[i] = 0d;}
        int cantidad = buffer.leerByte() & 255;
        for (int i = 0; i < cantidad; i++){
            int color = buffer.leerByte() & 255;
            int valor = buffer.leerInt();
            frecuencias[color] = valor;
        }
        return frecuencias;
    }

    private static void decodificarHuffman(BufferedImage img, NodoHuffman raiz, BufferReaderArrayBytes buffer){
        for (int y = 0; y < img.getHeight(); y++){
            for (int x = 0; x < img.getWidth(); x++){
                NodoHuffman nodo = raiz;
                while (nodo.simbolo == null){
                    boolean bit = buffer.leerBoolean();
                    if (bit)
                        nodo = nodo.h2;
                    else
                        nodo = nodo.h1;
                }
                img.setRGB(x, y, (new Color(nodo.simbolo, nodo.simbolo, nodo.simbolo).getRGB()));
            }
        }
    }

    public static List<Imagen> levantarArchivo(String path){
        List<Imagen> imagenes = new ArrayList<>();
        try {
            BufferReaderArrayBytes buffer = new BufferReaderArrayBytes( Files.readAllBytes(new File(path).toPath()), 0 );
            int cantidadBloques = buffer.leerInt();
            for (int i = 0; i < cantidadBloques; i++){
                int width = buffer.leerInt();
                int height = buffer.leerInt();
                BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
                NodoHuffman raizArbol = codificacionHuffman(calcularFrecuencias(buffer)).raiz;
                decodificarHuffman(img, raizArbol, buffer);
                imagenes.add(new Imagen(img));
                buffer.finalizarLecturaDeByte();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return imagenes;
    }

    public static void guardarEnArchivo(String path, List<Imagen> imagenes){
        byte[][] imagenCodificada = new byte[imagenes.size()][];
        byte[][] frecuencias = new byte[imagenes.size()][];
        int contador = 0;
        for (Imagen img: imagenes) {
            imagenCodificada[contador] = convertBooleanListToByteArray(codificarConHuffman(img));
            frecuencias[contador] = frecuenciasCodificadas(img);
            contador++;
        }

        try(FileOutputStream fos = new FileOutputStream(path)) {
            fos.write(intToBytes(imagenes.size()));
            for (int i = 0; i < imagenes.size(); i++) {
                fos.write(intToBytes(imagenes.get(i).getWidth()));
                fos.write(intToBytes(imagenes.get(i).getHeight()));
                fos.write(frecuencias[i]);
                fos.write(imagenCodificada[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static List<Boolean> codificarConHuffman(Imagen img){
        List<Boolean> salida = new ArrayList<>(img.getWidth() * img.getHeight()*2);
        Map<Integer, boolean[]> codificaciones = codificacionHuffman(getFrecuencias(img)).codificaciones;

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
        BufferWriterArrayBytes buffer = new BufferWriterArrayBytes(new ArrayList<>(img.getWidth() * img.getHeight() * 2));

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
                        if (contador == 1) {
                            buffer.agregarBoolean(false); //Indicar que no se codifica con un número de longitud
                            buffer.agregarByte((byte) colorAnterior);
                        } else {
                            buffer.agregarBoolean(true); //Indicar que se codifica con un número de longitud
                            buffer.agregarByte((byte) colorAnterior);
                            buffer.agregarByte((byte) contador);
                        }
                    }
                    contador = 1;
                    colorAnterior = color;
                }
            }
        }

        //La codificación para el último color (sí, hay DUP de código)
        if (colorAnterior > -1) {
            if (contador == 1) {
                buffer.agregarBoolean(false); //Indicar que no se codifica con un número de longitud
                buffer.agregarByte((byte) colorAnterior);
            } else {
                buffer.agregarBoolean(true); //Indicar que se codifica con un número de longitud
                buffer.agregarByte((byte) colorAnterior);
                buffer.agregarByte((byte) contador);
            }
        }

        buffer.finalizarEscritura();

        return buffer.getBytes();
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
