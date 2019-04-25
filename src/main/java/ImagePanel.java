import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JPanel;

public class ImagePanel extends JPanel {

    Image image;

    public void setBackground(Image image) {
        this.image = image;
    }

    @Override
    public void paintComponent(Graphics G) {
        super.paintComponent(G);
        double width = image.getWidth(this); double height = image.getHeight(this);
        if (width > this.getWidth()){
            double factor = this.getWidth() / width; width = width * factor; height = height * factor;
            if (height > this.getHeight()){
                factor = this.getHeight() / height;  width = width * factor; height = height * factor;
            }
        }else{
            double factor = this.getHeight() / height;  width = width * factor; height = height * factor;
            if (width > this.getWidth()){
                factor = this.getWidth() / width; width = width * factor; height = height * factor;
            }
        }

        double widthLibre = this.getWidth() - width; double heightLibre = this.getHeight() - height;

        G.drawImage(image, (int) (widthLibre / 2), (int) (heightLibre / 2), (int) width, (int) height, null);
    }

}