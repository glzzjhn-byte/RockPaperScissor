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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

public class MetaAI {

    Neuralnetwork network;
    Cache<String, double[]> trainingCache;

    /**
     * Initializes the MetaAI system and sets up an in-memory short-term cache using Caffeine.
     */
    public MetaAI(Neuralnetwork network) {
        this.network = network;

        this.trainingCache = Caffeine.newBuilder()
                .maximumSize(50)
                .executor(Runnable::run)
                .removalListener((String key, double[] data, com.github.benmanes.caffeine.cache.RemovalCause cause) -> {
                    if (data != null) {
                        dumpToDatabase(data);
                    }
                })
                .build();
    }

    /**
     * Establishes a connection to the SQLite database and ensures the necessary training table exists.
     */
    public void setupDatabase() {
        try (Connection connection = DriverManager.getConnection(
                    "jdbc:sqlite:learning_data.db");
             Statement statement = connection.createStatement()) {

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS training_table_v2 (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "`m1` DOUBLE, " +
                    "`m2` DOUBLE, " +
                    "`m3` DOUBLE, " +
                    "`target_result` DOUBLE" +
                    ")");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Starts the deep learning process using historical database records with a default of 100 epochs.
     */
    public void learnFromDatabase() {
        learnFromDatabase(100);
    }

    /**
     * Retrieves past moves from the database, merges them with the uncommitted memory cache, 
     * and trains the neural network over the specified number of epochs.
     */
    public void learnFromDatabase(int epochs) {
        java.util.List<double[]> inputsList = new java.util.ArrayList<>();
        java.util.List<Double> targetsList = new java.util.ArrayList<>();
        try (Connection connection = DriverManager.getConnection(
                    "jdbc:sqlite:learning_data.db");
             Statement statement = connection.createStatement();
             ResultSet results = statement.executeQuery("SELECT `m1`, `m2`, `m3`, `target_result` FROM (SELECT * FROM training_table_v2 ORDER BY id DESC LIMIT 300) AS sub ORDER BY id ASC")) {

            while (results.next()) {
                inputsList.add(new double[]{results.getDouble("m1"), results.getDouble("m2"), results.getDouble("m3")});
                targetsList.add(results.getDouble("target_result"));
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        for (double[] cachedData : trainingCache.asMap().values()) {
            if (cachedData != null && cachedData.length == 4) {
                inputsList.add(new double[]{cachedData[0], cachedData[1], cachedData[2]});
                targetsList.add(cachedData[3]);
            }
        }

        for (int i = 0; i < epochs; i++) {
            for (int j = 0; j < inputsList.size(); j++) {
                trainNetwork(inputsList.get(j), targetsList.get(j));
            }
        }
    }

    /**
     * Registers the current round into the short-term cache and immediately trains the network on it.
     */
    public void trainOnRound(double[] history, double targetOutput) {
        trainNetwork(history, targetOutput);
        
        trainingCache.put(UUID.randomUUID().toString(), new double[]{history[0], history[1], history[2], targetOutput});
    }

    /**
     * Invalidates the short-term cache, forcing uncommitted memory to be saved directly to the database.
     */
    public void forceDumpCache() {
        trainingCache.invalidateAll();
        trainingCache.cleanUp();
    }

    /**
     * Inserts a single array of cached move data into the SQLite database for long-term storage.
     */
    private void dumpToDatabase(double[] data) {
        try (Connection connection = DriverManager.getConnection(
                    "jdbc:sqlite:learning_data.db");
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("INSERT INTO training_table_v2 (`m1`, `m2`, `m3`, `target_result`) VALUES (" + data[0] + ", " + data[1] + ", " + data[2] + ", " + data[3] + ")");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Executes gradient descent by passing inputs and expected targets directly to the neural network.
     */
    private void trainNetwork(double[] currentInputs, double target) {
        double[] targets = {target};
        
        double learningRate = 0.5; 
        
        network.train(currentInputs, targets, learningRate);
    }
}