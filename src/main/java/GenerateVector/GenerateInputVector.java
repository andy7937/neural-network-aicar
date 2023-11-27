package GenerateVector;

import simulator.Point;
import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import neuralNetwork.NeuralNetwork;


public class GenerateInputVector {

    public static int numInputs;

    public INDArray generateInputVector(List<Point> sensorCollisionPoints, double carVelocity, double carAcceleration, NeuralNetwork neuralNetwork) {

        // Assuming the neural network has  inputs:
        // 0. sensor 1 x
        // 1. sensor 1 y
        // 2. sensor 2 x
        // 3. sensor 2 y
        // 4. sensor 3 x
        // 5. sensor 3 y
        // 6. sensor 4 x
        // 7. sensor 4 y
        // 8. sensor 5 x
        // 9. sensor 5 y
        // 10. sensor 6 x
        // 11. sensor 6 y
        // 12. sensor 7 x
        // 13. sensor 7 y
        // 14. car velocity
        // 15. car acceleration


        // Initialize the input vector with zeros
        INDArray inputVector = Nd4j.zeros(1, numInputs);

        // Set the sensor values
        inputVector.putScalar(0, normalize(sensorCollisionPoints.get(0).x, 0, 1920));
        inputVector.putScalar(1, normalize(sensorCollisionPoints.get(0).y, 0, 1080));
        inputVector.putScalar(2, normalize(sensorCollisionPoints.get(1).x, 0, 1920));
        inputVector.putScalar(3, normalize(sensorCollisionPoints.get(1).y, 0, 1080));
        inputVector.putScalar(4, normalize(sensorCollisionPoints.get(2).x, 0, 1920));
        inputVector.putScalar(5, normalize(sensorCollisionPoints.get(2).y, 0, 1080));
        inputVector.putScalar(6, normalize(sensorCollisionPoints.get(3).x, 0, 1920));
        inputVector.putScalar(7, normalize(sensorCollisionPoints.get(3).y, 0, 1080));
        inputVector.putScalar(8, normalize(sensorCollisionPoints.get(4).x, 0, 1920));
        inputVector.putScalar(9, normalize(sensorCollisionPoints.get(4).y, 0, 1080));
        inputVector.putScalar(10, normalize(sensorCollisionPoints.get(5).x, 0, 1920));
        inputVector.putScalar(11, normalize(sensorCollisionPoints.get(5).y, 0, 1080));
        inputVector.putScalar(12, normalize(sensorCollisionPoints.get(6).x, 0, 1920));
        inputVector.putScalar(13, normalize(sensorCollisionPoints.get(6).y, 0, 1080));

        // Set the car velocity and acceleration
        inputVector.putScalar(14, normalize(carVelocity, 0 , 80));
        inputVector.putScalar(15, normalize(carAcceleration, 0, 80));

        return inputVector;

    }

    private double normalize(double value, double min, double max) {
        return (value - min) / (max - min);
    }
    
}
