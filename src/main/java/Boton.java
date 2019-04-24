import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import javax.swing.JPanel;

public class Boton extends JPanel {
	private int x;
	private int y;
	private int width;
	private int height;
	private Imagen img = null;
	private boolean remarcar = false;
	private Color c = Color.WHITE;
	
	public Boton(int x,int y,int width, int height) {
		this.x=x;
		this.y=y;
		this.width=width;
		this.height=height;
	}
	
	public void addImage(Imagen i) {
		img = i;
		img.setX(x);
		img.setY(y);
	}
	
	public void remarcar(boolean b, Color c) {
		remarcar = b;
		this.c = c;
	}
	
	public boolean contains(Point p) {
		return(p.getX()<=x+width/4d && p.getX()> x && p.getY() >y && p.getY()<= y+height/4d);
	}
	
	public void paintComponent(Graphics g) {
		if(img != null)
			img.paintComponent(g);

		if (remarcar) {
			g.setColor(c);
			g.drawRect(x+1, y+1, width/4-3, height/4-3);
		}
			
		g.setColor(Color.black);
		if(contains(Screen.mseOver))
			g.setColor(Color.red);
		g.drawRect(x, y, width/4-1, height/4-1);
	}
}
