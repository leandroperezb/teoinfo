public class FuenteMarkoviana {
    protected double[][] probabilidades;
    protected double[] probabilidadesEstacionarias;
    private final int ITERACIONES = 10000;
    static double epsilonEsperanza = 0.00000001d;
    static double epsilonVarianza = 0.00001d;

    public FuenteMarkoviana(double[][] probabilidades){
        this.probabilidades = probabilidades;

        for (int i = 0; i < probabilidades.length; i++){
            for (int j = 0; j < probabilidades.length; j++){
                if (probabilidades[i][j] != 0d){
                    this.probabilidadesEstacionarias = vectorEstacionario(i);
                    return;
                }
            }
        }

        this.probabilidadesEstacionarias = vectorEstacionario(0);
    }

    public FuenteMarkoviana(double[][] probabilidades, double[] probabilidadesEstacionarias){
        this.probabilidades = probabilidades;
        this.probabilidadesEstacionarias = probabilidadesEstacionarias;
    }

    public int darSimbolo(int n){
        if (n >= probabilidades.length || n < 0) throw new IllegalArgumentException("Símbolo dado como argumento fuera de rango");
        double prob = Math.random();
        double contador = 0f;
        for (int i = 0; i < probabilidades.length; i++){
            contador += probabilidades[n][i];
            if (prob < contador)
                return i;
        }
        return probabilidades.length - 1;
    }


    public int darSimbolo(){
        double prob = Math.random();
        double contador = 0f;
        for (int i = 0; i < probabilidadesEstacionarias.length; i++){
            contador += probabilidadesEstacionarias[i];
            if (prob < contador)
                return i;
        }
        return probabilidadesEstacionarias.length - 1;
    }

    protected static boolean converge(double valor1, double valor2, double epsilon){
        return (Math.abs(valor1 - valor2) < epsilon);
    }

    protected static boolean converge(double[] valor1, double[] valor2, double epsilon){
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

        while (pasos < ITERACIONES || !converge(vecAnterior, vector, 0.000001d)){
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

        while (tiradas < ITERACIONES || !converge(esperanzaVieja, esperanza, epsilonEsperanza)){
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

        while (tiradas < ITERACIONES || !converge(varianzaVieja, varianza, epsilonVarianza)){
            tiradas++;
            simboloAnterior = this.darSimbolo(simboloAnterior);
            sumatoriaEsperanza += simboloAnterior;
            sumatoriaVarianza += Math.pow(simboloAnterior - (double) sumatoriaEsperanza / tiradas, 2);

            varianzaVieja = varianza;
            varianza = (double) sumatoriaVarianza / tiradas;
        }

        return varianza;
    }


    public double entropiaConMemoria(){
        double entropia = this.entropiaSinMemoria();

        //Sumar entropía condicional
        for (int i = 0; i < probabilidades.length; i++){
            entropia += probabilidadesEstacionarias[i] * FuenteMarkoviana.entropiaSinMemoria(probabilidades[i]);
        }

        return entropia;
    }



    public static double entropiaSinMemoria(double[] probabilidades){
        double entropia = 0d;

        for (int i = 0; i < probabilidades.length; i++){
            if (probabilidades[i] != 0)
                entropia += probabilidades[i] * Math.log(probabilidades[i]) / Math.log(2d);
        }

        return -entropia;
    }

    public double entropiaSinMemoria(){
        return FuenteMarkoviana.entropiaSinMemoria(this.probabilidadesEstacionarias);
    }
}
