import javax.swing.*;
import java.awt.*;

public class ButtonPanel extends JPanel {
    private JButton greenBallButton;
    private JButton redBallButton;

    public ButtonPanel() {
        greenBallButton = new JButton("Add Green Ball");
        redBallButton = new JButton("Add Red Ball");

        //setLayout(new FlowLayout(FlowLayout.RIGHT));  // Use FlowLayout with RIGHT alignment
        add(greenBallButton);
        add(redBallButton);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Paint child components onto the off-screen buffer

        //setLayout(new FlowLayout(FlowLayout.CENTER)); // Adjust the layout manager as needed
        paintComponents(g);
    }

    public JButton getGreenBallButton() {

        return greenBallButton;
    }

    public JButton getRedBallButton() {

        return redBallButton;
    }
}
