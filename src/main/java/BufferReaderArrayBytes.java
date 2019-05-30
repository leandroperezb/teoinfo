public class BufferReaderArrayBytes {
    private byte[] entrada;
    private int bytePos = 0;
    private int arrayPos;
    public BufferReaderArrayBytes(byte[] entrada, int pos){this.entrada = entrada; this.arrayPos = pos;}

    public boolean leerBoolean(){
        byte b = entrada[arrayPos];
        b = (byte) ((b << bytePos) & 128);
        boolean resultado = (b == (byte) 128);
        bytePos++;
        if (bytePos == 8){
            arrayPos++;
            bytePos = 0;
        }
        return resultado;
    }

    public byte leerByte(){
        byte resultado;
        if (bytePos != 0) { //Si el byte está guardado "a medias" en dos bytes
            resultado = (byte) (entrada[arrayPos] << bytePos); //Obtener la primera parte del byte

            arrayPos++;

            resultado = (byte) (resultado | ((entrada[arrayPos] & 255) >> (8 - bytePos)) ); //Obtener la última parte
        }else{ //Si el byte está guardado en forma entera
            resultado = entrada[arrayPos];
            arrayPos++;
        }
        return resultado;
    }

    public int leerInt(){
        int resultado = 0;
        for (int i = 0; i < 4; i++){
            byte b = this.leerByte();
            resultado = ((resultado << 8) | (b & 255));
        }
        return resultado;
    }

    public void finalizarLecturaDeByte(){
        if (bytePos != 0) {
            arrayPos++;
            bytePos = 0;
        }
    }

}
