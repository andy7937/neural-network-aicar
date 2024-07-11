package GenerateVector;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import neuralNetwork.NeuralNetwork;

public class GenerateOutputVector {

    public static int numOutputs;
    public static int numInputs;
    public INDArray outputVector;

    public int generateOutputVector(INDArray input, NeuralNetwork neuralNetwork) {
        // Assuming the neural network has 2 outputs:
        // 1. A (turn left)
        // 2. D (turn right)
        // 3. Nothing (do nothing)


        // Get the neural network's predictions
        INDArray predictions = neuralNetwork.predict(input);

        // Choose the action based on the highest predicted probability
        int actionIndex = Nd4j.argMax(predictions, 1).getInt(0);

        outputVector = predictions;

        return actionIndex;
    }
}