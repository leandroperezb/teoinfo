import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Screen extends JPanel implements Runnable{
	private List<Imagen> imagenes;
	public static Point mseOver = new Point(0, 0);
	public static Point mseClick = new Point(-1, -1);
	private Thread thread = new Thread(this);
    private static Vector<Boton> botones = new Vector<Boton>();
    private int numIm = 0;
    private int imagenWidth;
    private Imagen img = null;
    private final Color ColorMayorEntropia = Color.GREEN;
    private final Color ColorMenorEntropia = Color.BLUE;

	public Screen(Imagen imagen, JFrame f) {

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
        for(int i=0;i<botones.size();i++) {
        	botones.get(i).addImage(imagenes.get(i));
        	double entropia = imagenes.get(i).entropiaSimple();
        	
        	if(entropia < entropiaMin) {
        		entropiaMin = entropia;
        		posEntropiaMin = i;
        	}
        	
        	if(entropia > entropiaMax) {
        		entropiaMax= entropia;
        		posEntropiaMax = i;
        	}
        }
        
        botones.get(posEntropiaMin).remarcar(true, ColorMenorEntropia);
        botones.get(posEntropiaMax).remarcar(true, ColorMayorEntropia);
	}
	
	//dibujo sobre el frame
	public void paintComponent(Graphics g) {
		
		for(int i=0;i<botones.size();i++) {
			botones.get(i).paintComponent(g);
			if(botones.get(i).contains(mseClick)) {
				int imagenAnalizada = i+1;
				JFrame frame = new JFrame("Imagen analizada "+imagenAnalizada);
				img = imagenes.get(i);
                frame.setSize(800,700);
                frame.setLocation(350,250);
                Main.mostrarHistograma(frame, Main.hacerDataset(img), imagenAnalizada);
                frame.setVisible(true);
                numIm = i+1;
			}
		}
		mseClick = new Point(-1, -1);
		
		if (img!= null) {
            g.setColor(Color.black);
			g.drawString(" Datos de imagen: "+numIm , imagenWidth/4+20, 40);
			g.drawString(" Esperanza: "+img.esperanza() , imagenWidth/4+20, 40+20);
			g.drawString(" Varianza: "+img.varianza(), imagenWidth/4+20, 40+40);
			g.drawString(" Desvío "+Math.sqrt(img.varianza() ) , imagenWidth/4+20, 40+60);
			g.drawString(" Entropía simple "+img.entropiaSimple() , imagenWidth/4+20, 40+80);
		}
		
		g.setColor(ColorMayorEntropia);
		g.drawString(" ---Mayor Entropia--- ", imagenWidth/4+20, 20);
		g.setColor(ColorMenorEntropia);
		g.drawString(" ---Menor Entropia--- ", imagenWidth/4+20, 30);
		
	}

	@Override
	public void run() {
		while(true) {
			repaint();//redibujar segun accion del mouse
			try {
				Thread.sleep(1);
			} catch (Exception e) {}
		}
	}

	public static void setOverMse(Point point) {
		mseOver = point;
		
	}

	public static void setMseClick(Point point) {
		mseClick = point;
		
	}

}
