package GenerateVector;

import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import neuralNetwork.NeuralNetwork;


public class GenerateInputVector {

    public static int numInputs;

    public INDArray generateInputVector(List<Double> sensorDistance, double carVelocity, double carAcceleration, NeuralNetwork neuralNetwork) {

        // Assuming the neural network has  inputs:
        // 0. sensor 1 distance
        // 1. sensor 2 distance
        // 2. sensor 3 distance 
        // 3. sensor 4 distance
        // 4. sensor 5 distance
        // 5. sensor 6 distance
        // 6. sensor 7 distance
        // 7. car velocity
        // 8. car acceleration




    // Initialize the input vector with zeros
    INDArray inputVector = Nd4j.zeros(1, numInputs);

    // Set the sensor values
    for (int i = 0; i < sensorDistance.size(); i++) {
        Double sensorPoint = sensorDistance.get(i);
        inputVector.putScalar(i, normalize(sensorPoint, 0, 300));
    }

    // Set the car velocity
    inputVector.putScalar(7, normalize(carVelocity, 0, 60));

    // Set the car acceleration
    inputVector.putScalar(8, normalize(carAcceleration, -10, 10));


    return inputVector;

    }

    private double normalize(double value, double min, double max) {
        return (value - min) / (max - min);
    }
    
}
