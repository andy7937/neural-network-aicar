package GenerateVector;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class GenerateOutputVector {

        public INDArray generateOutputVector(INDArray input) {

        // Assuming the neural network has 6 outputs:
        // 0. Move left
        // 1. Move right
        // 2. Move up
        // 3. Move down
        // 4. Move top left
        // 5. Move top right
        // 6. Move bottom left
        // 7. Move bottom right
        // 8. Eat everything adjacent
        // 9. Random Movement    
        // 10. Do nothing
        int numOutputs = 11;
        INDArray output = Nd4j.zeros(1, numOutputs);

        return output;
 
    }


    
}
