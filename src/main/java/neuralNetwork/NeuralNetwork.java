package neuralNetwork;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class NeuralNetwork {

    private MultiLayerNetwork model;
    public static int numInputs;
    public static int numHiddenNeurons;
    public static int numOutputs;
    Random random = new Random(System.currentTimeMillis());


    public NeuralNetwork(int numInputs, int numHiddenNeurons, int numOutputs) {
        int i = random.nextInt(100000000);
        int j = random.nextInt(100000000);

        // Randomize the learning rate during initialization
        double learningRate = 0.01; // You can adjust the range as needed

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(i + j + 5)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(learningRate)) // Use the randomized learning rate
                .list()
                .layer(new DenseLayer.Builder()
                        .nIn(numInputs)
                        .nOut(numHiddenNeurons)
                        .activation(Activation.RELU)
                        .build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
                        .activation(Activation.SOFTMAX)
                        .nIn(numHiddenNeurons)
                        .nOut(numOutputs)
                        .build())
                .build();

        this.model = new MultiLayerNetwork(conf);
        this.model.init();
    }

    public NeuralNetwork mutateNeuralNetwork(NeuralNetwork model) {
        try {
            // Clone the model to create an independent copy
            MultiLayerNetwork mutatedModel = model.getModel().clone();
    
            // Perform mutation on the cloned model
            mutateModel(mutatedModel);
    
            // Create a new NeuralNetwork and set the mutated model
            NeuralNetwork mutatedNetwork = new NeuralNetwork(numInputs, numHiddenNeurons, numOutputs);
            mutatedNetwork.setModel(mutatedModel);
    
            return mutatedNetwork;
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Handle the exception appropriately in your application
        }
    }
    
    public void mutateModel(MultiLayerNetwork model) {
        // Implement your mutation logic here
        // Example: Perturb some of the weights in the model
        double mutationRate = 0.3; // Adjust the mutation rate as needed
    
        for (int i = 0; i < model.getLayers().length; i++) {
            INDArray weights = model.getLayer(i).getParam("W");
            for (int j = 0; j < weights.length(); j++) {
                if (random.nextDouble() < mutationRate) {
                    // Perturb the weight with a small random value
                    weights.putScalar(j, weights.getDouble(j) + random.nextGaussian() * 0.05);
                }
            }
        }
    }
    
    public NeuralNetwork crossoverNeuralNetwork(NeuralNetwork model1, NeuralNetwork model2) {
        try {
            // Clone the models of the parents
            MultiLayerNetwork clonedModel1 = model1.getModel().clone();
            MultiLayerNetwork clonedModel2 = model2.getModel().clone();

    
            // Crossover the results of the first two crossovers
            MultiLayerNetwork crossoveredModel = crossoverModels(clonedModel1, clonedModel2);
    
            // Create a new NeuralNetwork and set the crossovered model
            NeuralNetwork crossoveredNetwork = new NeuralNetwork(numInputs, numHiddenNeurons, numOutputs);
            crossoveredNetwork.setModel(crossoveredModel);
    
            return crossoveredNetwork;
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Handle the exception appropriately in your application
        }
    }
    
    public MultiLayerNetwork crossoverModels(MultiLayerNetwork model1, MultiLayerNetwork model2) {
        // Implement your crossover logic here
        // Example: Exchange some of the weights between the two models
        for (int i = 0; i < model1.getLayers().length; i++) {
            INDArray weights1 = model1.getLayer(i).getParam("W");
            INDArray weights2 = model2.getLayer(i).getParam("W");
    
    
            performCrossover(weights1, weights2);
        }
    
        return model1; // Return one of the models (you may adjust this based on your logic)
    }
    
    public void performCrossover(INDArray weights1, INDArray weights2) {
        // Implement your specific crossover strategy here
        // Example: Exchange some of the weights between the two models
        for (int i = 0; i < weights1.length(); i++) {
            if (random.nextBoolean()) {
                // Swap some of the weights between the two models
                double temp = weights1.getDouble(i);
                weights1.putScalar(i, weights2.getDouble(i));
                weights2.putScalar(i, temp);
            }
        }
    }

    // Get the model of the neural network
    public MultiLayerNetwork getModel() {
        return model;
    }
    
    // Train the neural network with a single step of simulation data
    public INDArray trainStep(INDArray input, INDArray target) {
        model.fit(input, target);
        // Assuming the output of the neural network is needed after training
        return model.output(input);
    }

    // Predict based on the current state of the neural network
    public INDArray predict(INDArray input) {
        return model.output(input);
    }

    // Get eh weights of the input layer
    public INDArray getInputWeights() {
        return model.getLayer(0).getParam("W");
    }

    // Get the weights of the output layer
    public INDArray getOutputWeights() {
        return model.getLayer(1).getParam("W");
    }

    public NeuralNetwork clone() {
        try {
            // Clone the model to create an independent copy
            MultiLayerNetwork clonedModel = model.clone();

            // Create a new BlobNeuralNetwork and set the cloned model
            NeuralNetwork clonedNetwork = new NeuralNetwork(numInputs, numHiddenNeurons, numOutputs); // Adjust the parameters as needed
            clonedNetwork.setModel(clonedModel);

            return clonedNetwork;
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Handle the exception appropriately in your application
        }
    }

    public void setModel(MultiLayerNetwork model) {
        this.model = model;
    }

    // Save and load methods remain the same

    public void saveModel(String path) {
        String modelsFolderPath = "models/";
        String fullPath = modelsFolderPath + path;
    
        try {
            // Create the "models" folder if it doesn't exist
            File modelsFolder = new File(modelsFolderPath);
            if (!modelsFolder.exists()) {
                modelsFolder.mkdir();
            }
    
            // Save the model in the "models" folder
            model.save(new File(fullPath), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadModel(String path) {
        String modelsFolderPath = "models/";
        String fullPath = modelsFolderPath + path;
    
        try {
            // Load the model from the "models" folder
            model = MultiLayerNetwork.load(new File(fullPath), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}