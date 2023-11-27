package GenerateVector;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import neuralNetwork.NeuralNetwork;

public class GenerateOutputVector {

    public static int numOutputs;
    public static int numInputs;
    public INDArray outputVector;

    public int generateOutputVector(INDArray input, NeuralNetwork neuralNetwork) {
        // Assuming the neural network has 5 outputs:
        // 0. W (move forward)
        // 1. A (turn left)
        // 2. S (move backward)
        // 3. D (turn right)
        // 4. space (stop)

        // Initialize the output vector with zeros
        INDArray output = Nd4j.zeros(1, numInputs);

        // Get the neural network's predictions
        INDArray predictions = neuralNetwork.predict(output);/* Call your neural network's predict method with the input */;
        outputVector = predictions;

        // Choose the action based on the highest predicted probability
        int actionIndex = Nd4j.argMax(predictions, 1).getInt(0);

        // Set the corresponding element in the output vector to 1

        return actionIndex;
    }
}
