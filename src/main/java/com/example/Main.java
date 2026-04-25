package com.example;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        Neuralnetwork network = new Neuralnetwork(2, 5, 1);
        MetaAI ai = new MetaAI(network);
        ai.setupDatabase();
        ai.learnFromDatabase();

        double lastUserChoice = 0.0;

        while (true) {
            System.out.print("Enter 1 for Rock, 2 for Paper, 3 for Scissors (0 to quit): ");
            int userChoice = scanner.nextInt();

            if (userChoice == 0) {
                break;
            }

            double[] inputData = { lastUserChoice, 1.0 };
            double[] aiOutput = network.processInformation(inputData);

            int computerChoice = (int) (aiOutput[0] * 3) + 1;

            if (computerChoice > 3) {
                computerChoice = 3;
            } else if (computerChoice < 1) {
                computerChoice = 1;
            }

            System.out.println("Computer chose: " + computerChoice);

            if (userChoice == computerChoice) {
                System.out.println("Tie");
            } else if ((userChoice == 1 && computerChoice == 3) ||
                    (userChoice == 2 && computerChoice == 1) ||
                    (userChoice == 3 && computerChoice == 2)) {
                System.out.println("You win");
            } else if ((userChoice == 1 && computerChoice == 2) ||
                    (userChoice == 2 && computerChoice == 3) ||
                    (userChoice == 3 && computerChoice == 1)) {
                System.out.println("Computer wins");
            } else {
                System.out.println("Invalid input");
            }

            int winningMove = (userChoice % 3) + 1;
            double targetOutput = 0.5;

            if (winningMove == 1) {
                targetOutput = 0.1;
            } else if (winningMove == 2) {
                targetOutput = 0.5;
            } else if (winningMove == 3) {
                targetOutput = 0.9;
            }

            ai.trainOnRound(lastUserChoice, 1.0, targetOutput);

            lastUserChoice = (double) userChoice;
        }

        scanner.close();
    }
}