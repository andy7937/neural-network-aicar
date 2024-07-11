package simulator;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import org.nd4j.linalg.api.ndarray.INDArray;

public class SimulatorConfig {
    private static SimulatorConfig instance;

    public double carX = 100, carY = 123; // Car position (double for smoother movements)
    public double carVelocity = 0; // Car speed
    public double carAcceleration = 0; // Car acceleration
    public double carAngle = 90; // Car angle in degrees
    public final int baseCarTurnRate = 10; // Car turning speed
    public final double maxVelocity = 10; // Maximum velocity
    public final double accelerationRate = 0.075; // Acceleration rate
    public final double velocityDecayRate = 0.5; // Velocity decay rate
    public final double friction = 0.02; // Friction to simulate deceleration
    public final int trackWidth = 1920;
    public final int trackHeight = 1080;
    public final int carWidth = 40;
    public final int carHeight = 20;
    public final int delay = 10; // Delay in milliseconds for the timer

    public List<Wall> raceCourse = new ArrayList<>();
    public List<Point> barriers = new ArrayList<>();
    public BufferedImage offScreenBuffer;
    public Point firstClick;
    public int numInputs = 7;
    public int numHiddenNeurons = 5;
    public int numOutputs = 3;
    public List<INDArray> outputList = new ArrayList<>();
    public List<INDArray> inputList = new ArrayList<>();
    public List<Integer> actionList = new ArrayList<>();
    public List<Point> pointsCreated = new ArrayList<>();
    public List<Car> cars = new ArrayList<>();
    public int numOfCars = 50;
    public int iteration = 0;
    public int maxIterations = 500;
    public int generation = 0;
    public int maxGenerations = 1000;
    public int radius = 180;
    public boolean isWallAdded = false;

    private SimulatorConfig() {
        // Private constructor to prevent instantiation
    }

    public static SimulatorConfig getInstance() {
        if (instance == null) {
            instance = new SimulatorConfig();
        }
        return instance;
    }
}
