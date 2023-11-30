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



    // Initialize the input vector with zeros
    INDArray inputVector = Nd4j.zeros(1, numInputs);

    // Set the sensor values
    for (int i = 0; i < Math.min(sensorCollisionPoints.size(), 7); i++) {
        Point sensorPoint = sensorCollisionPoints.get(i);

        if (sensorPoint.x == 0 && sensorPoint.y == 0) {
            inputVector.putScalar(i * 2, -1);
            inputVector.putScalar(i * 2 + 1, -1);

        }else{
            inputVector.putScalar(i * 2, normalize(sensorPoint.x, 0, 1920));
            inputVector.putScalar(i * 2 + 1, normalize(sensorPoint.y, 0, 1080));

        }



    }


    return inputVector;

    }

    private double normalize(double value, double min, double max) {
        return (value - min) / (max - min);
    }
    
}
