package simulator;

import javax.swing.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Random;

import GenerateVector.GenerateInputVector;
import GenerateVector.GenerateOutputVector;
import neuralNetwork.NeuralNetwork;

public class Simulator extends JFrame {

    private SimulatorConfig config = SimulatorConfig.getInstance();
    private RaceCourseManager raceCourseManager;
    private CarManager carManager;

    Random random = new Random(System.currentTimeMillis());

    public Simulator() {
        setTitle("Car Racing Game");
        setSize(config.trackWidth, config.trackHeight);
        setResizable(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        raceCourseManager = new RaceCourseManager();
        SimulatorPanel panel = new SimulatorPanel();
        carManager = new CarManager();

        // Initialize the neural networks
        NeuralNetwork neuralNetwork = new NeuralNetwork(config.numInputs, config.numHiddenNeurons, config.numOutputs);

        for (int i = 0; i < config.numOfCars; i++) {
            neuralNetwork = new NeuralNetwork(config.numInputs, config.numHiddenNeurons, config.numOutputs);
            Car car = new Car(config.carX, config.carY, config.carAngle);
            car.neuralNetwork = neuralNetwork;
            config.cars.add(car);
        }

        GenerateInputVector.numInputs = config.numInputs;
        GenerateOutputVector.numOutputs = config.numOutputs;
        GenerateOutputVector.numInputs = config.numInputs;
        NeuralNetwork.numInputs = config.numInputs;
        NeuralNetwork.numHiddenNeurons = config.numHiddenNeurons;
        NeuralNetwork.numOutputs = config.numOutputs;

        config.offScreenBuffer = new BufferedImage(config.trackWidth, config.trackHeight, BufferedImage.TYPE_INT_ARGB);

        raceCourseManager.initializeRaceCourse();

        add(panel);

        // For creating walls with clicks if needed
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int mouseX = e.getX() - getInsets().left;
                int mouseY = e.getY() - getInsets().top;

                if (config.firstClick == null) {
                    // First click
                    config.firstClick = new Point(mouseX, mouseY);
                    config.pointsCreated.add(config.firstClick);
                } else {
                    // Second click
                    Point secondClick = new Point(mouseX, mouseY);
                    config.raceCourse.add(raceCourseManager.createWall(config.firstClick, secondClick));
                    config.pointsCreated.add(secondClick);
                    // Reset firstClick for the next wall
                    config.firstClick = null;
                    config.isWallAdded = true;
                }
            }
        });

        // code for running once window is closed for debugging for logging
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                for (Point points : config.pointsCreated) {
                    System.out.println(points.x + " " + points.y);
                }
            }
        });

        setFocusable(true);

        // Create a timer to repaint the panel every delay milliseconds
        Timer timer = new Timer(config.delay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.moveCar();
                repaint();
            }
        });

        // Create a thread to calculate the sensor collision points
        Thread sensorThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    for (Car car : config.cars) {
                        carManager.calculateSensorCollisionPoints(car, config.barriers, config.raceCourse);
                        repaint();
                    }
                    try {
                        Thread.sleep(10); // Adjust the sleep duration as needed
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        synchronized (config.barriers) {
            sensorThread.start(); // Start the sensor thread
            timer.start(); // Start the timer
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Simulator game = new Simulator();
            game.setVisible(true);
        });
    }

    private void drawTrack(Graphics g) {
        // Draw race track
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, config.trackWidth, config.trackHeight);
    
        g.setColor(Color.WHITE);
        for (Wall wall : config.raceCourse) {
            if (wall.isCurveD || wall.isCurveU) {
                // Assuming you have a radius for the curved wall

                int x = wall.start.x - config.radius;
                int y = wall.start.y - config.radius;
                int width = 2 * config.radius;
                int height = 2 * config.radius;
    
                if (wall.isCurveD) {
                    // Draw a downward curved wall
                    g.drawArc(x, y, width, height, 0, 180);
                } else {
                    // Draw an upward curved wall
                    g.drawArc(x, y, width, height, 180, 180);
                }
            } else {
                // Draw a straight line
                g.drawLine(wall.start.x, wall.start.y, wall.end.x, wall.end.y);
            }
        }
    }

    private void drawSensorLines(Graphics g, Car car) {
        List<Point> currentSensorCollisionPoints = car.sensorCollisionPoint;
    
        if (currentSensorCollisionPoints != null) {
            g.setColor(Color.YELLOW);
    
            for (Point sensorCollisionPoint : currentSensorCollisionPoints) {
                if (sensorCollisionPoint.x != -1 && sensorCollisionPoint.y != -1) {
                    g.drawLine((int) car.x + config.carWidth / 2, (int) car.y + config.carHeight / 2,
                            sensorCollisionPoint.x, sensorCollisionPoint.y);
                }
            }
        }
    }

    // give each car a different color
    private void drawCar(Graphics g, Car car) {
        Graphics2D g2d = (Graphics2D) g;


        g2d.setColor(car.colour);
    
        // Create an AffineTransform to rotate the car
        AffineTransform oldTransform = g2d.getTransform();
        AffineTransform newTransform = new AffineTransform();
        newTransform.rotate(Math.toRadians(car.angle), car.x + config.carWidth / 2.0, car.y + config.carHeight / 2.0);
        g2d.setTransform(newTransform);
    
        // Draw the rotated car
        g2d.fillRect((int) car.x, (int) car.y, config.carWidth, config.carHeight);
    
        // Draw headlights (white squares) on the front of the car
        g2d.setColor(Color.WHITE); // Headlight color
        int headlightSize = 5; // Headlight size
    
        // Left headlight
        int leftHeadlightX = (int) (car.x + config.carWidth - headlightSize);
        int leftHeadlightY = (int) (car.y);
        g2d.fillRect(leftHeadlightX, leftHeadlightY, headlightSize, headlightSize);
    
        // Right headlight
        int rightHeadlightX = (int) (car.x + config.carWidth - headlightSize);
        int rightHeadlightY = (int) (car.y + config.carHeight - headlightSize);
        g2d.fillRect(rightHeadlightX, rightHeadlightY, headlightSize, headlightSize);
    
        // Reset the transform to the original state
        g2d.setTransform(oldTransform);
    }

    public class SimulatorPanel extends JPanel {

        public void moveCar() {
            carManager.moveCar();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics offScreenGraphics = config.offScreenBuffer.getGraphics();
            offScreenGraphics.clearRect(0, 0, config.trackWidth, config.trackHeight);

            drawTrack(offScreenGraphics);

            for (Car car : config.cars) {
                if (!car.isDead) {
                    drawSensorLines(offScreenGraphics, car);
                }
                drawCar(offScreenGraphics, car);
            }

            g.drawImage(config.offScreenBuffer, 0, 0, this);
        }
    }
}
