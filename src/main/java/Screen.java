import java.awt.Color;
import java.awt.Font;
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
	public static Point mseOver;
	public static Point mseClick;
	private static Screen sc; //Workaround para entrar a la instancia desde los métodos estáticos que estaban definidos
	private Thread thread;
	private static Thread threadEsperanza = new Thread();
	private static Thread threadVarianza = new Thread();
	public static Semaphore sem;
	private static int bloqueSeleccionado;
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
	private static double varianzaAMostrar;
	
	public final static int FRAME_WIDTH = 800;
    public final static int FRAME_HEIGHT = 700;
    public final static int FRAME_LOC_X = 350;
    public final static int FRAME_LOC_Y = 250;
    
    private int espaceX = 20;
    private int espaceY = 20;
    private Font fontRefence = new Font("referencia", Font.BOLD, 16);
    private Font fontDat = new Font("datos", Font.BOLD, 12);
    
    private static Boton cargarImagen;
    private int botonCIX = 550;
    private int botonCIY = 450;
    private int botonCIWidth = 700;
    private int botonCIHeight = 140;
    private String botonCIName = "Nueva Imagen";

	public Screen(Imagen imagen, JFrame f) {
		Screen.sc = this;
		f.addMouseListener(new KeyHandel());
		f.addMouseMotionListener(new KeyHandel());
		frame = f;
		sem = new Semaphore(0);
		thread = new Thread(this);
		thread.start();
		
		cargarImagen = new Boton(botonCIX, botonCIY, botonCIWidth, botonCIHeight);
		cargarImagen.setName(botonCIName);
        
		reset(imagen);
	}
	
	public void reset(Imagen imagen) {
		frenarThreads();

		this.imagen = imagen;
		imagenWidth = imagen.getWidth();
		imagenes = imagen.obtenerCuadrantes();
		img = null;
		bloqueSeleccionado = Integer.MIN_VALUE;
		esperanzaAMostrar = Double.NEGATIVE_INFINITY;
		varianzaAMostrar = Double.NEGATIVE_INFINITY;


		mseOver = new Point(0, 0);
		mseClick = new Point(-1, -1);
		

		botones = new ArrayList<>();
        //inicializar botones
        for(int j=0;j<imagen.getHeight()/Imagen.TAMANIOBLOQUECUADRANTE;j++)
        	for(int i=0;i<imagen.getWidth()/Imagen.TAMANIOBLOQUECUADRANTE;i++){
        		botones.add(new Boton(i*(imagen.TAMANIOBLOQUECUADRANTE/Imagen.ESCALA),
						j*(Imagen.TAMANIOBLOQUECUADRANTE/Imagen.ESCALA),Imagen.TAMANIOBLOQUECUADRANTE,
						Imagen.TAMANIOBLOQUECUADRANTE));
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

	private static void frenarThreads() {
		if (threadEsperanza.isAlive()) {
			//Si la ejecución anterior no terminó, la aborto y reseteo por las dudas el valor
			//que la imagen haya guardado, dado que podría haberse guardado un valor no válido
			//producto del corte repentino
			threadEsperanza.stop();
			img.resetSprnz();
		}

		if (threadVarianza.isAlive()) {
			//Lo mismo que con el thread de la esperanza, pero para el cálculo de la varianza
			threadVarianza.stop();
			img.resetVrnz();
		}
	}

	//dibujo sobre el frame
	public synchronized void paintComponent(Graphics g) {
		g.clearRect(0, 0, getWidth(), getHeight());
		for(int i=0;i<botones.size();i++) {
			botones.get(i).paintComponent(g);
		}
		
		cargarImagen.setBackground(Color.GREEN);
		cargarImagen.paintComponent(g);

		if (img!= null) {
			final int yInicial = 100;
            g.setColor(Color.black);
            g.setFont(fontRefence);
			g.drawString(" Datos de imagen: "+numIm , imagenWidth/Imagen.ESCALA +espaceX, yInicial);

			g.setFont(fontDat);
            if (esperanzaAMostrar == Double.NEGATIVE_INFINITY){
				g.drawString(" Esperanza: calculando..." , imagenWidth/Imagen.ESCALA +espaceX, yInicial+espaceY*2);
			}else {
				g.drawString(" Esperanza: " + esperanzaAMostrar, imagenWidth /Imagen.ESCALA + espaceX, yInicial + espaceY*2);
			}

            if (varianzaAMostrar == Double.NEGATIVE_INFINITY){
				g.drawString(" Varianza: calculando...", imagenWidth /Imagen.ESCALA + espaceX, yInicial + espaceY*3);
				g.drawString(" Desvío: calculando... ", imagenWidth /Imagen.ESCALA + espaceX, yInicial + espaceY*4);
			}else {
				g.drawString(" Varianza: " + varianzaAMostrar, imagenWidth /Imagen.ESCALA + espaceX, yInicial + espaceY*3);
				g.drawString(" Desvío: " + Math.sqrt(varianzaAMostrar), imagenWidth /Imagen.ESCALA + espaceX, yInicial + espaceY*4);
			}
			g.drawString(" Entropía sin memoria: "+img.entropiaSimple() , imagenWidth/Imagen.ESCALA +espaceX, yInicial+espaceY*5);
		}
		
		g.setFont(fontRefence);
		g.setColor(ColorMayorEntropia);
		g.drawString(" *Mayor Entropía* ", imagenWidth/Imagen.ESCALA +espaceX, espaceY);
		g.setColor(ColorMenorEntropia);
		g.drawString(" *Menor Entropía* ", imagenWidth/Imagen.ESCALA +espaceX, espaceY*2);
		g.setColor(ColorPromedioEntropia);
		g.drawString(" *Entropía promedio* ", imagenWidth/Imagen.ESCALA +espaceX, espaceY*3);
	}


	public static void setOverMse(Point point) {
		if (point == null) return;
		mseOver = point;
		Imagen imagen = Screen.sc.imagen;

		if(cargarImagen != null && cargarImagen.contains(point) && bloqueSeleccionado != -1) {
			bloqueSeleccionado = -1;
			Screen.sc.repaint();
			return;
		}

		//Si el mouse entra a un bloque que no era el que se encontraba seleccionado anteriormente, redibujar
		if (mseOver.getX() < imagen.getWidth()/Imagen.ESCALA && mseOver.getY() < imagen.getHeight()/Imagen.ESCALA){
			int cantCol = imagen.getWidth() / Imagen.TAMANIOBLOQUECUADRANTE;
			int y = (int) Math.ceil(mseOver.getY() / (Imagen.TAMANIOBLOQUECUADRANTE/Imagen.ESCALA));
			int x = (int) Math.ceil(mseOver.getX() / (Imagen.TAMANIOBLOQUECUADRANTE/Imagen.ESCALA));

			int bloque = (y-1)*cantCol+x-1;
			if (bloque != bloqueSeleccionado){
				bloqueSeleccionado = bloque; Screen.sc.repaint();
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

		if(cargarImagen != null && cargarImagen.contains(point)) {
			Main.abrirArchivo(frame);
			return;
		}
		
		if (mseClick.getX() < imagen.getWidth()/Imagen.ESCALA && mseClick.getY() < imagen.getHeight()/Imagen.ESCALA){
			for(int i=0;i<botones.size();i++) {
				if(botones.get(i).contains(mseClick)) {
					int imagenAnalizada = i+1;
					JFrame frame = new JFrame("Imagen analizada "+imagenAnalizada);

					frenarThreads();

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

	                frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
	                frame.setLocation(FRAME_LOC_X, FRAME_LOC_Y);
	                
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
