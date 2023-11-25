package GenerateVector;

import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;


public class GenerateInputVector {

    public static int numOfInputSensors;

    public INDArray generateInputVector() {

        // Initialize the input vector with zeros
        INDArray inputVector = Nd4j.zeros(1, numOfInputSensors);

        return inputVector;

    }
    
}
