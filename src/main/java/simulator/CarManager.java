package simulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.nd4j.linalg.api.ndarray.INDArray;

import GenerateVector.GenerateInputVector;
import GenerateVector.GenerateOutputVector;
import neuralNetwork.NeuralNetwork;

public class CarManager {

    private SimulatorConfig config = SimulatorConfig.getInstance();
    private NeuralNetwork neuralNetwork = new NeuralNetwork(config.numInputs, config.numHiddenNeurons, config.numOutputs);
    private NeuralNetwork mutatedNeuralNetwork = new NeuralNetwork(config.numInputs, config.numHiddenNeurons, config.numOutputs);
    private GenerateInputVector generateInputVector = new GenerateInputVector();
    private GenerateOutputVector generateOutputVector = new GenerateOutputVector();
    Random random = new Random(System.currentTimeMillis());

    public void moveCar() {

        increaseIteration();

        for (Car car: config.cars){
            if (!car.isDead){
                sendNeuralNetworkInformation(car);
                car.velocity += car.acceleration;
                car.reward++;

                // Apply friction to simulate deceleration
                if (car.acceleration == 0) {
                    if (car.velocity > 0) {
                        car.velocity -= config.friction;
                    } else if (car.velocity < 0) {
                        car.velocity += config.friction;
                    }
                }

                if (car.velocity < 0.05 && car.velocity > -0.05){
                    car.velocity = 0;
                }

                // Limit velocity to maxVelocity
                car.velocity = Math.min(config.maxVelocity, Math.max(-config.maxVelocity, car.velocity));

                            // Update position based on velocity and angle
                double angleInRadians = Math.toRadians(car.angle);
                car.x += car.velocity * Math.cos(angleInRadians);
                car.y += car.velocity * Math.sin(angleInRadians);

            
                for (Point barrier : config.barriers) {
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
                        } else if (car.x > config.trackWidth - config.carWidth) {
                            handleCollision(car);
                        }
                
                        if (car.y < 0) {
                            handleCollision(car);
                        } else if (car.y > config.trackHeight - config.carHeight) {
                            handleCollision(car);
                        }
                    }
                }
            }
        }
    }

public void calculateSensorCollisionPoints(Car car, List<Point> barriers, List<Wall> raceCourse) {
            List<Point> newSensorCollisionPoints = new ArrayList<>();
            List<Double> newSensorDistances = new ArrayList<>(); // New list to store sensor distances
        
            int numSensors = 7; // Adjust the number of sensors as needed
            double startSensorAngle = Math.toRadians(-60); // Start angle for the first sensor
            double endSensorAngle = Math.toRadians(60); // End angle for the last sensor
            double angleIncrement = (endSensorAngle - startSensorAngle) / (numSensors - 1); // Angle increment between sensors
            double angleInRadians = Math.toRadians(car.angle);
        
            double maxSensorDistance = 300.0; // Maximum sensor distance
        
            for (int i = 0; i < numSensors; i++) {
                double sensorX = car.x + config.carWidth / 2.0;
                double sensorY = car.y + config.carHeight / 2.0;
        
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
        
        
        public Point calculateCollisionPoint(double startX, double startY, double endX, double endY, Car car) {
            Point closestCollisionPoint = null;
            double closestDistance = Double.MAX_VALUE;
        
            for (Point barrier : config.barriers) {
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
        
        public boolean lineIntersectsCircle(double startX, double startY, double endX, double endY, double circleX, double circleY, double radius) {
            double dx = endX - startX;
            double dy = endY - startY;
            double a = dx * dx + dy * dy;
            double b = 2 * (dx * (startX - circleX) + dy * (startY - circleY));
            double c = circleX * circleX + circleY * circleY + startX * startX + startY * startY
                    - 2 * (circleX * startX + circleY * startY) - radius * radius;
        
            return b * b - 4 * a * c >= 0;
        }

        public void sendNeuralNetworkInformation(Car car){
            // Update the neural network based on the total distance traveled
            INDArray inputVector = generateInputVector.generateInputVector(car.sensorDistance, car.velocity, car.acceleration, car.neuralNetwork);
            int outputAction = generateOutputVector.generateOutputVector(inputVector, car.neuralNetwork);

            // Update information for predictive training
            config.outputList.add(generateOutputVector.outputVector);
            config.inputList.add(inputVector);
            config.actionList.add(outputAction);
            double adjustedTurnRate = config.baseCarTurnRate * (Math.abs(car.velocity * 0.5) / config.maxVelocity);

            // Update the neural network based on the output given
            switch (outputAction) {
                case 0:
                    car.angle -= adjustedTurnRate;                     
                    break;
                case 1:
                    car.angle += adjustedTurnRate;                     
                    break;
                case 2:
                    break;
                default:
                    break;    
            }

            car.acceleration = config.accelerationRate;
        }
        

        public void handleCollision(Car car){
            System.out.println("Collision detected!");

            car.isDead = true;

            // Reset car position and total distance traveled
            car.velocity = 0;
            car.acceleration = 0;
        }

        public boolean isAllCarsDead(){
            for (Car car : config.cars){
                if (!car.isDead){
                    return false;
                }
            }
            return true;
        }

        public void increaseIteration(){
            System.out.println("Iteration: " + config.iteration);
            config.iteration++;
        
            // if all cars are dead or iteration is greater than maxIterations, update neural networks and get next generation
            if (config.iteration >= config.maxIterations || isAllCarsDead()){
                List<Car> topCars = findBestCar();
                System.out.println("Generation: " + config.generation);


                for (int i = 5; i < config.numOfCars; i++){


                    neuralNetwork.crossoverNeuralNetwork(topCars.get(0).neuralNetwork, topCars.get(1).neuralNetwork);

                    
                    // half chance to mutate the crossover neural network
                    if (random.nextInt(3) == 0){
                        mutatedNeuralNetwork = neuralNetwork.mutateNeuralNetwork(neuralNetwork);
                        config.cars.get(i).neuralNetwork = mutatedNeuralNetwork;

                    }

                    // third chance of just getting the crossover neural network
                    else if (random.nextInt(3) == 0){
                        config.cars.get(i).neuralNetwork = neuralNetwork;
                    }
                    
                    // half chance to mutate one of the two top neural networks
                    else{
                        int rand = random.nextInt(2);
                        neuralNetwork = topCars.get(rand).neuralNetwork;
                        config.cars.get(i).neuralNetwork = neuralNetwork.mutateNeuralNetwork(neuralNetwork);

                    }

                }

                // add the top 2 neural networks to the next generation in case the mutations are worse
                config.cars.get(0).neuralNetwork = topCars.get(0).neuralNetwork;
                config.cars.get(1).neuralNetwork = topCars.get(0).neuralNetwork;
                config.cars.get(2).neuralNetwork = topCars.get(1).neuralNetwork;
                config.cars.get(3).neuralNetwork = topCars.get(1).neuralNetwork;

                // add 4 random neural networks to the next generation
                config.cars.get(4).neuralNetwork = new NeuralNetwork(config.numInputs, config.numHiddenNeurons, config.numOutputs);

            

                config.iteration = 0;
                config.generation++;
                resetCars();

            }

            if (config.generation >= config.maxGenerations){
                System.out.println("Done");
                System.exit(0);
            }

        }

          // find top 2 best cars
          private List<Car> findBestCar(){

            List<Car> carsCopy = new ArrayList<>(config.cars);
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
            for (Car car : config.cars){
                car.x = config.carX;
                car.y = config.carY;
                car.angle = config.carAngle;
                car.isDead = false;
                car.acceleration = 0;
                car.velocity = 0;
                car.reward = 0;
                config.inputList.clear();
                config.outputList.clear();
                config.actionList.clear();
            }
        }
}
