import java.awt.Point;
import java.awt.event.*;

public class KeyHandel implements MouseMotionListener, MouseListener{
	@Override
	public void mouseDragged(MouseEvent e) {
		//Screen.setOverMse(new Point(e.getX() + (Main.FrameWidth - Screen.getMyWidth())/2, e.getY() - (Frame.getsize().height - Screen.getMyHeight())));	
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		Screen.setOverMse(new Point(e.getX()-8 , e.getY() - 31));
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		Screen.setMseClick(new Point(e.getX()-8 , e.getY() - 31));
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
