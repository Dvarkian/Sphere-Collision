import javax.swing.JFrame; // Used for creating a window.
import javax.swing.JPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage; // Used for double-buffering, to make rendering smoother.
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;


public class App extends JPanel {

    private BufferedImage buffer; // Off-screen buffer
    // Makes rendering smoother.

    private ArrayList<Ball> balls; // Correct declaration

    private Timer ballAddTimer;

    private long lastRenderTime = System.nanoTime();


    private double g = 9.81 * 10; // Acceleration due to gravity.

    private double dt = 0;

    private Mouse ballMouseListener;


    private JButton greenBallButton;
    private JButton redBallButton;

    private ButtonPanel buttonPanel = new ButtonPanel();
    private JScrollPane buttonScrollPane;


    private int simulationPaneTopOffset = 50;
    private int elementPaneWidth = 280;

    public App() {
        balls = new ArrayList<>();
        // Create and add balls to the list
        //balls.add(new Ball(0, 0, 1, 1, 10, Color.RED));
        //balls.add(new Ball(100, 100, 1e-16, 1e-16, 1e-16, g, 50, Color.BLUE));
        //balls.add(new Ball(300, 300, 0, 0, 10, Color.GREEN));
        // Add more balls as needed


        buttonScrollPane = new JScrollPane(buttonPanel);
        buttonScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);


        /*

        ballAddTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNewBall();
            }
        });
        ballAddTimer.start();

        */

        // Initialize and add the BallMouseListener to the JFrame
        ballMouseListener = new Mouse(this);
        addMouseListener(ballMouseListener);


        // Initialize buttons

        greenBallButton = buttonPanel.getGreenBallButton();
        redBallButton = buttonPanel.getRedBallButton();

        // Add ActionListeners to the buttons
        greenBallButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNewBall(Color.GREEN);
            }
        });

        redBallButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNewBall(Color.RED);
            }
        });

        // Create a layout manager (for example, BorderLayout) and add components
        setLayout(new FlowLayout(FlowLayout.RIGHT));  // Use FlowLayout with RIGHT alignment

        // Set layout manager for the main frame (BorderLayout, for example)
        setLayout(new BorderLayout());

        setBackground(Color.WHITE); // Set the background color to white


        // Add the scrollable button list to the right side
        add(buttonScrollPane, BorderLayout.EAST);

        //add(buttonPanel);
    }



    public void mouseClicked(int mouseX, int mouseY, int button) {

        if (button == MouseEvent.BUTTON1) {


            // Add a new ball at the mouse click location
            balls.add(new Ball(mouseX, mouseY-simulationPaneTopOffset, 0, 0, 0, g, 10, Color.ORANGE));

        }

        // Repaint the JFrame to show the newly added ball
        repaint();
    }

    private void addNewBall(Color color) {
        // Add a new ball with the specified color
        balls.add(new Ball(0, simulationPaneTopOffset, 0, 0, 0, g, 10, color));
        repaint();
    }

    private void move() {
        int windowWidth = getWidth();
        int windowHeight = getHeight();

        int topBound = windowHeight - simulationPaneTopOffset;
        int bottomBound = 0;
        int leftBound = 0;
        int rightBound = windowWidth - elementPaneWidth;

        int wallColisions = 0;



        // Create an iterator to avoid ConcurrentModificationException
        ArrayList<Ball> ballsCopy = new ArrayList<>(balls);

        for (Ball ball : ballsCopy) {
            ball.move(topBound, bottomBound, leftBound, rightBound, dt);

            // Check and handle collisions between balls
            for (Ball other : balls) {
                if (ball != other && ball.collidesWith(other, dt)) {
                    ball.collide(other, windowHeight, simulationPaneTopOffset, dt);
                }
            }

            // If you need to remove a ball based on certain conditions, do it on the original list
            // For example, remove a ball if it goes outside the window bounds
            //if (ball.getX() < 0 || ball.getX() > windowWidth || ball.getY() < 0 || ball.getY() > windowHeight) {
            //    balls.remove(ball);
            //}
        }
    }



    private void render(Graphics graphic) {

        int windowWidth = getWidth();
        int windowHeight = getHeight();

        // Only create the buffer if it hasn't been created yet
        buffer = new BufferedImage(windowWidth-elementPaneWidth, windowHeight, BufferedImage.TYPE_INT_RGB);


        Graphics offScreenGraphics = buffer.getGraphics();

        offScreenGraphics.setColor(getBackground());
        offScreenGraphics.fillRect(0, 0, windowWidth-elementPaneWidth, windowHeight);

        double totalKineticEnergy = 0;
        double totalGravitationalPotentialEnergy = 0;

        // Calculate FPS
        long currentTime = System.nanoTime();
        dt = (currentTime - lastRenderTime) / 1e9; // Convert nanoseconds to seconds
        lastRenderTime = currentTime;
        double fps = 1.0 / dt;

        // Draw buttons onto the off-screen buffer
        //greenBallButton.paint(offScreenGraphics);
        //redBallButton.paint(offScreenGraphics);


        for (Ball ball : balls) {

            totalKineticEnergy += ball.getKineticEnergy();
            totalGravitationalPotentialEnergy += ball.getGravitationalPotentialEnergy(windowHeight, g);

            ball.render(offScreenGraphics);
        }

        double totalEnergy = totalKineticEnergy + totalGravitationalPotentialEnergy;





        Font font = new Font("Arial", Font.PLAIN, 12);  // You can adjust the font and size as needed
        offScreenGraphics.setFont(font);
        offScreenGraphics.setColor(Color.BLACK);  // Set the color for the text


        offScreenGraphics.drawString(String.format("FPS: %.0f", fps), 50, 40);
        offScreenGraphics.drawString(String.format("KE: %.1f", totalKineticEnergy), 50, 50);
        offScreenGraphics.drawString(String.format("GPE: %.1f", totalGravitationalPotentialEnergy), 50, 60);
        offScreenGraphics.drawString(String.format("E: %.1f", totalEnergy), 50, 70);

        //setLayout(new FlowLayout(FlowLayout.CENTER)); // Adjust the layout manager as needed
        //buttonPanel.paintComponent(offScreenGraphics);

        offScreenGraphics.dispose();

        // Draw the off-screen buffer onto the screen
        graphic.drawImage(buffer, 0, simulationPaneTopOffset, this);
    }


    @Override
    public void paintComponent(Graphics graphic) {
        super.paintComponent(graphic);

        // Draw the off-screen buffer onto the screen
        graphic.drawImage(buffer, 0, simulationPaneTopOffset, this);
    }



    public static void main(String[] args) {
        JFrame frame = new JFrame("Moving Ball!");
        App app = new App();
        frame.add(app);
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        frame.addWindowListener(
            new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    app.ballAddTimer.stop();
                }
            }
        );

        // Add a ComponentListener to the frame to detect window resizing
        frame.addComponentListener(
            new java.awt.event.ComponentAdapter() {
                public void componentResized(java.awt.event.ComponentEvent evt) {
                    app.repaint();
                }
            }
        );

        frame.setVisible(true);

        // Use a Swing Timer for continuous updates
        Timer timer = new Timer(4, (ActionEvent e) -> {
            app.move();
            app.repaint();
            app.render(app.getGraphics());  // Call render with the Graphics object

        });
        timer.start();

    }
}
