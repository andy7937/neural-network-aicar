package simulator;

import javax.swing.*;

import org.nd4j.linalg.api.ndarray.INDArray;
import GenerateVector.GenerateInputVector;
import GenerateVector.GenerateOutputVector;
import neuralNetwork.NeuralNetwork;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Simulator extends JFrame {

    private double carX, carY; // Car position (double for smoother movements)
    private double carVelocity = 0; // Car speed
    private double carAcceleration = 0; // Car acceleration
    private double carAngle = 0; // Car angle in degrees
    private final int baseCarTurnRate = 10; // Car turning speed
    private final double maxVelocity = 8; // Maximum velocity
    private final double accelerationRate = 0.075; // Acceleration rate
    private final double velocityDecayRate = 0.5; // Velocity decay rate
    private final double friction = 0.02; // Friction to simulate deceleration
    private final int trackWidth = 1920;
    private final int trackHeight = 1080;
    private final int carWidth = 40;
    private final int carHeight = 20;
    private final int delay = 10; // Delay in milliseconds for the timer

    private List<Wall> raceCourse;
    private List<Point> barriers;
    private BufferedImage offScreenBuffer;
    private Point firstClick;
    private int numInputs = 14;
    private int numHiddenNeurons = 8;
    private int numOutputs = 2;
    private List<INDArray> outputList = new ArrayList<>();
    private List<INDArray> inputList = new ArrayList<>();
    private List<Integer> actionList = new ArrayList<>();
    private int reward = 0;
    private List<Point> pointsCreated = new ArrayList<>();
    private List<Car> cars = new ArrayList<>();
    private int numOfCars = 10;
    private int iteration = 0;
    private int maxIterations = 500;
    private int generation = 0;
    private int maxGenerations = 100;



    Random random = new Random();
    

    public Simulator() {
        setTitle("Car Racing Game");
        setSize(trackWidth, trackHeight);
        setResizable(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        // Initialize the neural networks
        carX = 160;
        carY = 123;
        carAngle = 90;
        NeuralNetwork neuralNetwork = new NeuralNetwork(numInputs, numHiddenNeurons, numOutputs);

        for (int i = 0; i < numOfCars; i++){
            neuralNetwork = new NeuralNetwork(numInputs, numHiddenNeurons, numOutputs);
            Car car = new Car(carX, carY, carAngle);
            car.neuralNetwork = neuralNetwork;
            cars.add(car);

        }
        GenerateInputVector.numInputs = numInputs;
        GenerateOutputVector.numOutputs = numOutputs;
        GenerateOutputVector.numInputs = numInputs;


        offScreenBuffer = new BufferedImage(trackWidth, trackHeight, BufferedImage.TYPE_INT_ARGB);


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

        // For creating walls with clicks if needed
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int mouseX = e.getX();
                int mouseY = e.getY();

                if (firstClick == null) {
                    // First click
                    firstClick = new Point(mouseX, mouseY);
                    pointsCreated.add(firstClick);
                } else {
                    // Second click
                    Point secondClick = new Point(mouseX, mouseY);
                    raceCourse.add(createWall(firstClick, secondClick));
                    pointsCreated.add(secondClick);

                    // Reset firstClick for the next wall
                    firstClick = null;
                    initBarrier();
                }
            }
        });

        // code for running once window is closed for debugging
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                for (Point points : pointsCreated){
                    System.out.println(points.x + " " + points.y);
                }
            }
        });


        setFocusable(true);

        // Create a to repaint the panel every delay milliseconds
        Timer timer = new Timer(delay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // if all cars are dead then stop the timer
                panel.moveCar();
                repaint();

            }
        });

        // Create a thread to calculate the sensor collision points
        Thread sensorThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    for (Car car : cars){
                        panel.calculateSensorCollisionPoints(car);
                    }
                    try {
                        Thread.sleep(10); // Adjust the sleep duration as needed
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        sensorThread.start(); // Start the sensor thread
        timer.start(); // Start the timer
    }

    private void initializeRaceCourse() {

        raceCourse = new ArrayList<>(); // Initialize the raceCourse list

        raceCourse.add(createWall(new Point(34, 69), new Point(37, 558)));
        raceCourse.add(createWall(new Point(37, 558), new Point(129, 813)));
        raceCourse.add(createWall(new Point(129, 813), new Point(129, 814)));
        raceCourse.add(createWall(new Point(129, 814), new Point(288, 922)));
        raceCourse.add(createWall(new Point(288, 922), new Point(299, 927)));
        raceCourse.add(createWall(new Point(299, 927), new Point(627, 947)));
        raceCourse.add(createWall(new Point(627, 947), new Point(665, 812)));
        raceCourse.add(createWall(new Point(665, 812), new Point(651, 524)));
        raceCourse.add(createWall(new Point(651, 524), new Point(742, 385)));
        raceCourse.add(createWall(new Point(742, 385), new Point(765, 387)));
        raceCourse.add(createWall(new Point(765, 387), new Point(937, 385)));
        raceCourse.add(createWall(new Point(937, 385), new Point(1023, 474)));
        raceCourse.add(createWall(new Point(1023, 474), new Point(942, 404)));
        raceCourse.add(createWall(new Point(942, 404), new Point(1037, 488)));
        raceCourse.add(createWall(new Point(1037, 488), new Point(1041, 698)));
        raceCourse.add(createWall(new Point(1041, 698), new Point(1056, 711)));
        raceCourse.add(createWall(new Point(1056, 711), new Point(1076, 907)));
        raceCourse.add(createWall(new Point(1076, 907), new Point(1090, 924)));
        raceCourse.add(createWall(new Point(1090, 924), new Point(1329, 944)));
        raceCourse.add(createWall(new Point(1329, 944), new Point(1352, 954)));
        raceCourse.add(createWall(new Point(1352, 954), new Point(1608, 952)));
        raceCourse.add(createWall(new Point(1608, 952), new Point(1618, 967)));
        raceCourse.add(createWall(new Point(1618, 967), new Point(1800, 969)));
        raceCourse.add(createWall(new Point(1800, 969), new Point(1805, 975)));
        raceCourse.add(createWall(new Point(1805, 975), new Point(1794, 123)));
        raceCourse.add(createWall(new Point(1794, 123), new Point(259, 88)));
        raceCourse.add(createWall(new Point(259, 88), new Point(272, 280)));
        raceCourse.add(createWall(new Point(272, 280), new Point(284, 298)));
        raceCourse.add(createWall(new Point(284, 298), new Point(304, 429)));
        raceCourse.add(createWall(new Point(304, 429), new Point(306, 436)));
        raceCourse.add(createWall(new Point(306, 436), new Point(367, 578)));
        raceCourse.add(createWall(new Point(367, 578), new Point(454, 574)));
        raceCourse.add(createWall(new Point(454, 574), new Point(517, 600)));
        raceCourse.add(createWall(new Point(517, 600), new Point(455, 583)));
        raceCourse.add(createWall(new Point(455, 583), new Point(375, 606)));
        raceCourse.add(createWall(new Point(375, 606), new Point(521, 315)));
        raceCourse.add(createWall(new Point(521, 315), new Point(523, 613)));
        raceCourse.add(createWall(new Point(523, 613), new Point(527, 340)));
        raceCourse.add(createWall(new Point(527, 340), new Point(707, 227)));
        raceCourse.add(createWall(new Point(707, 227), new Point(713, 244)));
        raceCourse.add(createWall(new Point(713, 244), new Point(1004, 224)));
        raceCourse.add(createWall(new Point(1004, 224), new Point(1013, 234)));
        raceCourse.add(createWall(new Point(1013, 234), new Point(1174, 281)));
        raceCourse.add(createWall(new Point(1174, 281), new Point(1176, 290)));
        raceCourse.add(createWall(new Point(1176, 290), new Point(1324, 468)));
        raceCourse.add(createWall(new Point(1324, 468), new Point(1325, 488)));
        raceCourse.add(createWall(new Point(1325, 488), new Point(1291, 668)));
        raceCourse.add(createWall(new Point(1291, 668), new Point(1300, 694)));
        raceCourse.add(createWall(new Point(1300, 694), new Point(1375, 837)));
        raceCourse.add(createWall(new Point(1375, 837), new Point(1673, 863)));
        raceCourse.add(createWall(new Point(1673, 863), new Point(1381, 861)));
        raceCourse.add(createWall(new Point(1381, 861), new Point(1665, 872)));
        raceCourse.add(createWall(new Point(1665, 872), new Point(1651, 136)));
        raceCourse.add(createWall(new Point(42, 82), new Point(270, 96)));

        



        // Initialize the barriers list
        barriers = new ArrayList<>(); 
                initBarrier();

    }

    public void initBarrier(){

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

        private GenerateInputVector generateInputVector = new GenerateInputVector();
        private GenerateOutputVector generateOutputVector = new GenerateOutputVector();

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
                    if (carVelocity != 0) {
                        // Adjust the turning rate based on the car's velocity
                        double adjustedTurnRate = baseCarTurnRate * (Math.abs(carVelocity * 0.8) / maxVelocity);
                        carAngle -= adjustedTurnRate; 
                    }
                    break;
                case KeyEvent.VK_D:
                    if (carVelocity != 0) {
                        // Adjust the turning rate based on the car's velocity
                        double adjustedTurnRate = baseCarTurnRate * (Math.abs(carVelocity * 0.8) / maxVelocity);
                        carAngle += adjustedTurnRate;
                    }
                    break;
                case KeyEvent.VK_SPACE:
                    if (carVelocity > 0.5) {
                        carVelocity -= velocityDecayRate;
                    } else if (carVelocity < -0.5) {
                        carVelocity += velocityDecayRate;
                    }
                    break;
                default:
                    break;
            }
        }

        public void handleKeyRelease(KeyEvent e) {
            int key = e.getKeyCode();

            switch (key) {
                case KeyEvent.VK_W:
                case KeyEvent.VK_S:
                    carAcceleration = 0;
                    break;

            }
        }
        

        public void moveCar() {

            increaseIteration();

            for (Car car: cars){
                if (!car.isDead){
                    sendNeuralNetworkInformation(car);
                    car.velocity += car.acceleration;

                    // Apply friction to simulate deceleration
                    if (car.acceleration == 0) {
                        if (car.velocity > 0) {
                            car.velocity -= friction;
                        } else if (car.velocity < 0) {
                            car.velocity += friction;
                        }
                    }

                    if (car.velocity < 0.05 && car.velocity > -0.05){
                        car.velocity = 0;
                    }

                    // Limit velocity to maxVelocity
                    car.velocity = Math.min(maxVelocity, Math.max(-maxVelocity, car.velocity));

                                // Update position based on velocity and angle
                    double angleInRadians = Math.toRadians(car.angle);
                    car.x += car.velocity * Math.cos(angleInRadians);
                    car.y += car.velocity * Math.sin(angleInRadians);

                
                    for (Point barrier : barriers) {
                        int barrierRadius = 5; // You can adjust this value based on your needs
                    
                        if (Math.abs(car.x - barrier.x) <= barrierRadius && Math.abs(car.y - barrier.y) <= barrierRadius) {

                            if (car.velocity > 0){
                                car.x += Math.cos(angleInRadians);
                                car.y += Math.sin(angleInRadians);
                            }
                            else if (car.velocity < 0){
                                car.x -= Math.cos(angleInRadians);
                                car.y -= Math.sin(angleInRadians);
                            }
                            // collision
                            handleCollision(car);

                            // If the car goes off the screen
                            if (car.x < 0) {
                                handleCollision(car);
                            } else if (car.x > trackWidth - carWidth) {
                                handleCollision(car);
                            }
                    
                            if (car.y < 0) {
                                handleCollision(car);
                            } else if (car.y > trackHeight - carHeight) {
                                handleCollision(car);
                            }
                        }
                    }
                }
            }
              
            
            repaint(); 
        }


        private void calculateSensorCollisionPoints(Car car) {
            List<Point> newSensorCollisionPoints = new ArrayList<>();
        
            int numSensors = 7; // Adjust the number of sensors as needed
            double startSensorAngle = Math.toRadians(-45); // Start angle for the first sensor
            double endSensorAngle = Math.toRadians(45); // End angle for the last sensor
            double angleIncrement = (endSensorAngle - startSensorAngle) / (numSensors - 1); // Angle increment between sensors
            double angleInRadians = Math.toRadians(car.angle);
        
            for (int i = 0; i < numSensors; i++) {
                double sensorX = car.x + carWidth / 2.0;
                double sensorY = car.y + carHeight / 2.0;
        
                double sensorEndX = sensorX + Math.cos(angleInRadians + startSensorAngle);
                double sensorEndY = sensorY + Math.sin(angleInRadians + startSensorAngle);
        
                Point collisionPoint = calculateCollisionPoint(sensorX, sensorY, sensorEndX, sensorEndY, car);
        
                if (collisionPoint != null) {
                    // Check if the collision point is in front of the car
                    double angleToCollisionPoint = Math.atan2(collisionPoint.y - sensorY, collisionPoint.x - sensorX);
                    double angleDifference = Math.abs(Math.atan2(Math.sin(angleToCollisionPoint - angleInRadians),
                            Math.cos(angleToCollisionPoint - angleInRadians)));
        
                    // Adjust this threshold to control the sensitivity of the sensors
                    double angleThreshold = Math.toRadians(90);
        
                    if (angleDifference <= angleThreshold) {
                        newSensorCollisionPoints.add(collisionPoint);
                    }
                } else{
                    newSensorCollisionPoints.add(new Point(-1, -1));
                }
        
                startSensorAngle += angleIncrement;
            }

            car.sensorCollisionPoint = newSensorCollisionPoints;
        }
    
        
        private Point calculateCollisionPoint(double startX, double startY, double endX, double endY, Car car) {
            Point closestCollisionPoint = null;
            double closestDistance = Double.MAX_VALUE;
        
            for (Point barrier : barriers) {
                // Check if the barrier is in front of the car
                double angleToBarrier = Math.atan2(barrier.y - startY, barrier.x - startX);
                double angleDifference = Math.abs(Math.atan2(Math.sin(angleToBarrier - Math.toRadians(car.angle)),
                        Math.cos(angleToBarrier - Math.toRadians(car.angle))));
        
                // Adjust this threshold to control the sensitivity of the sensors
                double angleThreshold = Math.toRadians(90);
        
                if (angleDifference <= angleThreshold) {
                    // Check if the barrier intersects the sensor line
                    if (lineIntersectsCircle(startX, startY, endX, endY, barrier.x, barrier.y, 5)) {
                        double distance = Math.sqrt(Math.pow(barrier.x - startX, 2) + Math.pow(barrier.y - startY, 2));
                        if (distance < closestDistance) {
                            closestDistance = distance;
                            closestCollisionPoint = new Point((int) barrier.x, (int) barrier.y);
                        }
                    }
                }
            }
        
            return closestCollisionPoint != null ? closestCollisionPoint : closestCollisionPoint;
        }
        
        private boolean lineIntersectsCircle(double startX, double startY, double endX, double endY, double circleX, double circleY, double radius) {
            double dx = endX - startX;
            double dy = endY - startY;
            double a = dx * dx + dy * dy;
            double b = 2 * (dx * (startX - circleX) + dy * (startY - circleY));
            double c = circleX * circleX + circleY * circleY + startX * startX + startY * startY
                    - 2 * (circleX * startX + circleY * startY) - radius * radius;
        
            return b * b - 4 * a * c >= 0;
        }

        private void sendNeuralNetworkInformation(Car car){
            // Update the neural network based on the total distance traveled
            INDArray inputVector = generateInputVector.generateInputVector(car.sensorCollisionPoint, car.velocity, car.acceleration, car.neuralNetwork);
            int outputAction = generateOutputVector.generateOutputVector(inputVector, car.neuralNetwork);

            // Update information for predictive training
            outputList.add(generateOutputVector.outputVector);
            inputList.add(inputVector);
            actionList.add(outputAction);
            double adjustedTurnRate = baseCarTurnRate * (Math.abs(car.velocity * 0.8) / maxVelocity);
            reward++;

            // Update the neural network based on the output given
            switch (outputAction) {
                case 0:
                    car.angle -= adjustedTurnRate;                     
                    break;
                case 1:
                    car.angle += adjustedTurnRate;                     
                    break;
                default:
                    break;    
            }

            car.acceleration = accelerationRate;
        }
        

        private void handleCollision(Car car){
            System.out.println("Collision detected!");

            car.isDead = true;

            // Reset car position and total distance traveled
            car.velocity = 0;
            car.acceleration = 0;
            // Update barriers and sensorCollisionPoints
        }

        private boolean isAllCarsDead(){
            for (Car car : cars){
                if (!car.isDead){
                    return false;
                }
            }
            return true;
        }

        private void increaseIteration(){
            System.out.println("Iteration: " + iteration);
            iteration++;
        
            // if all cars are dead or iteration is greater than maxIterations, update neural networks and get next generation
            if (iteration >= maxIterations || isAllCarsDead()){
                List<Car> topCars = findBestCar();
                System.out.println("Generation: " + generation);

                // for the top 3 cars, do something
                for (Car car : topCars){
                }
                iteration = 0;
                generation++;
                System.out.println("Generation: " + generation);
                resetCars();
             
            }

            if (generation >= maxGenerations){
                System.out.println("Done");
                System.exit(0);
            }

        }

        // find top 3 best cars
        private List<Car> findBestCar(){

            List<Car> carsCopy = new ArrayList<>(cars);
            List<Car> topCars = new ArrayList<>();
            Car topcar = null;

            for (int i = 0; i <= 3; i++){
                topcar = carsCopy.get(0);
                for (Car car : carsCopy){
                    if (car.reward > topcar.reward){
                        topcar = car;
                    }
                }
                carsCopy.remove(topcar);
                topCars.add(topcar);
            }

            return topCars;
        }

        private void resetCars(){
            for (Car car : cars){
                findBestCar();
                car.x = carX;
                car.y = carY;
                car.angle = carAngle;
                car.isDead = false;
                car.acceleration = 0;
                car.velocity = 0;
                car.reward = 0;
                inputList.clear();
                outputList.clear();
                actionList.clear();
            }
        }


        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics offScreenGraphics = offScreenBuffer.getGraphics();
            offScreenGraphics.clearRect(0, 0, trackWidth, trackHeight);

            drawTrack(offScreenGraphics);

            for (Car car : cars){
                drawCar(offScreenGraphics, car);
                drawSensorLines(offScreenGraphics, car);          
            }
            
            g.drawImage(offScreenBuffer, 0, 0, this);
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

        private void drawSensorLines(Graphics g, Car car) {
            List<Point> currentSensorCollisionPoints = car.sensorCollisionPoint;
        
            if (currentSensorCollisionPoints != null) {
                g.setColor(Color.YELLOW);
        
                for (Point sensorCollisionPoint : currentSensorCollisionPoints) {
                    if (sensorCollisionPoint.x != -1 && sensorCollisionPoint.y != -1) {
                        g.drawLine((int) car.x + carWidth / 2, (int) car.y + carHeight / 2,
                                sensorCollisionPoint.x, sensorCollisionPoint.y);
                    }
                }
            }
        }

        private void drawCar(Graphics g, Car car) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.RED);
        
            // Create an AffineTransform to rotate the car
            AffineTransform oldTransform = g2d.getTransform();
            AffineTransform newTransform = new AffineTransform();
            newTransform.rotate(Math.toRadians(car.angle), car.x + carWidth / 2.0, car.y + carHeight / 2.0);
            g2d.setTransform(newTransform);
        
            // Draw the rotated car
            g2d.fillRect((int) car.x, (int) car.y, carWidth, carHeight);
        
            // Draw headlights (white squares) on the front of the car
            g2d.setColor(Color.WHITE); // Headlight color
            int headlightSize = 5; // Headlight size
        
            // Left headlight
            int leftHeadlightX = (int) (car.x + carWidth - headlightSize);
            int leftHeadlightY = (int) (car.y);
            g2d.fillRect(leftHeadlightX, leftHeadlightY, headlightSize, headlightSize);
        
            // Right headlight
            int rightHeadlightX = (int) (car.x + carWidth - headlightSize);
            int rightHeadlightY = (int) (car.y + carHeight - headlightSize);
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
