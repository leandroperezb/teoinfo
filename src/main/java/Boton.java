import java.awt.Color;
import java.awt.Font;
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
	private String name = null;
	private Font font = new Font(null, Font.BOLD, 14);
	private int centerTextLocX = 35;
	private int centerTextLocyY = 22;
	
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
		return(p.getX()<=x+width && p.getX()> x && p.getY() >y && p.getY()<= y+height);
	}
	
	public void setName(String n) {
		name = n;
	}
	
	public void paintComponent(Graphics g) {
		if(img != null)
			img.paintComponent(g);
			
		g.setColor(Color.black);
		if(Screen.bloqueSeleccionado != Integer.MIN_VALUE && contains(Screen.mseOver))
			g.setColor(Color.red);
		g.drawRect(x, y, width -1, height -1);
		
		if (name != null) {
			g.setFont(font);
			g.drawString(name, x+centerTextLocX, y+centerTextLocyY);
		}

		if (remarcar) {
			g.setColor(c);
			g.drawRect(x+1, y+1, width -3, height -3);
		}
	}
}
