public class Sudoku implements SudokuSolver {

    private int board[][];

    public Sudoku() {
        this.board = new int[9][9];
    }

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

    private boolean solve(int row, int col) {
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
        return solve(0, 0);
    }

    public static void main(String[] args) {
        Sudoku sudoku = new Sudoku();
        sudoku.setCell(0, 1, 7);
        System.out.println(sudoku);
        sudoku.solve();
        System.out.println(sudoku);
        //System.out.println(sudoku);
    }

    @Override
    public void randomize() {

    }

    /**
     *
     */
    private boolean ensureOk(int row, int col) {
        if (row < 0 || row > 8 || col < 0 || col > 8)
            return false;
        return true;
    }

    @Override
    public void setCell(int row, int col, int val) throws IllegalArgumentException {
        if (!ensureOk(row, col) || !isOk(row, col, val) || val < 1 || val > 9)
            throw new IllegalArgumentException("Aja baja!");
        board[row][col] = val;
    }

    @Override
    public int getCell(int row, int col) throws IllegalArgumentException {
        if (!ensureOk(row, col))
            throw new IllegalArgumentException("Aja baja!");
        return board[row][col];
    }
}
