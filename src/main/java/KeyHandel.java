import java.awt.Point;
import java.awt.event.*;

public class KeyHandel implements MouseMotionListener, MouseListener{
	private final int error = 31;
	
	@Override
	public void mouseDragged(MouseEvent e) {
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (Screen.sc != null)
		Screen.setOverMse(new Point(e.getX()-8 - Screen.sc.getX(), e.getY() - error - Screen.sc.getY()));
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (Screen.sc != null)
		Screen.setMseClick(new Point(e.getX()-8 - Screen.sc.getX(), e.getY() - error - Screen.sc.getY()));
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {		
	}

	@Override
	//Soltar el boton del mouse
	public void mouseReleased(MouseEvent e) {		
	}

}
