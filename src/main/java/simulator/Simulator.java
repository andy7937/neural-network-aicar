package simulator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

public class Simulator extends JFrame {

    private double carX, carY; // Car position (double for smoother movements)
    private double carVelocity = 0; // Car speed
    private double carAcceleration = 0; // Car acceleration
    private double carAngle = 0; // Car angle in degrees
    private final double maxVelocity = 5; // Maximum velocity
    private final double accelerationRate = 0.1; // Acceleration rate
    private final double velocityDecayRate = 0.3; // Velocity decay rate
    private final double friction = 0.02; // Friction to simulate deceleration
    private final int carTurnSpeed = 5; // Car turning speed
    private final int trackWidth = 800;
    private final int trackHeight = 800;
    private final int carWidth = 40;
    private final int carHeight = 20;
    private final int delay = 10; // Delay in milliseconds for the timer
    private List<Point> whiteSpots;

    public Simulator() {
        setTitle("Car Racing Game");
        setSize(trackWidth, trackHeight);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        carX = trackWidth / 2.0;
        carY = trackHeight / 2.0;

        // Initialize white spots
        initializeWhiteSpots();

        SimulatorPanel panel = new SimulatorPanel();
        add(panel);

        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                panel.handleKeyPress(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                panel.handleKeyRelease(e);
            }
        });

        setFocusable(true);

        Timer timer = new Timer(delay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.moveCar();
            }
        });

        timer.start(); // Start the timer
    }

    private void initializeWhiteSpots() {
        whiteSpots = new ArrayList<>();
        for (int i = 0; i < 10; i++) {  // You can adjust the number of white spots
            int spotSize = 10; // Adjust the size of the white spots
            int x = (int) (Math.random() * (trackWidth - spotSize));
            int y = (int) (Math.random() * (trackHeight - spotSize));
            whiteSpots.add(new Point(x, y));
        }
    }

    public class SimulatorPanel extends JPanel {

        public void handleKeyPress(KeyEvent e) {
            int key = e.getKeyCode();

            switch (key) {
                case KeyEvent.VK_W:
                    carAcceleration = accelerationRate;
                    break;
                case KeyEvent.VK_S:
                    carAcceleration = -accelerationRate;
                    break;
                case KeyEvent.VK_A:
                    carAngle -= carTurnSpeed;
                    break;
                case KeyEvent.VK_D:
                    carAngle += carTurnSpeed;
                    break;
                case KeyEvent.VK_SPACE:
                if (carVelocity > 0){
                    carVelocity -= velocityDecayRate;
                }else if (carVelocity < 0){
                    carVelocity += velocityDecayRate;
                   
                }
                    default:
                break;
            }
        }

        public void handleKeyRelease(KeyEvent e) {
            int key = e.getKeyCode();

            switch (key) {
                case KeyEvent.VK_W:
                case KeyEvent.VK_S:
                case KeyEvent.VK_SPACE:
                    carAcceleration = 0;
                    break;
            }
        }

        public void moveCar() {
            // Update velocity based on acceleration
            carVelocity += carAcceleration;
        
            // Apply friction to simulate deceleration
            if (carAcceleration == 0) {
                if (carVelocity > 0) {
                    carVelocity -= friction;
                } else if (carVelocity < 0) {
                    carVelocity += friction;
                }
            }
        
            // Limit velocity to maxVelocity
            carVelocity = Math.min(maxVelocity, Math.max(-maxVelocity, carVelocity));
        
            // Update position based on velocity and angle
            double angleInRadians = Math.toRadians(carAngle);
            carX += carVelocity * Math.cos(angleInRadians);
            carY += carVelocity * Math.sin(angleInRadians);
        
            // Add code to check for collisions with walls if needed
        
            repaint(); // Repaint the panel after each movement
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawTrack(g);
            drawCar(g);
        }

        private void drawTrack(Graphics g) {
            // Draw race track
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, trackWidth, trackHeight);

            // Draw white spots
            g.setColor(Color.WHITE);
            for (Point point : whiteSpots) {
                g.fillRect(point.x, point.y, 10, 10);
            }
        }

        private void drawCar(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.RED);

            // Create an AffineTransform to rotate the car
            AffineTransform oldTransform = g2d.getTransform();
            AffineTransform newTransform = new AffineTransform();
            newTransform.rotate(Math.toRadians(carAngle), carX + carWidth / 2.0, carY + carHeight / 2.0);
            g2d.setTransform(newTransform);

            // Draw the rotated car
            g2d.fillRect((int) carX, (int) carY, carWidth, carHeight);

            // Reset the transform to the original state
            g2d.setTransform(oldTransform);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Simulator game = new Simulator();
            game.setVisible(true);
        });
    }
}
