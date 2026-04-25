package com.example;

public class Neuralnetwork {

    double[] firstLayer;
    double[] middleLayer;
    double[] finalLayer;

    double[][] firstToMiddleConnections;
    double[][] middleToFinalConnections;

    double[] middleAdjustments;
    double[] finalAdjustments;

    public Neuralnetwork(int startingPoints, int middlePoints, int endingPoints) {
        firstLayer = new double[startingPoints];
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
}