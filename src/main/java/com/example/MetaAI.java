package com.example;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

public class MetaAI {

    Neuralnetwork network;
    Cache<String, double[]> trainingCache;

    public MetaAI(Neuralnetwork network) {
        this.network = network;

        this.trainingCache = Caffeine.newBuilder()
                .maximumSize(50)
                .removalListener((String key, double[] data, com.github.benmanes.caffeine.cache.RemovalCause cause) -> {
                    if (data != null) {
                        dumpToDatabase(data[0], data[1], data[2]);
                    }
                })
                .build();
    }

    public void setupDatabase() {
        try {
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/",
                    "root",
                    "JohnRonao"
            );

            Statement statement = connection.createStatement();

            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS learning_data");
            statement.executeUpdate("USE learning_data");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS training_table (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "`first_value` DOUBLE, " +
                    "`second_value` DOUBLE, " +
                    "`target_result` DOUBLE" +
                    ")");

            connection.close();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void learnFromDatabase() {
        try {
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/learning_data",
                    "root",
                    "JohnRonao"
            );

            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery("SELECT `first_value`, `second_value`, `target_result` FROM training_table");

            while (results.next()) {
                double firstInput = results.getDouble("first_value");
                double secondInput = results.getDouble("second_value");
                double target = results.getDouble("target_result");

                trainNetwork(firstInput, secondInput, target);
            }

            connection.close();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void trainOnRound(double firstInput, double secondInput, double targetOutput) {
        trainNetwork(firstInput, secondInput, targetOutput);

        trainingCache.put(UUID.randomUUID().toString(), new double[]{firstInput, secondInput, targetOutput});
    }

    public void forceDumpCache() {
        trainingCache.invalidateAll();
        trainingCache.cleanUp();
    }

    private void dumpToDatabase(double firstInput, double secondInput, double targetOutput) {
        try {
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/learning_data",
                    "root",
                    "JohnRonao"
            );
            Statement statement = connection.createStatement();
            statement.executeUpdate("INSERT INTO training_table (`first_value`, `second_value`, `target_result`) VALUES (" + firstInput + ", " + secondInput + ", " + targetOutput + ")");
            connection.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void trainNetwork(double firstInput, double secondInput, double target) {
        double[] currentInputs = {firstInput, secondInput};
        double[] currentOutputs = network.processInformation(currentInputs);

        double difference = target - currentOutputs[0];
        double learningRate = 0.5;

        for (int i = 0; i < network.firstToMiddleConnections.length; i++) {
            for (int j = 0; j < network.firstToMiddleConnections[i].length; j++) {
                network.firstToMiddleConnections[i][j] += difference * learningRate;
            }
        }

        for (int i = 0; i < network.middleToFinalConnections.length; i++) {
            for (int j = 0; j < network.middleToFinalConnections[i].length; j++) {
                network.middleToFinalConnections[i][j] += difference * learningRate;
            }
        }

        for (int i = 0; i < network.middleAdjustments.length; i++) {
            network.middleAdjustments[i] += difference * learningRate;
        }

        for (int i = 0; i < network.finalAdjustments.length; i++) {
            network.finalAdjustments[i] += difference * learningRate;
        }
    }
}