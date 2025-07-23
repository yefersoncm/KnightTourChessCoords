package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class KnightTourChessCoords {

    private static final int BOARD_SIZE = 8;
    private static int[][] chessBoard = new int[BOARD_SIZE][BOARD_SIZE];
    private static int[] xMoves = {2, 1, -1, -2, -2, -1, 1, 2}; // Possible X-direction moves for the knight
    private static int[] yMoves = {1, 2, 2, 1, -1, -2, -2, -1}; // Possible Y-direction moves for the knight

    // Array to store the sequence of visited chess coordinates
    private static String[] tourPath = new String[BOARD_SIZE * BOARD_SIZE];

    // Inner class to represent a potential move with its Warnsdorff's degree
    private static class Move implements Comparable<Move> {
        int row;
        int col;
        int degree; // Number of available moves from this potential square

        public Move(int row, int col, int degree) {
            this.row = row;
            this.col = col;
            this.degree = degree;
        }

        // Compare Moves based on their degree (for sorting)
        @Override
        public int compareTo(Move other) {
            return Integer.compare(this.degree, other.degree);
        }
    }

    public static void main(String[] args) {
        solveKnightTour();
    }

    /**
     * Initiates the Knight's Tour problem resolution.
     * The starting position is now randomly selected.
     */
    public static void solveKnightTour() {
        // Initialize the board with -1 (indicating unvisited squares)
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                chessBoard[i][j] = -1;
            }
        }

        // --- Randomly select a starting position ---
        Random random = new Random();
        int startRow = random.nextInt(BOARD_SIZE); // Random row from 0 to BOARD_SIZE-1
        int startCol = random.nextInt(BOARD_SIZE); // Random column from 0 to BOARD_SIZE-1

        System.out.println("Attempting to solve the Knight's Tour starting from " + toChessCoordinate(startRow, startCol) + " using Warnsdorff's Rule...");
        // Disabling detailed backtracking log for Warnsdorff's as it's typically very fast
        // System.out.println("--- Backtracking Log ---");

        // The knight starts at the randomly chosen position and it's the first step (0)
        chessBoard[startRow][startCol] = 0;
        tourPath[0] = toChessCoordinate(startRow, startCol); // Save the starting chess coordinate

        long startTime = System.currentTimeMillis(); // Start timer
        boolean solved = solveKnightTourUtil(startRow, startCol, 1);
        long endTime = System.currentTimeMillis();   // End timer

        if (solved) {
            System.out.println("\n--- Knight's Tour Found! ---");
            System.out.println("Time taken: " + (endTime - startTime) + " ms");
            System.out.println("Movement sequence in chess notation:");
            printTourPath();
            System.out.println("\nBoard with step order:");
            printBoard();
        } else {
            System.out.println("\nNo Knight's Tour found that visits all squares from the starting position (" + toChessCoordinate(startRow, startCol) + ").");
            System.out.println("Time taken: " + (endTime - startTime) + " ms");
        }
    }

    /**
     * Recursive function that attempts to find a Knight's Tour using Warnsdorff's Rule.
     *
     * @param currentRow The knight's current row.
     * @param currentCol The knight's current column.
     * @param moveCount   The number of moves made so far.
     * @return true if a tour is found, false otherwise.
     */
    private static boolean solveKnightTourUtil(int currentRow, int currentCol, int moveCount) {
        // If all squares have been visited, a tour is found
        if (moveCount == BOARD_SIZE * BOARD_SIZE) {
            return true;
        }

        List<Move> possibleMoves = new ArrayList<>();

        // Generate all valid next moves and calculate their degrees
        for (int i = 0; i < 8; i++) {
            int nextRow = currentRow + xMoves[i];
            int nextCol = currentCol + yMoves[i];

            if (isValidMove(nextRow, nextCol)) {
                // IMPORTANT: Calculate the degree for the 'next' square, considering it as visited
                // temporarily to avoid counting moves back to the current square, or counting to squares
                // that would become invalid if this move was made.
                chessBoard[nextRow][nextCol] = moveCount; // Temporarily mark as visited for degree calculation
                int degree = getDegree(nextRow, nextCol);
                chessBoard[nextRow][nextCol] = -1; // Unmark immediately after degree calculation

                possibleMoves.add(new Move(nextRow, nextCol, degree));
            }
        }

        // Sort moves based on Warnsdorff's Rule (ascending degree)
        Collections.sort(possibleMoves);

        // Iterate through the sorted possible moves
        for (Move move : possibleMoves) {
            int nextRow = move.row;
            int nextCol = move.col;

            // This check is technically redundant if isValidMove was used when populating possibleMoves,
            // but it's good defensive programming in case external factors change board state in complex scenarios.
            // For this specific code, it primarily re-checks against already visited status if degree calculation
            // was done differently.
            if (chessBoard[nextRow][nextCol] == -1) { // Ensure it's still unvisited after sorting
                chessBoard[nextRow][nextCol] = moveCount; // Mark the square with the step number
                tourPath[moveCount] = toChessCoordinate(nextRow, nextCol); // Save the chess coordinate

                // Recursive call for the next move
                if (solveKnightTourUtil(nextRow, nextCol, moveCount + 1)) {
                    return true; // If the recursive call found a tour, propagate it
                } else {
                    // Backtracking: current path does not lead to a full tour
                    chessBoard[nextRow][nextCol] = -1; // Unmark the square
                    tourPath[moveCount] = null; // Remove the coordinate from the path

                    // Generally, with Warnsdorff's, you see much less backtracking,
                    // so logging every step might not be as useful. Uncomment if needed for debugging.
                    // System.out.println("BACKTRACKING: Move " + moveCount + ": From " + toChessCoordinate(currentRow, currentCol) +
                    //                    " attempted " + toChessCoordinate(nextRow, nextCol) + ". No solution from here. Undoing.");
                }
            }
        }
        return false; // No tour found from this position
    }

    /**
     * Calculates the "degree" of a square: the number of valid, unvisited moves
     * available from that square. Used by Warnsdorff's Rule.
     *
     * @param row The row of the square to check.
     * @param col The column of the square to check.
     * @return The number of valid onward moves from (row, col).
     */
    private static int getDegree(int row, int col) {
        int degree = 0;
        for (int i = 0; i < 8; i++) {
            int nextNextRow = row + xMoves[i];
            int nextNextCol = col + yMoves[i];

            // Check if the potential 'next-next' move is within bounds and unvisited
            if (nextNextRow >= 0 && nextNextRow < BOARD_SIZE &&
                    nextNextCol >= 0 && nextNextCol < BOARD_SIZE &&
                    chessBoard[nextNextRow][nextNextCol] == -1) {
                degree++;
            }
        }
        return degree;
    }

    /**
     * Checks if a square is a valid move for the knight.
     * A move is valid if it's within board bounds and the square is unvisited.
     *
     * @param row The row of the square.
     * @param col The column of the square.
     * @return true if it's a valid move, false otherwise.
     */
    private static boolean isValidMove(int row, int col) {
        return (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE && chessBoard[row][col] == -1);
    }

    /**
     * Converts matrix coordinates (row, col) to standard chess notation (e.g., A1, H8).
     * In chess, columns range from 'A' to 'H' and rows from '1' to '8'.
     * We assume matrix row 0 is chess row 8, and matrix column 0 is chess column 'A'.
     *
     * @param row The matrix row (0 to 7).
     * @param col The matrix column (0 to 7).
     * @return The coordinate in chess notation.
     */
    private static String toChessCoordinate(int row, int col) {
        char columnChar = (char) ('A' + col);
        int rowNumber = BOARD_SIZE - row; // Chess rows go from 1 to 8, where matrix row 0 is chess row 8
        return String.valueOf(columnChar) + rowNumber;
    }

    /**
     * Prints the chessBoard showing the order of the knight's moves.
     */
    private static void printBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                System.out.printf("%3d", chessBoard[i][j]); // Format to align numbers
            }
            System.out.println();
        }
    }

    /**
     * Prints the complete tour path in chess notation.
     */
    private static void printTourPath() {
        for (int i = 0; i < tourPath.length; i++) {
            System.out.print(tourPath[i]);
            if (i < tourPath.length - 1) {
                System.out.print(" -> ");
            }
            if ((i + 1) % 10 == 0) { // Break line every 10 moves for better readability
                System.out.println();
            }
        }
        System.out.println();
    }
}