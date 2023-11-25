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
    private final double maxVelocity = 10; // Maximum velocity
    private final double accelerationRate = 0.1; // Acceleration rate
    private final double velocityDecayRate = 0.3; // Velocity decay rate
    private final double friction = 0.02; // Friction to simulate deceleration
    private final int carTurnRate = 5; // Car turning speed
    private final int trackWidth = 1920;
    private final int trackHeight = 1080;
    private final int carWidth = 40;
    private final int carHeight = 20;
    private final int delay = 1; // Delay in milliseconds for the timer

    private List<Wall> raceCourse;
    private List<Point> barriers;

    public Simulator() {
        setTitle("Car Racing Game");
        setSize(trackWidth, trackHeight);
        setResizable(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        carX = trackWidth / 2.0;
        carY = trackHeight / 2.0;

        // Initialize white spots
        initializeRaceCourse();

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

    private void initializeRaceCourse() {

        raceCourse = new ArrayList<>(); // Initialize the raceCourse list
        raceCourse.add(createWall(new Point(100, 100), new Point(500, 100)));
        raceCourse.add(createWall(new Point(500, 100), new Point(700, 300)));
        raceCourse.add(createWall(new Point(700, 300), new Point(500, 500)));
        raceCourse.add(createWall(new Point(500, 500), new Point(100, 500)));
        raceCourse.add(createWall(new Point(100, 500), new Point(100, 100))); 


        barriers = new ArrayList<>(); 

        // collision detection
        for (Wall wall : raceCourse) {
        Point start = wall.getStart();
        Point end = wall.getEnd();

        // Iterate through each point on the line segment between start and end
        for (int t = 0; t <= 100; t++) {
            int x = start.x + t * (end.x - start.x) / 100;
            int y = start.y + t * (end.y - start.y) / 100;
            barriers.add(new Point(x, y));
        }
}
    }


    public Wall createWall(Point start, Point end) {
        Wall wall = new Wall(null, null);
        wall.start = start;
        wall.end = end;
        return wall;
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
                if (carVelocity != 0){
                    carAngle -= carTurnRate;
                }                   
                 break;
                case KeyEvent.VK_D:
                if (carVelocity != 0){
                    carAngle += carTurnRate;
                }
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

            if (carVelocity < 0.05 && carVelocity > -0.05){
                carVelocity = 0;
            }
        
            // Limit velocity to maxVelocity
            carVelocity = Math.min(maxVelocity, Math.max(-maxVelocity, carVelocity));
        
            // Update position based on velocity and angle
            double angleInRadians = Math.toRadians(carAngle);
            carX += carVelocity * Math.cos(angleInRadians);
            carY += carVelocity * Math.sin(angleInRadians);

        
            for (Point barrier : barriers) {
                int barrierRadius = 5; // You can adjust this value based on your needs
            
                if (Math.abs(carX - barrier.x) <= barrierRadius && Math.abs(carY - barrier.y) <= barrierRadius) {
                    carVelocity = 0;
                    System.out.println("Collision detected!");

                    // move the car back a bit to avoid repeated collision detection
                    carX -= Math.cos(angleInRadians);
                    carY -= Math.sin(angleInRadians);
                }
            }

            

            // If the car goes off the screen, move it to the other side
            if (carX < 0 || carX > trackWidth || carY < 0 || carY > trackHeight) {
                carX = (carX + trackWidth) % trackWidth;
                carY = (carY + trackHeight) % trackHeight;
            }
    
            repaint(); 
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
            for (Wall wall : raceCourse) {
                g.drawLine(wall.start.x, wall.start.y, wall.end.x, wall.end.y);
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
        
            // Draw headlights (white squares) on the front of the car
            g2d.setColor(Color.WHITE); // Headlight color
            int headlightSize = 5; // Headlight size
        
            // Left headlight
            int leftHeadlightX = (int) (carX + carWidth - headlightSize);
            int leftHeadlightY = (int) (carY);
            g2d.fillRect(leftHeadlightX, leftHeadlightY, headlightSize, headlightSize);
        
            // Right headlight
            int rightHeadlightX = (int) (carX + carWidth - headlightSize);
            int rightHeadlightY = (int) (carY + carHeight - headlightSize);
            g2d.fillRect(rightHeadlightX, rightHeadlightY, headlightSize, headlightSize);
        
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
