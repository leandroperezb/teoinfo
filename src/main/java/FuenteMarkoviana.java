public class FuenteMarkoviana {
    protected double[][] probabilidades;
    private final int iteraciones = 10000;
    private final double epsilon = 0.000001d;

    public FuenteMarkoviana(double[][] probabilidades){
        this.probabilidades = probabilidades;
    }

    public int darSimbolo(int n){
        if (n >= probabilidades.length || n < 0) throw new IllegalArgumentException("Símbolo dado como argumento fuera de rango");
        double prob = Math.random();
        double contador = 0f;
        for (int i = 0; i < probabilidades.length; i++){
            contador += probabilidades[i][n];
            if (prob < contador)
                return i;
        }
        return probabilidades.length - 1;
    }


    //Una prueba en la que le daba el vector estacionario para obtener los símbolos
    public int darSimbolo(double[] vector){
        double prob = Math.random();
        double contador = 0f;
        for (int i = 0; i < vector.length; i++){
            contador += vector[i];
            if (prob < contador)
                return i;
        }
        return vector.length - 1;
    }

    protected boolean converge(double valor1, double valor2, double epsilon){
        return (Math.abs(valor1 - valor2) < epsilon);
    }

    protected boolean converge(double[] valor1, double[] valor2, double epsilon){
        if (valor1.length != valor2.length) throw new IllegalArgumentException("Los arreglos no coinciden en tamaño");

        for (int i = 0; i < valor1.length; i++){
            if (Math.abs(valor1[i] - valor2[i]) >= epsilon)
                return false;
        }

        return true;
    }

    public double[] vectorEstacionario(int simboloInicial){
        long pasos = 0;
        long[] emisiones = new long[probabilidades.length];
        double[] vecAnterior = new double[probabilidades.length];
        double[] vector = new double[probabilidades.length];
        for (int i = 0; i < probabilidades.length; i++){
            emisiones[i] = 0; vecAnterior[i] = 0d; vector[i] = 0d;
        }

        int simbolo = simboloInicial;

        while (pasos < iteraciones || !converge(vecAnterior, vector, epsilon)){
            simbolo = darSimbolo(simbolo);
            emisiones[simbolo]++;
            pasos++;
            for (int i = 0; i < vector.length; i++){
                vecAnterior[i] = vector[i];
                vector[i] = (double) emisiones[i] / pasos;
            }
        }

        return vector;
    }


    public double esperanza(int simboloInicial){
        long tiradas = 0; long sumatoria = 0;
        double esperanzaVieja = 0d; double esperanza = 0d;
        int simboloAnterior = simboloInicial;

        while (tiradas < iteraciones || !converge(esperanzaVieja, esperanza, 0.00000001d)){
            tiradas++;
            simboloAnterior = this.darSimbolo(simboloAnterior);
            sumatoria += simboloAnterior;

            esperanzaVieja = esperanza;
            esperanza = (double) sumatoria / tiradas;
        }

        return esperanza;
    }


    public double varianza(int simboloInicial){    	
        long tiradas = 0; long sumatoriaEsperanza = 0; long sumatoriaVarianza = 0;
        double varianzaVieja = 0d; double varianza = 0d;
        int simboloAnterior = simboloInicial;

        while (tiradas < iteraciones || !converge(varianzaVieja, varianza, 0.00001d)){
            tiradas++;
            simboloAnterior = this.darSimbolo(simboloAnterior);
            sumatoriaEsperanza += simboloAnterior;
            sumatoriaVarianza += Math.pow(simboloAnterior - (double) sumatoriaEsperanza / tiradas, 2);

            varianzaVieja = varianza;
            varianza = (double) sumatoriaVarianza / tiradas;
        }

        return varianza;
    }
}
