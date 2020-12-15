import java.util.Random;

/** Represents a sudoku-board. */
public class Sudoku implements SudokuSolver {

    private int[][] board;
    private final static Random RAND = new Random();
    private volatile boolean running;

    public Sudoku() {
        this.board = new int[9][9];
    }

    /**
     * Tries to solve the current sudoku board.
     * @return true if a solution is found and false if the board can't be solved.
     */
    @Override
    public boolean solve() {
        running = true;
        return solve(0, 0);
    }

    /** Clears the entire sudoku board. */
    @Override
    public void clear() {
        for(int r = 0; r < 9; r++) {
            for(int c = 0; c < 9; c++) {
                board[r][c] = 0;
            }
        }
    }

    /** Fills the sudoku-board with random (valid) numbers. */
    @Override
    public void randomize() {
        final int CELLS_TO_FILL = 5 + RAND.nextInt(8);
        int filled = 0;
        int r, c;
        while (filled < CELLS_TO_FILL) {
            int value = 1 + RAND.nextInt(9);
            r = RAND.nextInt(9);
            c = RAND.nextInt(9);

            if (isOk(r, c, value)) {
                setCell(r, c, value);
                filled++;
            }
        }
    }

    /** Sets a given cell at row and column with the given value. */
    @Override
    public void setCell(int row, int col, int val) throws IllegalArgumentException {
        if (outOfBounds(row, col) || !isOk(row, col, val) || val < 1 || val > 9)
            throw new IllegalArgumentException(String.format("Failed to put %s at (%s, %s)!\n", val, row, col));
        board[row][col] = val;
    }

    /** Returns the value of the given cell at given row and column. */
    @Override
    public int getCell(int row, int col) throws IllegalArgumentException {
        if (outOfBounds(row, col))
            throw new IllegalArgumentException(String.format("Failed to read at (%s, %s)\n", row, col));
        return board[row][col];
    }

    /** Stops the current attempt to solve the board.
     *  This should be called from a different thread. */
    public void stopSolve() {
        running = false;
    }

    /** Returns a visual representation of the board */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < 9; r++) {
            sb.append("\n");
            for (int c = 0; c < 9; c++) {
                sb.append(String.format(" %s ", board[r][c]));
            }
        }
        return sb.toString();
    }


    /** --- Private helper-methods --- */

    /** Checks if the given cell and value is valid. Naively expects that all numbers
     *  are withing range (0-8 for row, col) and 1-9 for value.
     *  The reason for this is to save computation. */
    private boolean isOk(int row, int col, int value) {
        for (int i = 0; i < 8; i++) {
            if (board[row][i] == value || board[i][col] == value)
                return false;
        }

        int rStart = (row / 3) * 3;
        int cStart = (col / 3) * 3;

        for (int r = rStart; r < rStart+3; r++) {
            for (int c = cStart; c < cStart+3; c++) {
                if (board[r][c] == value)
                    return false;
            }
        }

        return true;
    }

    /** Recursive method that tries to solve the sudoku.
     *  To be able to stop this solve-method from a different thread,
     *  we've added a flag that must be true before proceeding the method. */
    private boolean solve(int row, int col) {
        if (!running)
            return false;

        if (col == 9) {
            if (row == 8)
                return true;
            row++;
            col = 0;
        }

        if (board[row][col] == 0) {
            for (int value = 1; value <= 9; value++) {
                if (isOk(row, col, value)) {
                    board[row][col] = value;        // Sets cell
                    if (solve(row, col+1))
                        return true;
                    board[row][col] = 0;
                }
            }
        } else {
            return solve(row, col+1);
        }
        return false;
    }

    /** Ensures that the given row, col are within 0-8. */
    private boolean outOfBounds(int row, int col) {
        return row < 0 || row > 8 || col < 0 || col > 8;
    }

}
