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

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class RockPaperScissorsApp extends Application {
    private Neuralnetwork network;
    private MetaAI ai;
    private final double[] moveHistory = {0.0, 0.0, 0.0};

    private Label resultLabel;
    private Label computerChoiceLabel;
    private Button rockButton;
    private Button paperButton;
    private Button scissorsButton;

    /**
     * Initializes the JavaFX UI elements, layout, and instantiates the AI components.
     */
    @Override
    public void start(Stage primaryStage) {
        network = new Neuralnetwork(3, 8, 1);
        ai = new MetaAI(network);
        ai.setupDatabase();
        ai.learnFromDatabase();

        primaryStage.setTitle("Rock Paper Scissors AI");

        computerChoiceLabel = new Label("Computer chose: None");
        computerChoiceLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        
        resultLabel = new Label("Make your move!");
        resultLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));

        VBox infoBox = new VBox(20, computerChoiceLabel, resultLabel);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setPadding(new Insets(40, 20, 20, 20));

        rockButton = new Button("Rock");
        paperButton = new Button("Paper");
        scissorsButton = new Button("Scissors");

        rockButton.setMaxWidth(Double.MAX_VALUE);
        paperButton.setMaxWidth(Double.MAX_VALUE);
        scissorsButton.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(rockButton, Priority.ALWAYS);
        HBox.setHgrow(paperButton, Priority.ALWAYS);
        HBox.setHgrow(scissorsButton, Priority.ALWAYS);

        String buttonStyle = "-fx-font-size: 16px; -fx-padding: 15; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;";
        rockButton.setStyle(buttonStyle + "-fx-background-color: #ff7675; -fx-text-fill: white;");
        paperButton.setStyle(buttonStyle + "-fx-background-color: #74b9ff; -fx-text-fill: white;");
        scissorsButton.setStyle(buttonStyle + "-fx-background-color: #55efc4; -fx-text-fill: #2d3436;");

        rockButton.setOnAction(e -> playRound(1));
        paperButton.setOnAction(e -> playRound(2));
        scissorsButton.setOnAction(e -> playRound(3));

        HBox buttonBox = new HBox(15, rockButton, paperButton, scissorsButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 30, 30, 30));

        BorderPane root = new BorderPane();
        root.setCenter(infoBox);
        root.setBottom(buttonBox);
        root.setStyle("-fx-background-color: #f5f6fa;");

        Scene scene = new Scene(root, 550, 300);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(450);
        primaryStage.setMinHeight(250);

        primaryStage.setOnCloseRequest(e -> {
            ai.forceDumpCache();
            Platform.exit();
        });

        primaryStage.show();
    }

    /**
     * Executes a game round. Disables the UI, runs deep learning epochs asynchronously, 
     * predicts the computer's move, calculates the winner, and updates the state.
     */
    private void playRound(int userChoice) {
        rockButton.setDisable(true);
        paperButton.setDisable(true);
        scissorsButton.setDisable(true);
        
        computerChoiceLabel.setText("AI is analyzing past moves...");
        resultLabel.setText("Deep learning in progress...");
        resultLabel.setTextFill(Color.web("#34495e"));

        final double[] inputData = { moveHistory[0], moveHistory[1], moveHistory[2] };

        new Thread(() -> {
            ai.learnFromDatabase(500);

            try { Thread.sleep(400); } catch (InterruptedException ignored) {}

            double[] aiOutput = network.processInformation(inputData);

            int computerChoice = (int) (aiOutput[0] * 3) + 1;
            if (computerChoice > 3) computerChoice = 3;
            else if (computerChoice < 1) computerChoice = 1;

            final String computerMove = switch (computerChoice) {
                case 1 -> "Rock";
                case 2 -> "Paper";
                case 3 -> "Scissors";
                default -> "";
            };

            int winningMove = (userChoice % 3) + 1;
            final double targetOutput = switch (winningMove) {
                case 1 -> 0.1;
                case 2 -> 0.5;
                case 3 -> 0.9;
                default -> 0.5;
            };

            ai.trainOnRound(inputData, targetOutput);

            final int finalComputerChoice = computerChoice;
            Platform.runLater(() -> {
                computerChoiceLabel.setText("Computer chose: " + computerMove);

                if (userChoice == finalComputerChoice) {
                    resultLabel.setText("Result: Tie");
                    resultLabel.setTextFill(Color.web("#feca57")); // Yellow
                } else if ((userChoice == 1 && finalComputerChoice == 3) ||
                        (userChoice == 2 && finalComputerChoice == 1) ||
                        (userChoice == 3 && finalComputerChoice == 2)) {
                    resultLabel.setText("Result: You win!");
                    resultLabel.setTextFill(Color.web("#1dd1a1")); // Green
                } else {
                    resultLabel.setText("Result: Computer wins");
                    resultLabel.setTextFill(Color.web("#ff6b6b")); // Red
                }

                moveHistory[0] = moveHistory[1];
                moveHistory[1] = moveHistory[2];
                moveHistory[2] = userChoice / 3.0; // Normalize between 0 and 1

                rockButton.setDisable(false);
                paperButton.setDisable(false);
                scissorsButton.setDisable(false);
            });
        }).start();
    }

    /**
     * Launch entry point for the JavaFX sequence.
     */
    public static void main(String[] args) {
        launch(args);
    }
}