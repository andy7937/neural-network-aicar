package simulator;

import javax.swing.*;

import org.nd4j.linalg.api.ndarray.INDArray;

import GenerateVector.GenerateInputVector;
import GenerateVector.GenerateOutputVector;
import neuralNetwork.NeuralNetwork;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Simulator extends JFrame {

    private double carX = 100, carY = 123; // Car position (double for smoother movements)
    private double carVelocity = 0; // Car speed
    private double carAcceleration = 0; // Car acceleration
    private double carAngle = 90; // Car angle in degrees
    private final int baseCarTurnRate = 10; // Car turning speed
    private final double maxVelocity = 10; // Maximum velocity
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
    private int numInputs = 9;
    private int numHiddenNeurons = 5;
    private int numOutputs = 2;
    private List<INDArray> outputList = new ArrayList<>();
    private List<INDArray> inputList = new ArrayList<>();
    private List<Integer> actionList = new ArrayList<>();
    private List<Point> pointsCreated = new ArrayList<>();
    private List<Car> cars = new ArrayList<>();
    private int numOfCars = 20;
    private int iteration = 0;
    private int maxIterations = 500;
    private int generation = 0;
    private int maxGenerations = 1000;
    private int radius = 180;



    Random random = new Random(System.currentTimeMillis());
    

    public Simulator() {
        setTitle("Car Racing Game");
        setSize(trackWidth, trackHeight);
        setResizable(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        // Initialize the neural networks
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
        NeuralNetwork.numInputs = numInputs;
        NeuralNetwork.numHiddenNeurons = numHiddenNeurons;
        NeuralNetwork.numOutputs = numOutputs;


        offScreenBuffer = new BufferedImage(trackWidth, trackHeight, BufferedImage.TYPE_INT_ARGB);


        initializeRaceCourse();

        SimulatorPanel panel = new SimulatorPanel();
        add(panel);

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

        sensorThread.start(); // Start the sensor thread
        timer.start(); // Start the timer
    }

    private void initializeRaceCourse() {

        raceCourse = new ArrayList<>(); // Initialize the raceCourse list

        // wall behind start
        raceCourse.add(createWall(new Point(30, 60), new Point(200, 60)));

        // first straight line
        raceCourse.add(createWall(new Point(30, 60), new Point(30, 800)));
        raceCourse.add(createWall(new Point(200, 60), new Point(200, 800)));

        // first curve up (from cars perspective)
        raceCourse.add(createCurvedU(new Point(30 + radius, 800)));

        // wall from end of curveU striaght up
        raceCourse.add(createWall(new Point(30 + radius, 800), new Point(30 + radius, 200)));
        raceCourse.add(createWall(new Point(210 + radius, 800), new Point(210 + radius, 200)));

        // second curve down (from cars perspective)
        raceCourse.add(createCurveD(new Point(30 + radius + radius, 200)));

        // wall from end of curveD tilted straight right
        raceCourse.add(createWall(new Point(210 + radius, 200), new Point(450 + radius, 500)));
        raceCourse.add(createWall(new Point(390 + radius , 200), new Point(630 + radius, 500)));

        // wall from tilted straight right to tiled straight left
        raceCourse.add(createWall(new Point(450 + radius, 500), new Point(210 + radius, 800)));
        raceCourse.add(createWall(new Point(630 + radius , 500), new Point(390 + radius, 800)));


        // second curve up (from cars perspective)
        raceCourse.add(createCurvedU(new Point(30 + radius * 3, 800)));

        // from second curve, getting smaller wall
        raceCourse.add(createWall(new Point(30 + radius * 4, 800), new Point(30 + radius * 4 + 120, 500)));

        // opening tunnel from tight wall
        raceCourse.add(createWall(new Point(30 + radius * 4 + 120, 500), new Point(30 + radius * 4 + 360, 200)));

        // small walls in the tunnel

        // first row of small walls
        raceCourse.add(createWall(new Point(30 + radius * 4 + 10, 350), new Point(30 + radius * 4 + 30, 350)));
        raceCourse.add(createWall(new Point(30 + radius * 4 + 80, 350), new Point(30 + radius * 4 + 100, 350)));
        raceCourse.add(createWall(new Point(30 + radius * 4 + 150, 350), new Point(30 + radius * 4 + 170, 350)));


        // second row of small walls
        raceCourse.add(createWall(new Point(30 + radius * 3 + 40, 250), new Point(30 + radius * 3 + 60, 250)));
        raceCourse.add(createWall(new Point(30 + radius * 4, 250), new Point(30 + radius * 4 + 20, 250)));
        raceCourse.add(createWall(new Point(30 + radius * 4 + 160, 250), new Point(30 + radius * 4 + 180, 250)));
        raceCourse.add(createWall(new Point(30 + radius * 5 + 120, 250), new Point(30 + radius * 5 + 140, 250)));

















        // Initialize the barriers list
        initBarrier();

    }

    public void initBarrier() {
        barriers = new ArrayList<>();
    
        for (Wall wall : raceCourse) {
            Point start = wall.getStart();
            Point end = wall.getEnd();
    
            // wall is curved downwards
            if (wall.isCurveD) {
    
                // Iterate through angles to create points on the semi-circle
                for (int angle = 180; angle <= 360; angle++) {
                    int x = start.x + (int) (radius * Math.cos(Math.toRadians(angle)));
                    int y = start.y + (int) (radius * Math.sin(Math.toRadians(angle)));
                    barriers.add(new Point(x, y));
                }
            } else if (wall.isCurveU) {    
                // Iterate through angles to create points on the semi-circle
                for (int angle = 0; angle <= 180; angle++) {
                    int x = start.x + (int) (radius * Math.cos(Math.toRadians(angle)));
                    int y = start.y + (int) (radius * Math.sin(Math.toRadians(angle)));
                    barriers.add(new Point(x, y));
                }
            } else {
                // wall is a straight line
                for (int t = 0; t <= 100; t++) {
                    int x = start.x + t * (end.x - start.x) / 100;
                    int y = start.y + t * (end.y - start.y) / 100;
                    barriers.add(new Point(x, y));
                }
            }
        }
    }


    public Wall createWall(Point start, Point end) {
        Wall wall = new Wall(null, null);
        wall.start = start;
        wall.end = end;
        return wall;
    }

    public Wall createCurveD(Point start) {
        Wall wall = new Wall(null, null);
        wall.start = start;
        wall.isCurveD = true;
        return wall;
    }

    public Wall createCurvedU(Point start) {
        Wall wall = new Wall(null, null);
        wall.start = start;
        wall.isCurveU = true;
        return wall;
    }



    public class SimulatorPanel extends JPanel {

        private NeuralNetwork neuralNetwork = new NeuralNetwork(numInputs, numHiddenNeurons, numOutputs);
        private NeuralNetwork mutatedNeuralNetwork = new NeuralNetwork(numInputs, numHiddenNeurons, numOutputs);
        private GenerateInputVector generateInputVector = new GenerateInputVector();
        private GenerateOutputVector generateOutputVector = new GenerateOutputVector();

        public void moveCar() {

            increaseIteration();

            for (Car car: cars){
                if (!car.isDead){
                    sendNeuralNetworkInformation(car);
                    car.velocity += car.acceleration;
                    car.reward++;

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
            List<Double> newSensorDistances = new ArrayList<>(); // New list to store sensor distances
        
            int numSensors = 7; // Adjust the number of sensors as needed
            double startSensorAngle = Math.toRadians(-60); // Start angle for the first sensor
            double endSensorAngle = Math.toRadians(60); // End angle for the last sensor
            double angleIncrement = (endSensorAngle - startSensorAngle) / (numSensors - 1); // Angle increment between sensors
            double angleInRadians = Math.toRadians(car.angle);
        
            double maxSensorDistance = 300.0; // Maximum sensor distance
        
            for (int i = 0; i < numSensors; i++) {
                double sensorX = car.x + carWidth / 2.0;
                double sensorY = car.y + carHeight / 2.0;
        
                double sensorEndX = sensorX + Math.cos(angleInRadians + startSensorAngle) * maxSensorDistance;
                double sensorEndY = sensorY + Math.sin(angleInRadians + startSensorAngle) * maxSensorDistance;
        
                Point collisionPoint = calculateCollisionPoint(sensorX, sensorY, sensorEndX, sensorEndY, car);
        
                if (collisionPoint != null) {
                    // Check if the collision point is in front of the car
                    double angleToCollisionPoint = Math.atan2(collisionPoint.y - sensorY, collisionPoint.x - sensorX);
                    double angleDifference = Math.abs(Math.atan2(Math.sin(angleToCollisionPoint - angleInRadians),
                            Math.cos(angleToCollisionPoint - angleInRadians)));
        
                    // Adjust this threshold to control the sensitivity of the sensors
                    double angleThreshold = Math.toRadians(90);
        
                    if (angleDifference <= angleThreshold) {
                        // Limit the distance if it exceeds the maximum allowed distance
                        double distance = Math.sqrt(Math.pow(collisionPoint.x - sensorX, 2) + Math.pow(collisionPoint.y - sensorY, 2));
                        if (distance > maxSensorDistance) {
                            collisionPoint.x = (int) (sensorX + Math.cos(angleInRadians + startSensorAngle) * maxSensorDistance);
                            collisionPoint.y = (int) (sensorY + Math.sin(angleInRadians + startSensorAngle) * maxSensorDistance);
                            distance = maxSensorDistance;
                        }
                        newSensorCollisionPoints.add(collisionPoint);
                        newSensorDistances.add(distance); // Add the distance to the list
                    }
                } else {
                    newSensorCollisionPoints.add(new Point(-1, -1));
                    newSensorDistances.add(maxSensorDistance); 
                }
        
                startSensorAngle += angleIncrement;
            }
        
            car.sensorCollisionPoint = newSensorCollisionPoints;
            car.sensorDistance = newSensorDistances; // Set the list of sensor distances
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
            INDArray inputVector = generateInputVector.generateInputVector(car.sensorDistance, car.velocity, car.acceleration, car.neuralNetwork);
            int outputAction = generateOutputVector.generateOutputVector(inputVector, car.neuralNetwork);

            // Update information for predictive training
            outputList.add(generateOutputVector.outputVector);
            inputList.add(inputVector);
            actionList.add(outputAction);
            double adjustedTurnRate = baseCarTurnRate * (Math.abs(car.velocity * 0.5) / maxVelocity);

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


                for (int i = 5; i < numOfCars; i++){


                    neuralNetwork.crossoverNeuralNetwork(topCars.get(0).neuralNetwork, topCars.get(1).neuralNetwork);

                    
                    // half chance to mutate the crossover neural network
                    if (random.nextInt(3) == 0){
                        mutatedNeuralNetwork = neuralNetwork.mutateNeuralNetwork(neuralNetwork);
                        cars.get(i).neuralNetwork = mutatedNeuralNetwork;

                    }

                    // third chance of just getting the crossover neural network
                    else if (random.nextInt(3) == 0){
                        cars.get(i).neuralNetwork = neuralNetwork;
                    }
                    
                    // half chance to mutate one of the two top neural networks
                    else{
                        int rand = random.nextInt(2);
                        neuralNetwork = topCars.get(rand).neuralNetwork;
                        cars.get(i).neuralNetwork = neuralNetwork.mutateNeuralNetwork(neuralNetwork);

                    }

                }

                // add the top 2 neural networks to the next generation in case the mutations are worse
                cars.get(0).neuralNetwork = topCars.get(0).neuralNetwork;
                cars.get(1).neuralNetwork = topCars.get(0).neuralNetwork;
                cars.get(2).neuralNetwork = topCars.get(1).neuralNetwork;
                cars.get(3).neuralNetwork = topCars.get(1).neuralNetwork;

                // add 4 random neural networks to the next generation
                cars.get(4).neuralNetwork = new NeuralNetwork(numInputs, numHiddenNeurons, numOutputs);

            

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

        // find top 2 best cars
        private List<Car> findBestCar(){

            List<Car> carsCopy = new ArrayList<>(cars);
            List<Car> topCars = new ArrayList<>();
            Car topcar = null;

            for (int i = 0; i < 2; i++){
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

                if (car.isDead){
                    drawCar(offScreenGraphics, car);       
                }
                else{
                    drawSensorLines(offScreenGraphics, car);   
                    drawCar(offScreenGraphics, car);       
                }
            }
            
            g.drawImage(offScreenBuffer, 0, 0, this);
        }

        private void drawTrack(Graphics g) {
            // Draw race track
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, trackWidth, trackHeight);
        
            g.setColor(Color.WHITE);
            for (Wall wall : raceCourse) {
                if (wall.isCurveD || wall.isCurveU) {
                    // Assuming you have a radius for the curved wall

                    int x = wall.start.x - radius;
                    int y = wall.start.y - radius;
                    int width = 2 * radius;
                    int height = 2 * radius;
        
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
                        g.drawLine((int) car.x + carWidth / 2, (int) car.y + carHeight / 2,
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
