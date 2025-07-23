package org.example;

public class KnightTourChessCoords {

    private static final int BOARD_SIZE = 8;
    private static int[][] chessBoard = new int[BOARD_SIZE][BOARD_SIZE];
    private static int[] xMoves = {2, 1, -1, -2, -2, -1, 1, 2}; // Possible X-direction moves for the knight
    private static int[] yMoves = {1, 2, 2, 1, -1, -2, -2, -1}; // Possible Y-direction moves for the knight

    // Array to store the sequence of visited chess coordinates
    private static String[] tourPath = new String[BOARD_SIZE * BOARD_SIZE];

    public static void main(String[] args) {
        solveKnightTour();
    }

    /**
     * Initiates the Knight's Tour problem resolution.
     */
    public static void solveKnightTour() {
        // Initialize the board with -1 (indicating unvisited squares)
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                chessBoard[i][j] = -1;
            }
        }

        // The knight starts at position (0,0) and it's the first step (0)
        chessBoard[0][0] = 0;
        tourPath[0] = toChessCoordinate(0, 0); // Save the starting chess coordinate

        System.out.println("Attempting to solve the Knight's Tour...");
        System.out.println("--- Backtracking Log ---");

        // Try to solve the tour starting from the initial position
        if (solveKnightTourUtil(0, 0, 1)) {
            System.out.println("\n--- Tour Found! ---");
            System.out.println("Movement sequence in chess notation:");
            printTourPath();
            System.out.println("\nBoard with step order:");
            printBoard();
        } else {
            System.out.println("\nNo Knight's Tour found that visits all squares from the starting position (A1).");
        }
    }

    /**
     * Recursive function that attempts to find a Knight's Tour.
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

        // Probar los 8 posibles movimientos del caballo
        for (int i = 0; i < 8; i++) {
            int nextRow = currentRow + xMoves[i];
            int nextCol = currentCol + yMoves[i];

            // Verificar si el movimiento es válido
            if (isValidMove(nextRow, nextCol)) {
                chessBoard[nextRow][nextCol] = moveCount; // Mark the square with the step number
                tourPath[moveCount] = toChessCoordinate(nextRow, nextCol); // Save the chess coordinate

                // Recursive call for the next move
                if (solveKnightTourUtil(nextRow, nextCol, moveCount + 1)) {
                    return true; // If the recursive call found a tour, propagate it
                } else {
                    // If the current move does not lead to a tour, "undo" the move
                    // (backtracking) and try another option
                    chessBoard[nextRow][nextCol] = -1; // Unmark the square
                    tourPath[moveCount] = null; // Remove the coordinate from the path

                    // Log the backtracking step
                    System.out.println("BACKTRACKING: Move " + moveCount + ": From " + toChessCoordinate(currentRow, currentCol) +
                            " attempted " + toChessCoordinate(nextRow, nextCol) + ". No solution from here. Undoing.");
                }
            }
            // No 'else' needed here for invalid moves (out of bounds/already visited)
            // as they are simply skipped by the isValidMove check and don't involve backtracking
            // of a "marked" square.
        }
        return false; // No tour found from this position
    }

    /**
     * Checks if a square is a valid move for the knight.
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