import java.io.*;
import java.util.*;

public class Sudoku implements SudokuSolver {

    /**
     *  1. Skriva test
     *  2. Skriva fina kommentarer enligt javadoc-standard
     *  3. GUI
     *
     *
     */

    private int[][] board;
    private final static Random RAND = new Random(1337);
    private static final File TABLE_PATH = new File("sudoku-solver/solutions/table");

    private static Map<String, String> table;

    private volatile boolean running;

    public Sudoku() {
        this.board = new int[9][9];
        readTable();
    }

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

    /** Returns a visual representation of the board */
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

    @Override
    public boolean solve() {
        running = true;
        String input = stringify();
        String solution = findSolution();

        boolean result = false;

        if (solution == null) {
            result = solve(0, 0);
            saveResult(input, result);
        } else if (hasSolution(solution)) {
            System.out.println("Found saved result");
            decode(solution.getBytes());
            result = true;
        }

        return result;
    }

    /** Stops the current attempt to solve the board.
     *  This should be called from a different thread. */
    public void stopSolve() {
        running = false;
    }

    @Override
    public void randomize() {
        final int CELLS_TO_FILL = 16;
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

    /** Ensures that the given row, col are within 0-8. */
    private boolean outOfBounds(int row, int col) {
        return row < 0 || row > 8 || col < 0 || col > 8;
    }

    /** Clears the entire sudoku board using indices 0-8 instead of 1-9 */
    @Override
    public void clear() {
        for(int r = 0; r < 9; r++) {
            for(int c = 0; c < 9; c++) {
                board[r][c] = 0;
            }
        }
    }

    @Override
    public void setCell(int row, int col, int val) throws IllegalArgumentException {
        if (outOfBounds(row, col) || !isOk(row, col, val) || val < 1 || val > 9)
            throw new IllegalArgumentException(String.format("Failed to put %s at (%s, %s)!\n", val, row, col));
        board[row][col] = val;
    }

    @Override
    public int getCell(int row, int col) throws IllegalArgumentException {
        if (outOfBounds(row, col))
            throw new IllegalArgumentException(String.format("Failed to read at (%s, %s)\n", row, col));
        return board[row][col];
    }


    /**
     *  ---- COMPLETELY OPTIONAL -----
     *
     *  Methods that allow us to use a different backend engine, which takes
     *  the sudoku-board as a input string, solves the sudoku, and then outputs
     *  the sudoku-board as a sting again.
     *  External engines can be found in /engines
     */

    private Map<String, String> readTable() {
        table = new HashMap<>();
        try {
            Scanner scan = new Scanner(TABLE_PATH);
            while (scan.hasNext()) {
                table.put(scan.next(), scan.next());
            }
            scan.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return table;
    }

    private void saveResult(String input, boolean boardHasSolution) {
        try {
            FileWriter writer = new FileWriter(TABLE_PATH, true);
            String row = String.format("%s %s\n", input, stringify());
            writer.write(row);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String findSolution() {
        return table.getOrDefault(stringify(), null);
    }

    private boolean hasSolution(String solution) {
        return !solution.equals("X");
    }

    public static void main(String[] args) {
        Sudoku sudoku = new Sudoku();
        sudoku.randomize();
        sudoku.solve();


    }


    /** Compares the two different backend-engines in solving-speed. */
    private void engineTest() {
        clear();
        randomize();
        System.out.println("Solving following sudoku:");
        System.out.println(this);

        long t0, t1, t2;

        t0 = System.nanoTime();
        solveWithCEngine();
        t1 = System.nanoTime() - t0;

        clear();
        randomize();

        t0 = System.nanoTime();
        solve();
        t2 = System.nanoTime() - t0;

        System.out.println(this);
        System.out.printf("C backend: %.3f ms\n", t1 / 10e6);
        System.out.printf("Java backend: %.3f ms\n", t2 / 10e6);
    }

    /** Tries to solve the sudoku with a different (pre-compiled) backend engine. */
    private boolean solveWithCEngine() {
        final String ENGINE_PATH = "sudoku-solver/engines/c_solver";

        try {
            Process process = Runtime.getRuntime().exec(String.format("./%s %s", ENGINE_PATH, stringify()));
            if (process.exitValue() == 0) {
                // Found solution
                decode(process.getInputStream().readAllBytes());
                return true;
            } else {
                // Found no solution
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    /** Fills the sudoku board with the given board as an array of bytes.
     *  This is useful if we're reading board input from another solver-engine. */
    private void decode(byte[] input) {
        int r = 0, c = 0;
        for (int i = 0; i < input.length; i++) {
            board[r][c] = input[i] - '0';
            if (c == 8) {
                r++;
                c = 0;
            } else {
                c++;
            }
        }
    }

    /** Turns the sudoku-board to a string.
     * This is useful to pass the board as argument to another solver-engine. */
    private String stringify() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                sb.append(board[r][c]);
            }
        }
        return sb.toString();
    }


}
