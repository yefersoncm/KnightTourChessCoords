package org.example;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class KnightTourChessCoords {

    private static final int BOARD_SIZE = 8;
    private static int[][] chessBoard = new int[BOARD_SIZE][BOARD_SIZE];
    private static int[] xMoves = {2, 1, -1, -2, -2, -1, 1, 2}; // Possible X-direction moves for the knight
    private static int[] yMoves = {1, 2, 2, 1, -1, -2, -2, -1}; // Possible Y-direction moves for the knight

    // Array to store the sequence of visited chess coordinates for the final solution
    private static String[] tourPath = new String[BOARD_SIZE * BOARD_SIZE];

    // Path for the output log file
    private static final String LOG_FILE_PATH = "knight_tour_detailed_log.txt";
    private static PrintWriter logWriter; // PrintWriter to write to the file

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
     * The starting position is now randomly selected, and output is redirected to a file.
     */
    public static void solveKnightTour() {
        // Initialize the board with -1 (indicating unvisited squares)
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                chessBoard[i][j] = -1;
            }
        }

        // --- Setup file writer ---
        try {
            logWriter = new PrintWriter(new FileWriter(LOG_FILE_PATH));
        } catch (IOException e) {
            System.err.println("Error creating log file: " + e.getMessage());
            return; // Exit if file cannot be created
        }

        // --- Randomly select a starting position ---
        Random random = new Random();
        int startRow = random.nextInt(BOARD_SIZE); // Random row from 0 to BOARD_SIZE-1
        int startCol = random.nextInt(BOARD_SIZE); // Random column from 0 to BOARD_SIZE-1

        logWriter.println("Attempting to solve the Knight's Tour starting from " + toChessCoordinate(startRow, startCol) + " using Warnsdorff's Rule...");
        logWriter.println("\n--- Detailed Movement Log ---"); // New header for the detailed log

        // The knight starts at the randomly chosen position and it's the first step (0)
        chessBoard[startRow][startCol] = 0;
        tourPath[0] = toChessCoordinate(startRow, startCol); // Save the starting chess coordinate

        logWriter.println("STEP 0: Starting at " + toChessCoordinate(startRow, startCol)); // Log start

        long startTime = System.currentTimeMillis(); // Start timer
        boolean solved = solveKnightTourUtil(startRow, startCol, 1);
        long endTime = System.currentTimeMillis();   // End timer

        logWriter.println("\n--- Search Complete ---"); // End of detailed log

        if (solved) {
            logWriter.println("\n--- Knight's Tour Found! ---");
            logWriter.println("Time taken: " + (endTime - startTime) + " ms");
            logWriter.println("Movement sequence in chess notation:");
            printTourPathToFile(); // Call new method to print path to file
            logWriter.println("\nBoard with step order:");
            printBoardToFile(); // Call new method to print board to file
        } else {
            logWriter.println("\nNo Knight's Tour found that visits all squares from the starting position (" + toChessCoordinate(startRow, startCol) + ").");
            logWriter.println("Time taken: " + (endTime - startTime) + " ms");
        }

        // --- Ensure the log file is closed ---
        logWriter.close();
        System.out.println("Knight's Tour solution and detailed log saved to: " + LOG_FILE_PATH);
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

        // Generate all 8 potential next moves and evaluate them
        for (int i = 0; i < 8; i++) {
            int nextRow = currentRow + xMoves[i];
            int nextCol = currentCol + yMoves[i];
            String attemptedCoord = toChessCoordinate(nextRow, nextCol);

            // Check if the potential move is within bounds
            if (nextRow >= 0 && nextRow < BOARD_SIZE && nextCol >= 0 && nextCol < BOARD_SIZE) {
                // Check if the square is unvisited
                if (chessBoard[nextRow][nextCol] == -1) {
                    // This is a VALID potential move for exploration
                    // Temporarily mark as visited for accurate degree calculation
                    chessBoard[nextRow][nextCol] = moveCount;
                    int degree = getDegree(nextRow, nextCol);
                    chessBoard[nextRow][nextCol] = -1; // Unmark immediately after degree calculation

                    possibleMoves.add(new Move(nextRow, nextCol, degree));
                    logWriter.println("STEP " + moveCount + ": From " + toChessCoordinate(currentRow, currentCol) +
                            " -> Valid potential move to " + attemptedCoord + " (Degree: " + degree + ")");
                } else {
                    // This is an INVALID move because the square is already visited
                    logWriter.println("STEP " + moveCount + ": From " + toChessCoordinate(currentRow, currentCol) +
                            " -> INVALID move to " + attemptedCoord + " (Already visited). Skipping.");
                }
            } else {
                // This is an INVALID move because it's out of bounds
                logWriter.println("STEP " + moveCount + ": From " + toChessCoordinate(currentRow, currentCol) +
                        " -> INVALID move to " + attemptedCoord + " (Out of bounds). Skipping.");
            }
        }

        // Sort valid potential moves based on Warnsdorff's Rule (ascending degree)
        Collections.sort(possibleMoves);

        // Iterate through the sorted possible moves and explore them
        for (Move move : possibleMoves) {
            int nextRow = move.row;
            int nextCol = move.col;
            String nextCoord = toChessCoordinate(nextRow, nextCol);

            // Re-check if it's still unvisited (defensive, as it should be if added correctly)
            if (chessBoard[nextRow][nextCol] == -1) {
                chessBoard[nextRow][nextCol] = moveCount; // Mark the square with the step number
                tourPath[moveCount] = nextCoord; // Save the chess coordinate

                logWriter.println("STEP " + moveCount + ": Exploring path from " + toChessCoordinate(currentRow, currentCol) +
                        " to " + nextCoord + " (Selected as best next move).");

                // Recursive call for the next move
                if (solveKnightTourUtil(nextRow, nextCol, moveCount + 1)) {
                    return true; // If the recursive call found a tour, propagate it
                } else {
                    // Backtracking: current path does not lead to a full tour
                    chessBoard[nextRow][nextCol] = -1; // Unmark the square
                    tourPath[moveCount] = null; // Remove the coordinate from the path

                    logWriter.println("BACKTRACKING: Move " + moveCount + ": Path from " + toChessCoordinate(currentRow, currentCol) +
                            " via " + nextCoord + " led to a dead end. Undoing " + nextCoord + ".");
                }
            } else {
                // This should ideally not happen if logic for populating possibleMoves is correct
                logWriter.println("WARNING: Tried to re-visit " + nextCoord + " at move " + moveCount + " after sorting. Skipping.");
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
     * Prints the complete tour path in chess notation to the log file.
     */
    private static void printTourPathToFile() {
        for (int i = 0; i < tourPath.length; i++) {
            logWriter.print(tourPath[i]);
            if (i < tourPath.length - 1) {
                logWriter.print(" -> ");
            }
            if ((i + 1) % 10 == 0) { // Break line every 10 moves for better readability
                logWriter.println();
            }
        }
        logWriter.println();
    }

    /**
     * Prints the chessBoard showing the order of the knight's moves to the log file.
     */
    private static void printBoardToFile() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                logWriter.printf("%3d", chessBoard[i][j]); // Format to align numbers
            }
            logWriter.println();
        }
    }
}