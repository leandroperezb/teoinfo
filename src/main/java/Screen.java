import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Screen extends JPanel implements Runnable{
	static int ESCALA_IMAGEN = 4;
	static List<Imagen> imagenes;
	static Point mseOver;
	private static Point mseClick;
	static Screen sc = null; //Workaround para entrar a la instancia desde los métodos estáticos que estaban definidos
	private Thread thread;
	private static Thread threadEsperanza = new Thread();
	private static Thread threadDesvio = new Thread();
	private static Semaphore sem;
	static int bloqueSeleccionado;
    private static List<Boton> botones;

    private static int numIm = 0;
    private int imagenWidth;
    private static Imagen img;
    private final Color ColorMayorEntropia = Color.GREEN;
    private final Color ColorMenorEntropia = Color.BLUE;
	private final Color ColorPromedioEntropia = Color.ORANGE;
    private Imagen imagen;
    private static JFrame frame;

    private static double esperanzaAMostrar;
	private static double desvioAMostrar;

	static int posEntropiaMayor;
	static int posEntropiaMenor;
	static int posEntropiaPromedio;
	
	private final static int FRAME_WIDTH = 800;
    private final static int FRAME_HEIGHT = 700;
    private final static int FRAME_LOC_X = 500;
    private final static int FRAME_LOC_Y = 30;
    
    private int espaceX = 20;
    private int espaceY = 20;
    private Font fontRefence = new Font("referencia", Font.BOLD, 16);
    private Font fontDat = new Font("datos", Font.BOLD, 12);

	public Screen(Imagen imagen, JFrame f) {
		Screen.sc = this;
		this.setOpaque(false);

		frame = f;
		sem = new Semaphore(0);
		thread = new Thread(this);
		thread.start();

        
		reset(imagen);

		f.addMouseListener(new KeyHandel());
		f.addMouseMotionListener(new KeyHandel());
	}

	void onNuevosEpsilons(){
		frenarThreads();
		for (Imagen i : imagenes){
			i.resetVrnz(); i.resetSprnz(); i.resetDvio();
		}
		esperanzaAMostrar = Double.NEGATIVE_INFINITY;
		desvioAMostrar = Double.NEGATIVE_INFINITY;
		img = null;
		repaint();
	}

	private void encontrarPosicionesDeEntropias(){
		double entropiaMax = Double.NEGATIVE_INFINITY;
		double entropiaMin = Double.POSITIVE_INFINITY;
		double menorCercaniaAlPromedio = Double.POSITIVE_INFINITY;

		double entropiaPromedio = imagenes.stream().collect(Collectors.averagingDouble(Imagen::entropiaConMemoria));

		for(int i=0;i<imagenes.size();i++) {
			double entropia = imagenes.get(i).entropiaConMemoria();

			if(entropia < entropiaMin) {
				entropiaMin = entropia;
				posEntropiaMenor = i;
			}
			if(entropia > entropiaMax) {
				entropiaMax = entropia;
				posEntropiaMayor = i;
			}

			double cercania = Math.abs(entropia - entropiaPromedio);
			if (cercania < menorCercaniaAlPromedio){
				menorCercaniaAlPromedio = cercania;
				posEntropiaPromedio = i;
			}
		}
	}
	
	public void reset(Imagen imagen) {
		frenarThreads();

		this.imagen = imagen;
		imagenWidth = imagen.getWidth();
		imagenes = imagen.obtenerCuadrantes();
		img = null;
		bloqueSeleccionado = Integer.MIN_VALUE;
		esperanzaAMostrar = Double.NEGATIVE_INFINITY;
		desvioAMostrar = Double.NEGATIVE_INFINITY;


		mseOver = new Point(0, 0);
		mseClick = new Point(-1, -1);

		encontrarPosicionesDeEntropias();

		botones = new ArrayList<>();
		inicializarBotones();
	}

	private static void frenarThreads() {
		if (threadEsperanza.isAlive()) {
			//Si la ejecución anterior no terminó, la aborto y reseteo por las dudas el valor
			//que la imagen haya guardado, dado que podría haberse guardado un valor no válido
			//producto del corte repentino
			threadEsperanza.stop();
			img.resetSprnz();
		}

		if (threadDesvio.isAlive()) {
			//Lo mismo que con el thread de la esperanza, pero para el cálculo del desvío
			threadDesvio.stop();
			img.resetDvio();
		}
	}

	protected void recalcularEscala(){
		double heightLibre = this.getHeight(); double widthLibre = this.getWidth() - 300d;
		double width = imagen.getWidth(); double height = imagen.getHeight();
		double factor = 0d;
		if (width > widthLibre){
			factor = widthLibre / width;
			height = height * factor;
			if (height > heightLibre){
				factor *= heightLibre / height;
			}
		}else{
			factor = heightLibre / height;
			width = width * factor;
			if (width > widthLibre){
				factor *= widthLibre / width;
			}
		}

		int escala = (int) Math.ceil(1/factor);
		if (ESCALA_IMAGEN != escala){
			ESCALA_IMAGEN = escala;
			inicializarBotones();
		}
	}

	private void inicializarBotones() {
		botones.clear();
		int contImagenes = 0;
		for(int j = 0; j< imagen.getHeight(); j += Imagen.TAMANIOBLOQUECUADRANTE)
			for(int i = 0; i< imagen.getWidth(); i += Imagen.TAMANIOBLOQUECUADRANTE){
				botones.add(new Boton(i/ ESCALA_IMAGEN,
						j/ ESCALA_IMAGEN,
						imagenes.get(contImagenes).getWidth()/ ESCALA_IMAGEN,
						imagenes.get(contImagenes).getHeight()/ ESCALA_IMAGEN));

				contImagenes++;
			}


		//cargar imagen en botones
		for(int i=0;i<botones.size();i++) {
			botones.get(i).addImage(imagenes.get(i));
		}

		botones.get(posEntropiaPromedio).remarcar(true, ColorPromedioEntropia);
		botones.get(posEntropiaMenor).remarcar(true, ColorMenorEntropia);
		botones.get(posEntropiaMayor).remarcar(true, ColorMayorEntropia);

	}

	//dibujo sobre el frame
	public synchronized void paintComponent(Graphics g) {
		super.paintComponent(g);
		recalcularEscala();

		g.setColor(Color.GRAY); g.fillRect(getWidth()-6, 0, 2, getHeight());
		for(int i=0;i<botones.size();i++) {
			botones.get(i).paintComponent(g);
		}


		if (img!= null) {
			final int yInicial = 100;
            g.setColor(Color.black);
            g.setFont(fontRefence);
			g.drawString(" Datos de imagen: "+numIm , imagenWidth/ ESCALA_IMAGEN +espaceX, yInicial);

			g.setFont(fontDat);
            if (esperanzaAMostrar == Double.NEGATIVE_INFINITY){
				g.drawString(" Esperanza: calculando..." , imagenWidth/ ESCALA_IMAGEN +espaceX, yInicial+espaceY*2);
			}else {
				g.drawString(" Esperanza: " + esperanzaAMostrar, imagenWidth / ESCALA_IMAGEN + espaceX, yInicial + espaceY*2);
			}

            if (desvioAMostrar == Double.NEGATIVE_INFINITY){
				g.drawString(" Varianza: calculando...", imagenWidth / ESCALA_IMAGEN + espaceX, yInicial + espaceY*3);
				g.drawString(" Desvío: calculando... ", imagenWidth / ESCALA_IMAGEN + espaceX, yInicial + espaceY*4);
			}else {
				g.drawString(" Varianza: " + Math.pow(desvioAMostrar, 2), imagenWidth / ESCALA_IMAGEN + espaceX, yInicial + espaceY*3);
				g.drawString(" Desvío: " + desvioAMostrar, imagenWidth / ESCALA_IMAGEN + espaceX, yInicial + espaceY*4);
			}
			g.drawString(" Entropía sin memoria: "+img.entropiaSinMemoria() , imagenWidth/ ESCALA_IMAGEN +espaceX, yInicial+espaceY*5);
            g.drawString(" Entropía con memoria: "+img.entropiaConMemoria() , imagenWidth/ ESCALA_IMAGEN +espaceX, yInicial+espaceY*6);
		}
		
		g.setFont(fontRefence);
		g.setColor(ColorMayorEntropia);
		g.drawString(" *Mayor Entropía* ", imagenWidth/ ESCALA_IMAGEN +espaceX, espaceY);
		g.setColor(ColorMenorEntropia);
		g.drawString(" *Menor Entropía* ", imagenWidth/ ESCALA_IMAGEN +espaceX, espaceY*2);
		g.setColor(ColorPromedioEntropia);
		g.drawString(" *Entropía promedio* ", imagenWidth/ ESCALA_IMAGEN +espaceX, espaceY*3);
	}


	public static void setOverMse(Point point) {
		if (point == null) return;
		mseOver = point;

		Imagen imagen = Screen.sc.imagen;
		if (imagen == null) return;

		//Si el mouse entra a un bloque que no era el que se encontraba seleccionado anteriormente, redibujar
		if (mseOver.getX() < imagen.getWidth()/ ESCALA_IMAGEN && mseOver.getY() < imagen.getHeight()/ ESCALA_IMAGEN
				&& mseOver.getX() >= 0 && mseOver.getY() >=0){
			for(int i=0;i<botones.size();i++) {
				if (botones.get(i).contains(mseOver) && i != bloqueSeleccionado) {
					bloqueSeleccionado = i; Screen.sc.repaint();
				}
			}
		}else{
			if (bloqueSeleccionado != Integer.MIN_VALUE){
				bloqueSeleccionado = Integer.MIN_VALUE;
				Screen.sc.repaint();
			}
		}
	}

	public static void setMseClick(Point point) {
		if (point == null) return;
		mseClick = point; Imagen imagen = Screen.sc.imagen;
		
		if (mseClick.getX() < imagen.getWidth()/ ESCALA_IMAGEN && mseClick.getY() < imagen.getHeight()/ ESCALA_IMAGEN
				&& mseClick.getX() >= 0 && mseClick.getY() >=0){
			for(int i=0;i<botones.size();i++) {
				if(botones.get(i).contains(mseClick)) {
					int imagenAnalizada = i+1;
					JFrame frame = new JFrame("Imagen analizada "+imagenAnalizada);

					frenarThreads();

					//Hice click en un botón, por lo que quiero calcular las estadísticas para otro bloque,
					//con lo que reseteo los valores que está mostrando la GUI actualmente
					Screen.esperanzaAMostrar = Double.NEGATIVE_INFINITY; Screen.desvioAMostrar = Double.NEGATIVE_INFINITY;

					Screen.img = imagenes.get(i);

					threadEsperanza = new Thread(() -> {
						Screen.esperanzaAMostrar = Screen.img.esperanza();
						Screen.sem.release(); //Indico que hay una novedad para repintar el JPanel
					});
					threadDesvio = new Thread(() -> {
						Screen.desvioAMostrar = Screen.img.desvio();
						Screen.sem.release(); //Indico que hay una novedad para repintar el JPanel
					});
					threadEsperanza.start(); threadDesvio.start();

	                frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
	                frame.setLocation(FRAME_LOC_X, FRAME_LOC_Y);
	                
	                new Thread( () -> {
	                	Main.mostrarHistograma(frame, img.hacerDatasetRepeticiones(), imagenAnalizada);
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
