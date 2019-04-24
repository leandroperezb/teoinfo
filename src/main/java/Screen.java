import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.IntStream;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Screen extends JPanel implements Runnable{
	private static List<Imagen> imagenes;
	public static Point mseOver = new Point(0, 0);
	public static Point mseClick = new Point(-1, -1);
	private static Screen sc; //Workaround para entrar a la instancia desde los métodos estáticos que estaban definidos
	private Thread thread = new Thread(this);
	private static Thread threadEsperanza = new Thread();
	private static Thread threadVarianza = new Thread();
	public static Semaphore sem = new Semaphore(0);
	private static int bloqueSeleccionado = -1;
    private static List<Boton> botones = new ArrayList<>();
    private static int numIm = 0;
    private int imagenWidth;
    private static Imagen img = null;
    private final Color ColorMayorEntropia = Color.GREEN;
    private final Color ColorMenorEntropia = Color.BLUE;
	private final Color ColorPromedioEntropia = Color.ORANGE;
    private Imagen imagen;

    private static double esperanzaAMostrar = Double.NEGATIVE_INFINITY;
	private static double varianzaAMostrar = Double.NEGATIVE_INFINITY;

	public Screen(Imagen imagen, JFrame f) {
		Screen.sc = this; this.imagen = imagen;
		f.addMouseListener(new KeyHandel());
		f.addMouseMotionListener(new KeyHandel());

		thread.start();

		imagenWidth = imagen.getWidth();
        
		imagenes = imagen.obtenerCuadrantes();
		
        //inicializar botones
        for(int j=0;j<imagen.getHeight()/Imagen.TAMANIOBLOQUECUADRANTE;j++)
        	for(int i=0;i<imagen.getWidth()/Imagen.TAMANIOBLOQUECUADRANTE;i++){
        		botones.add(new Boton(i*(imagen.TAMANIOBLOQUECUADRANTE/4),j*(Imagen.TAMANIOBLOQUECUADRANTE/4),Imagen.TAMANIOBLOQUECUADRANTE,Imagen.TAMANIOBLOQUECUADRANTE));
        	}
        
        double entropiaMax = Double.NEGATIVE_INFINITY;
        int posEntropiaMax=0;
        double entropiaMin = Double.POSITIVE_INFINITY;
        int posEntropiaMin=0;

        //cargar imagen en botones
		double sumatoriaEntropia = 0d;
        for(int i=0;i<botones.size();i++) {
        	botones.get(i).addImage(imagenes.get(i));
        	double entropia = imagenes.get(i).entropiaSimple();
			sumatoriaEntropia += entropia;
        	
        	if(entropia < entropiaMin) {
        		entropiaMin = entropia;
        		posEntropiaMin = i;
        	}
        	
        	if(entropia > entropiaMax) {
        		entropiaMax= entropia;
        		posEntropiaMax = i;
        	}
        }

		double entropiaPromedio = sumatoriaEntropia / botones.size();

        double entropiaMasCercanaAlPromedio = imagenes.stream().map(Imagen::entropiaSimple)
				.min(Comparator.comparingDouble((i) -> Math.abs(i - entropiaPromedio))).get();

		int posEntropiaPromedio = IntStream.range(0, imagenes.size())
				.filter(i -> imagenes.get(i).entropiaSimple() == entropiaMasCercanaAlPromedio)
				.limit(1).sum();


		botones.get(posEntropiaPromedio).remarcar(true, ColorPromedioEntropia);
        botones.get(posEntropiaMin).remarcar(true, ColorMenorEntropia);
        botones.get(posEntropiaMax).remarcar(true, ColorMayorEntropia);
	}

	//dibujo sobre el frame
	public synchronized void paintComponent(Graphics g) {
		g.clearRect(0, 0, getWidth(), getHeight());
		for(int i=0;i<botones.size();i++) {
			botones.get(i).paintComponent(g);
		}

		if (img!= null) {
			final int yInicial = 65;
            g.setColor(Color.black);
			g.drawString(" Datos de imagen: "+numIm , imagenWidth/4+20, yInicial);


            if (esperanzaAMostrar == Double.NEGATIVE_INFINITY){
				g.drawString(" Esperanza: calculando..." , imagenWidth/4+20, yInicial+20);
			}else {
				g.drawString(" Esperanza: " + esperanzaAMostrar, imagenWidth / 4 + 20, yInicial + 20);
			}

            if (varianzaAMostrar == Double.NEGATIVE_INFINITY){
				g.drawString(" Varianza: calculando...", imagenWidth / 4 + 20, yInicial + 40);
				g.drawString(" Desvío: calculando... ", imagenWidth / 4 + 20, yInicial + 60);
			}else {
				g.drawString(" Varianza: " + varianzaAMostrar, imagenWidth / 4 + 20, yInicial + 40);
				g.drawString(" Desvío: " + Math.sqrt(varianzaAMostrar), imagenWidth / 4 + 20, yInicial + 60);
			}
			g.drawString(" Entropía sin memoria: "+img.entropiaSimple() , imagenWidth/4+20, yInicial+80);
		}

		
		g.setColor(ColorMayorEntropia);
		g.drawString(" ---Mayor Entropía--- ", imagenWidth/4+20, 20);
		g.setColor(ColorMenorEntropia);
		g.drawString(" ---Menor Entropía--- ", imagenWidth/4+20, 35);
		g.setColor(ColorPromedioEntropia);
		g.drawString(" ---Entropía promedio--- ", imagenWidth/4+20, 50);
		
	}


	public static void setOverMse(Point point) {
		mseOver = point;
		Imagen imagen = Screen.sc.imagen;

		//Si el mouse entra a un bloque que no era el que se encontraba seleccionado anteriormente, redibujar
		if (mseOver.getX() < imagen.getWidth()/4d && mseOver.getY() < imagen.getHeight()/4d){
			int cantCol = imagen.getWidth() / Imagen.TAMANIOBLOQUECUADRANTE;
			int y = (int) Math.ceil(mseOver.getY() / (Imagen.TAMANIOBLOQUECUADRANTE/4d));
			int x = (int) Math.ceil(mseOver.getX() / (Imagen.TAMANIOBLOQUECUADRANTE/4d));

			int bloque = (y-1)*cantCol+x-1;
			if (bloque != bloqueSeleccionado){
				bloqueSeleccionado = bloque; Screen.sc.repaint();
			}
		}
	}

	public static void setMseClick(Point point) {
		mseClick = point; Imagen imagen = Screen.sc.imagen;
		if (mseOver.getX() < imagen.getWidth()/4d && mseOver.getY() < imagen.getHeight()/4d){
			for(int i=0;i<botones.size();i++) {
				if(botones.get(i).contains(mseClick)) {
					int imagenAnalizada = i+1;
					JFrame frame = new JFrame("Imagen analizada "+imagenAnalizada);

					if (threadEsperanza.isAlive()){
						//Si la ejecución anterior no terminó, la aborto y reseteo por las dudas el valor
						//que la imagen haya guardado, dado que podría haberse guardado un valor no válido
						//producto del corte repentino
						threadEsperanza.stop(); img.resetSprnz();
					}

					if (threadVarianza.isAlive()){
						//Lo mismo que con el thread de la esperanza, pero para el cálculo de la varianza
						threadVarianza.stop(); img.resetVrnz();
					}

					//Hice click en un botón, por lo que quiero calcular las estadísticas para otro bloque,
					//con lo que reseteo los valores que está mostrando la GUI actualmente
					Screen.esperanzaAMostrar = Double.NEGATIVE_INFINITY; Screen.varianzaAMostrar = Double.NEGATIVE_INFINITY;

					Screen.img = imagenes.get(i);

					threadEsperanza = new Thread(() -> {
						Screen.esperanzaAMostrar = Screen.img.esperanza();
						Screen.sem.release(); //Indico que hay una novedad para repintar el JPanel
					});
					threadVarianza = new Thread(() -> {
						Screen.varianzaAMostrar = Screen.img.varianza();
						Screen.sem.release(); //Indico que hay una novedad para repintar el JPanel
					});
					threadEsperanza.start(); threadVarianza.start();

	                frame.setSize(800,700);
	                frame.setLocation(350,250);
	                
	                new Thread( () -> {
	                	Main.mostrarHistograma(frame, Main.hacerDataset(img), imagenAnalizada);
	                }).start();
	                
	                numIm = i+1;
	                break;
				}
			}
			Screen.sc.repaint();
		}
	}

	@Override
	public void run() {
		while (true){
			try {
				sem.acquire();
				Screen.sc.repaint();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}
}
