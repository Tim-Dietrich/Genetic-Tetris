package org.tetris.simple.Network;

import lombok.extern.slf4j.Slf4j;

import static org.tetris.simple.Utils.Config.mutationRate;

@Slf4j
public class NeuralNetwork {
    int inputNodes, hiddenNodes, outputNodes, hiddenLayers;
    Weights[] networkMatrix;

    public NeuralNetwork(int inputNodes, int hiddenNodes, int outputNodes, int hiddenLayers) {
        this.inputNodes = inputNodes;
        this.hiddenNodes = hiddenNodes;
        this.outputNodes = outputNodes;
        this.hiddenLayers = hiddenLayers;

        networkMatrix = new Weights[hiddenLayers + 1]; // hidden layers + output layer
        networkMatrix[0] = new Weights(hiddenNodes, inputNodes + 1);
        for (int i = 1; i < hiddenLayers; i++) {
            networkMatrix[i] = new Weights(hiddenNodes, hiddenNodes + 1);
        }
        networkMatrix[networkMatrix.length - 1] = new Weights(outputNodes, hiddenNodes + 1);
    }

    public void rollInitialWeights() {
        for (Weights matrix : networkMatrix) {
            matrix.randomizeWeights();
        }
    }

    public double[] calculateOutputLayer(double[] input) {
        Weights inputs = new Weights(input);
        Weights currentBias = inputs.addBiasNode();

        for (int i = 0; i < hiddenLayers; i++) {
            Weights hiddenIn = networkMatrix[i].dot(currentBias);
            Weights hiddenOp = hiddenIn.activate();
            currentBias = hiddenOp.addBiasNode();
        }

        Weights output = networkMatrix[networkMatrix.length - 1].dot(currentBias);
        return output.toArray();
    }

    public void mutate() {
        for (Weights matrix : networkMatrix) {
            matrix.mutateWeights(mutationRate);
        }
    }

    public NeuralNetwork crossover(NeuralNetwork parent) {
        NeuralNetwork child = new NeuralNetwork(inputNodes, hiddenNodes, outputNodes, hiddenLayers);
        for (int i = 0; i < networkMatrix.length; i++) {
            child.networkMatrix[i] = networkMatrix[i].onePointCrossover(parent.networkMatrix[i]);
        }
        return child;
    }

    @Override
    public NeuralNetwork clone() {
        NeuralNetwork clone = new NeuralNetwork(inputNodes, hiddenNodes, outputNodes, hiddenLayers);
        for (int i = 0; i < networkMatrix.length; i++) {
            clone.networkMatrix[i] = networkMatrix[i].clone();
        }

        return clone;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Weights weights : networkMatrix) {
            sb.append(weights.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}
