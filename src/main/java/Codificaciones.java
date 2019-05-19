import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.List;

public class Codificaciones {
    static double UMBRAL = 4d;

    public static Imagen levantarArchivo(String path){
        Imagen imagenDecodificada = null;
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(new File(path).toPath());
        } catch (IOException e) {
            e.printStackTrace();
            return imagenDecodificada;
        }

        BufferReaderArrayBytes buffer = new BufferReaderArrayBytes(bytes, 0);

        //Leer dimensiones de la imagen
        int widthImagen = buffer.leerInt();
        int heightImagen = buffer.leerInt();

        //Las dimensiones de cada bloque (salvo quizás algún borde)
        int sizeBloque = buffer.leerInt();

        int cantidadBloques = (int) Math.ceil((double) widthImagen / sizeBloque) * (int) Math.ceil((double) heightImagen / sizeBloque);

        //Crear una nueva imagen de esas dimensiones
        BufferedImage img = new BufferedImage(widthImagen, heightImagen, BufferedImage.TYPE_BYTE_GRAY);
        imagenDecodificada = new Imagen(img);

        //Leer las codificaciones utilizadas en cada bloque
        boolean[] codificadoConHuffman = new boolean[cantidadBloques];
        for (int i = 0; i < cantidadBloques; i++){
            codificadoConHuffman[i] = buffer.leerBoolean();
        }
        buffer.finalizarLecturaDeByte();

        int x = 0; int y = 0; //Coordenadas para saber en qué lugar de la imagen se está escribiendo cada bloque

        //Por cada bloque de imagen
        for (int i = 0; i < cantidadBloques; i++){
            //Obtener dimensiones del bloque actual
            int width = Math.min(sizeBloque, widthImagen - x);
            int height = Math.min(sizeBloque, heightImagen - y);

            //Decodificar con el método que corresponda
            if (codificadoConHuffman[i]) {
                NodoHuffman raizArbol = codificacionHuffman(decodificarFrecuencias(buffer)).raiz;
                decodificarConHuffman(imagenDecodificada, x, y, width, height, raizArbol, buffer);
            }else{
                decodificarConRLC(imagenDecodificada, x, y, width, height, buffer);
            }
            buffer.finalizarLecturaDeByte();

            //Escribir el próximo bloque al lado del actual
            x += width;
            //Si la coordenada x supera al ancho de la imagen entera, escribir el próximo bloque en la siguiente fila
            if (x >= widthImagen){
                x = 0; y+= height;
            }
        }


        return imagenDecodificada;
    }

    public static void guardarEnArchivo(String path, Imagen imagen){
        List<Imagen> imagenes = imagen.obtenerCuadrantes();
        byte[][] imagenCodificada = new byte[imagenes.size()][];
        byte[][] frecuencias = new byte[imagenes.size()][];

        BufferWriterArrayBytes codificadosConHuffman = new BufferWriterArrayBytes(new ArrayList<>());

        int contador = 0;
        for (Imagen img: imagenes) {
            //Arbitrariamente, se elige usar Huffman si la entropía de la imagen supera un determinado umbral
            if (img.entropiaConMemoria() > UMBRAL) {
                imagenCodificada[contador] = codificarConHuffman(img);
                frecuencias[contador] = codificarFrecuencias(img);
                //Indicar que este bloque se codificó con Huffman
                codificadosConHuffman.agregarBoolean(true);
            }else{
                imagenCodificada[contador] = codificarConRLC(img);
                //Indicar que este bloque no se codificó con Huffman (fue con RLC)
                codificadosConHuffman.agregarBoolean(false);
            }
            contador++;
        }
        codificadosConHuffman.finalizarEscritura();


        try(FileOutputStream fos = new FileOutputStream(path)) {
            //Escribir las dimensiones de la imagen
            fos.write(intToBytes(imagen.getWidth()));
            fos.write(intToBytes(imagen.getHeight()));

            //Escribir las dimensiones de cada bloque
            fos.write(intToBytes(Imagen.TAMANIOBLOQUECUADRANTE));

            /*Escribir las codificaciones utilizadas para cada bloque (una serie de bits en los que cada uno indica la
              codificación utilizada para el bloque correspondiente a su posición)*/
            fos.write(codificadosConHuffman.getBytes());

            //Por cada bloque de la imagen
            for (int i = 0; i < imagenes.size(); i++) {
                //Si se codificó con Huffman, guardar la lista de las frecuencias de apariciones de los símbolos
                if (imagenes.get(i).entropiaConMemoria() > UMBRAL) {
                    fos.write(frecuencias[i]);
                }
                fos.write(imagenCodificada[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] intToBytes(int entero){
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (entero >> 24);
        bytes[1] = (byte) ((entero << 8) >> 24);
        bytes[2] = (byte) ((entero << 16) >> 24);
        bytes[3] = (byte) entero;
        return bytes;
    }


    private static int[] getFrecuencias(Imagen img){
        int[] resultado = new int[256];
        for (int i = 0; i < 256; i++){resultado[i] = 0;}

        Map<Byte, Integer> frecuencias = getMapaFrecuencias(img);

        for (Map.Entry<Byte, Integer> entry: frecuencias.entrySet()){
            resultado[entry.getKey() & 255] = entry.getValue();
        }

        return resultado;
    }

    private static byte[] codificarFrecuencias(Imagen img){
        BufferWriterArrayBytes buffer = new BufferWriterArrayBytes(new ArrayList<>(256*5));
        Map<Byte, Integer> frecuencias = getMapaFrecuencias(img);

        buffer.agregarByte((byte) frecuencias.size()); //Anotar la cantidad de pares <Símbolo, Frecuencia> que hay

        for (Map.Entry<Byte, Integer> entry: frecuencias.entrySet()){
            buffer.agregarByte(entry.getKey()); //Agregar la intensidad de color
            buffer.agregarInt(entry.getValue()); //Agregar la cantidad de apariciones
        }

        buffer.finalizarEscritura();

        return buffer.getBytes();
    }

    private static Map<Byte, Integer> getMapaFrecuencias(Imagen img) {
        Map<Byte, Integer> frecuencias = new TreeMap<>();

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                byte color = (byte) img.getColor(x, y);
                if (frecuencias.containsKey(color)) {
                    frecuencias.put(color, frecuencias.get(color) + 1);
                } else {
                    frecuencias.put(color, 1);
                }
            }
        }
        return frecuencias;
    }

    private static int[] decodificarFrecuencias(BufferReaderArrayBytes buffer){
        int[] frecuencias = new int[256];
        for (int i = 0; i < 256; i++){frecuencias[i] = 0;}
        int cantidad = buffer.leerByte() & 255;
        for (int i = 0; i < cantidad; i++){
            int color = buffer.leerByte() & 255;
            int valor = buffer.leerInt();
            frecuencias[color] = valor;
        }
        return frecuencias;
    }

    private static void decodificarConHuffman(Imagen img, int xInicial, int yInicial, int width, int height, NodoHuffman raiz, BufferReaderArrayBytes buffer){
        //Para cada pixel de la imagen
        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
                //Pararse en la raíz del árbol de codificaciones obtenido anteriormente
                NodoHuffman nodo = raiz;
                while (nodo.simbolo == null){ //Mientras no se haya llegado a un nodo hoja (a un símbolo concreto)

                    //Leer el siguiente bit y moverse a derecha o a izquierda en el árbol de acuerdo al valor de éste
                    boolean bit = buffer.leerBoolean();
                    if (bit)
                        nodo = nodo.h2;
                    else
                        nodo = nodo.h1;
                }

                //Cuando se haya alcanzado un nodo hoja (se haya salido del while) pintar el pixel con el valor del símbolo
                img.setColor(x + xInicial, y + yInicial, nodo.simbolo);
            }
        }
    }


    public static byte[] codificarConHuffman(Imagen img){
        BufferWriterArrayBytes buffer = new BufferWriterArrayBytes(new ArrayList<>(img.getWidth() * img.getHeight()*2));

        //Obtener el mapa de codificaciones de símbolos correspondiente a las frecuencias de la imagen
        Map<Integer, boolean[]> codificaciones = codificacionHuffman(getFrecuencias(img)).codificaciones;

        //Codificar cada símbolo con su codificación correspondiente
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int color = img.getColor(x, y);
                boolean[] codificacion = codificaciones.get(color);
                for (boolean b: codificacion){
                    buffer.agregarBoolean(b);
                }
            }
        }

        buffer.finalizarEscritura();

        return buffer.getBytes();
    }


    private static void decodificarConRLC(Imagen img, int xInicial, int yInicial, int width, int height, BufferReaderArrayBytes buffer){
        int[] pixeles = new int[width * height];
        int pos = 0;
        while (pos < pixeles.length){ //Mientras falten pixeles por cargar
            boolean hayPar = buffer.leerBoolean();
            if (hayPar){
                //Si está codificado como par <Símbolo, Cantidad>, leer los dos bytes del par
                int color = buffer.leerByte() & 255;
                int cantidad = buffer.leerByte() & 255;
                for (int i = 0; i < cantidad; i++){
                    //Añadir tantos píxeles consecutivos del mismo color como indique la cantidad
                    pixeles[pos] = color;
                    pos++;
                }
            }else{
                //Si no se codificó como par, simplemente tomar el próximo byte con el color y cargarlo al pixel
                pixeles[pos] = buffer.leerByte() & 255;
                pos++;
            }
        }

        pos = 0;
        //Pintar la imagen con los pixeles decodificados
        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
                img.setColor(x + xInicial, y + yInicial, pixeles[pos]);
                pos++;
            }
        }
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
                    if (colorAnterior > -1)
                        agregarNuevaCorrida(buffer, colorAnterior, contador);
                    contador = 1;
                    colorAnterior = color;
                }
            }
        }

        //La codificación para el último color
        agregarNuevaCorrida(buffer, colorAnterior, contador);

        buffer.finalizarEscritura();

        return buffer.getBytes();
    }

    private static void agregarNuevaCorrida(BufferWriterArrayBytes buffer, int colorAnterior, int contador) {
        if (contador == 1) {
            buffer.agregarBoolean(false); //Indicar que no se codifica con un número de longitud
            buffer.agregarByte((byte) colorAnterior);
        } else {
            buffer.agregarBoolean(true); //Indicar que se codifica con un número de longitud
            buffer.agregarByte((byte) colorAnterior);
            buffer.agregarByte((byte) contador);
        }
    }


    private static class NodoHuffman implements Comparable{
        Integer frecuencia; Integer simbolo; NodoHuffman h1; NodoHuffman h2;

        public NodoHuffman(int frecuencia, Integer simbolo){
            this.frecuencia = frecuencia; this.simbolo = simbolo;
            this.h1 = null; this.h2 = null;
        }

        public NodoHuffman(int frecuencia, NodoHuffman h1, NodoHuffman h2){
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
            //Si el nodo es una hoja, agregar su codificación al mapa de codificaciones
            boolean[] cod = new boolean[codificacion.size()];
            int i = 0;
            for (Boolean b: codificacion){
                cod[i] = b;
                i++;
            }
            codificaciones.put(nodo.simbolo, cod);
        }else{
            //Si el nodo es un "nodo padre", visitar a los hijos

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

    public static ResultadoCodificacion codificacionHuffman(int frecuencias[]){
        Map<Integer, boolean[]> codificacion = new HashMap<>();
        PriorityQueue<NodoHuffman> colaDeNodosPadres = new PriorityQueue<>();

        //Agregar símbolos existentes con sus frecuencias
        for (int i = 0; i < frecuencias.length; i++){
            if (frecuencias[i] > 0){
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
