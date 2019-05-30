import java.util.List;

public class BufferWriterArrayBytes {
    private List<Byte> lista;
    private int bufferPos = 0;
    private byte buffer = 0;

    public BufferWriterArrayBytes(List<Byte> lista){this.lista = lista;}

    public void agregarBoolean(boolean b){
        if (b)
            buffer = (byte) ((buffer << 1) | 1);
        else
            buffer = (byte) (buffer << 1);
        bufferPos++;
        if (bufferPos == 8){
            lista.add(buffer);
            buffer = 0;
            bufferPos = 0;
        }
    }

    public void agregarByte(byte b){
        if (bufferPos != 0) { //Si el byte se escribe "a medias" en dos buffers
            buffer = (byte) (buffer << (8 - bufferPos));
            buffer = (byte) (buffer | ((b & 255) >> bufferPos)); //Guardar al final la primera parte del byte a guardar
            lista.add(buffer);

            buffer = (byte) ( (b << (32 - bufferPos)) >> (32 - bufferPos) ); //Guardar al principio la última parte del byte a guardar
        }else{ //Si el color entra entero en un nuevo byte
            lista.add((byte) b);
        }
    }

    public void agregarInt(int i){
        for (byte b: Codificaciones.intToBytes(i)){
            this.agregarByte(b);
        }
    }

    public void finalizarEscritura(){
        if ((bufferPos < 8) && (bufferPos != 0)) { //Si no se llenó el buffer, rellenar
            buffer = (byte) (buffer << (8 - bufferPos));
            lista.add(buffer);
        }
    }


    public byte[] getBytes(){
        byte[] resultado = new byte[lista.size()];
        int i = 0;
        for (Byte b: lista){
            resultado[i] = b;
            i++;
        }
        return resultado;
    }
}
