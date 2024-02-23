import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class Mouse implements MouseListener {

    private App app;  // Reference to the App class

    public Mouse(App app) {
        this.app = app;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        int mouseX = e.getX();
        int mouseY = e.getY();
        int button = e.getButton();
        app.mouseClicked(mouseX, mouseY, button);
    }

    // Other methods of the MouseListener interface
    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}