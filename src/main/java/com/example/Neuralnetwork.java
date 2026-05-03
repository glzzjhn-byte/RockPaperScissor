/*
 * Copyright (c) 2026 John Gabriel Ronao
 *
 * Special thanks to AI tools for designing the JavaFX UI, 
 * allowing me to focus much more on the backend logic!
 * 
 * Deep Learning Math Formulas applied in this project:
 * - Sigmoid Activation: https://en.wikipedia.org/wiki/Sigmoid_function 
 * - Backpropagation: https://en.wikipedia.org/wiki/Backpropagation
 * - Gradient Descent: https://en.wikipedia.org/wiki/Gradient_descent
 * 
 * Open Source 📖
 * 
 */
package com.example;

public class Neuralnetwork {

    double[] middleLayer;
    double[] finalLayer;

    double[][] firstToMiddleConnections;
    double[][] middleToFinalConnections;

    double[] middleAdjustments;
    double[] finalAdjustments;

    /**
     * Initializes the network layers, weights (connections), and biases (adjustments) with random values.
     */
    public Neuralnetwork(int startingPoints, int middlePoints, int endingPoints) {
        middleLayer = new double[middlePoints];
        finalLayer = new double[endingPoints];

        firstToMiddleConnections = new double[startingPoints][middlePoints];
        middleToFinalConnections = new double[middlePoints][endingPoints];

        middleAdjustments = new double[middlePoints];
        finalAdjustments = new double[endingPoints];

        for (int i = 0; i < startingPoints; i++) {
            for (int j = 0; j < middlePoints; j++) {
                firstToMiddleConnections[i][j] = Math.random() - 0.5;
            }
        }

        for (int i = 0; i < middlePoints; i++) {
            for (int j = 0; j < endingPoints; j++) {
                middleToFinalConnections[i][j] = Math.random() - 0.5;
            }
        }

        for (int i = 0; i < middlePoints; i++) {
            middleAdjustments[i] = Math.random() - 0.5;
        }

        for (int i = 0; i < endingPoints; i++) {
            finalAdjustments[i] = Math.random() - 0.5;
        }
    }

    /**
     * Executes a feed-forward pass of the neural network.
     * Calculates layer activations based on inputs and returns the final layer's predictions.
     */
    public double[] processInformation(double[] givenInformation) {
        for (int i = 0; i < middleLayer.length; i++) {
            double totalValue = middleAdjustments[i];
            for (int j = 0; j < givenInformation.length; j++) {
                totalValue += givenInformation[j] * firstToMiddleConnections[j][i];
            }
            middleLayer[i] = 1 / (1 + Math.exp(-totalValue));
        }

        for (int i = 0; i < finalLayer.length; i++) {
            double totalValue = finalAdjustments[i];
            for (int j = 0; j < middleLayer.length; j++) {
                totalValue += middleLayer[j] * middleToFinalConnections[j][i];
            }
            finalLayer[i] = 1 / (1 + Math.exp(-totalValue));
        }

        return finalLayer;
    }

    /**
     * Applies backpropagation using gradient descent to adjust network weights and biases.
     * Minimizes the error between the network's output and the expected targets.
     */
    public void train(double[] inputs, double[] targets, double learningRate) {
        processInformation(inputs);

        double[] outputDeltas = new double[finalLayer.length];
        for (int i = 0; i < finalLayer.length; i++) {
            double error = targets[i] - finalLayer[i];
            outputDeltas[i] = error * finalLayer[i] * (1 - finalLayer[i]);
        }

        double[] hiddenDeltas = new double[middleLayer.length];
        for (int i = 0; i < middleLayer.length; i++) {
            double error = 0.0;
            for (int j = 0; j < finalLayer.length; j++) {
                error += outputDeltas[j] * middleToFinalConnections[i][j];
            }
            hiddenDeltas[i] = error * middleLayer[i] * (1 - middleLayer[i]);
        }

        for (int i = 0; i < middleLayer.length; i++) {
            for (int j = 0; j < finalLayer.length; j++) {
                middleToFinalConnections[i][j] += learningRate * outputDeltas[j] * middleLayer[i];
            }
        }
        for (int i = 0; i < finalLayer.length; i++) {
            finalAdjustments[i] += learningRate * outputDeltas[i];
        }

        for (int i = 0; i < inputs.length; i++) {
            for (int j = 0; j < middleLayer.length; j++) {
                firstToMiddleConnections[i][j] += learningRate * hiddenDeltas[j] * inputs[i];
            }
        }
        for (int i = 0; i < middleLayer.length; i++) {
            middleAdjustments[i] += learningRate * hiddenDeltas[i];
        }
    }
}