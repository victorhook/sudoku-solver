import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class SudokuGUI extends Application {

    static Cell[][] cells;
    static Sudoku sudoku;
    static Label status;

    static final String STYLE_SHEET = "style.css";
    static List<Square> randomNumbers;
    static List<Square> invalidNumbers;

    static final Font FONT_BOLD = Font.font ("Courier", FontWeight.BOLD, 25),
                      FONT_NORMAL = Font.font ("Courier", 25);

    static final Background BACKGROUND_RED = new Background(new BackgroundFill(Color.RED,
                                                CornerRadii.EMPTY, Insets.EMPTY)),
                            BACKGROUND_NORMAL = new Background(new BackgroundFill(Color.TRANSPARENT,
                                    CornerRadii.EMPTY, Insets.EMPTY));

    static SolverTask solverTask;

    @Override
    public void start(Stage stage) {
        sudoku = new Sudoku();
        randomNumbers = new ArrayList<>();

        VBox frame = new VBox();
        GridPane board = new GridPane();

        cells = new Cell[9][9];
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                Cell cell = new Cell(r, c);
                cells[r][c] = cell;
                board.add(cell, c, r);

                // These IDS are used in the css to create the 3x3 grid
                if (r % 3 == 0 && r != 0) {
                    cell.setId("row");
                    if (c % 3 == 0 && c != 0)
                        cell.setId("rowAndCol");
                } else if (c % 3 == 0 && c != 0)
                    cell.setId("col");
            }
        }

        HBox buttonFrame = new HBox(10);
        Button btnSolve = new Button("Solve");
        Button btnClear = new Button("Clear");
        Button btnRandomize = new Button("New");
        Button btnFill = new Button("Demo");
        buttonFrame.getChildren().addAll(btnSolve, btnClear, btnRandomize, btnFill);

        // Status label
        HBox statusBox = new HBox();
        status = new Label();
        statusBox.getChildren().add(status);

        // Attach callbacks
        btnSolve.setOnAction(e -> solve());
        btnClear.setOnAction(e -> clear());
        btnRandomize.setOnAction(e -> randomize());
        btnFill.setOnAction(e -> fillDemo());

        frame.getChildren().addAll(statusBox, board, buttonFrame);

        resetFonts();

        Scene scene = new Scene(frame);

        addStyleSheet(scene);
        stage.setScene(scene);
        stage.show();
    }

    private void fillDemo() {
        sudoku.clear();
        sudoku.setCell(0, 2, 8);
        sudoku.setCell(0, 5, 9);
        sudoku.setCell(0, 7, 6);
        sudoku.setCell(0, 8, 2);
        sudoku.setCell(1, 8, 5);
        sudoku.setCell(2, 0, 1);
        sudoku.setCell(2, 2, 2);
        sudoku.setCell(2, 3, 5);
        sudoku.setCell(3, 3, 2);
        sudoku.setCell(3, 4, 1);
        sudoku.setCell(3, 7, 9);
        sudoku.setCell(4, 1, 5);
        sudoku.setCell(4, 6, 6);
        sudoku.setCell(5, 0, 6);
        sudoku.setCell(5, 7, 2);
        sudoku.setCell(5, 8, 8);
        sudoku.setCell(6, 0, 4);
        sudoku.setCell(6, 1, 1);
        sudoku.setCell(6, 3, 6);
        sudoku.setCell(6, 5, 8);
        sudoku.setCell(7, 0, 8);
        sudoku.setCell(7, 1, 6);
        sudoku.setCell(7, 4, 3);
        sudoku.setCell(7, 6, 1);
        sudoku.setCell(8, 6, 4);
        fillRandomNumbers();
        updateUI();
    }

    private void addStyleSheet(Scene scene) {
        String css = this.getClass().getResource(STYLE_SHEET).toExternalForm();
        scene.getStylesheets().add(css);
    }

    /** Callback from randomize-button. */
    private void randomize() {
        sudoku.clear();
        sudoku.randomize();
        fillRandomNumbers();
        status.setText("");
        updateUI();
    }

    /** Callback from clear-button. */
    private static void clear() {
        Function clearUI = () -> {
            sudoku.clear();
            updateUI();
            randomNumbers.clear();
            resetFonts();
            status.setText("");
        };

        // Must ensure task has been created.
        if (solverTask != null && solverTask.isRunning()) {
            // Update UI once the task finishes. Since it's running as a seperate thread
            // this is asynchronous and needs to be done in a callback.
            solverTask.stopSolve();
            solverTask.setOnSucceeded(e -> clearUI.call());
        } else {
            clearUI.call();
        }
    }

    private static void resetFonts() {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                cells[r][c].setFont(FONT_NORMAL);
                cells[r][c].setBackground(BACKGROUND_NORMAL);
            }
        }
    }

    /** Callback form the solve-button. */
    private static void solve() {
        List<Square> invalidNumbers = copyUIBoard();
        if (invalidNumbers.size() == 0) {
            startSolving();
        }
        else {
            status.setText("Invalid board");
            colorRed(invalidNumbers);
            sudoku.clear();
        }

    }

    private static void colorRed(List<Square> list) {
        for(int i = 0; i < list.size(); i++){
            cells[list.get(i).row][list.get(i).col].setBackground(BACKGROUND_RED);
        }
    }

    /** Fills a list with squares that the sudoku-board has just filled with random numbers.
     *  This is needed because these squares will be ignored when later copying to UI-board to the sudoku.
     */
    private static void fillRandomNumbers() {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                int res = sudoku.getCell(r, c);
                if (res > 0) {
                    randomNumbers.add(new Square(r, c));
                    cells[r][c].setFont(FONT_BOLD);
                }
            }
        }
    }

    /** Copies the UI-cells to the sudoku-board.
     * randomNumbers contain numbers that have already been generated and filled by the sudoku,
     * so these are ignored. */
    private static List<Square> copyUIBoard() {
        invalidNumbers = new ArrayList<>();
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (!randomNumbers.contains(new Square(r, c))) {
                    String value = cells[r][c].getText();
                    if (!value.equals(" ")) {
                        int cell = Integer.parseInt(value);
                        try {
                            sudoku.setCell(r, c, cell);
                        } catch (IllegalArgumentException e) {
                            // If this exception occurs, the board is invalid according to the sudoku-rules.
                            // Exit quick and inform user that it's not solvable.
                            invalidNumbers.add(new Square(r, c));
                        }
                    }
                }
            }
        }
        return invalidNumbers;
    }

    /** Starts the solver-thread and updates the status-label accordingly. */
    private static void startSolving() {
        solverTask = new SolverTask();
        solverTask.setOnSucceeded(solvable -> {
            if (((Task<Boolean>) solvable.getSource()).getValue()) {
                status.setText("Solved");
                updateUI();
            } else {
                status.setText("Failed to solve sudoku");
            }
            sudoku.clear();
        });
        solverTask.runAsDaemon();

        status.setText("Solving...");
    }

    /** Updates all the cells according to the sudoku-board. */
    private static void updateUI() {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                String value = String.valueOf(sudoku.getCell(r, c));
                cells[r][c].setText(value);
            }
        }
    }



    static class Cell extends TextField {
        /** This class represents a cell on the sudoku-board. */

        int row, col;
        final static int SIZE = 60;

        public Cell(int row, int col) {
            this.row = row;
            this.col = col;

            // Brute-force the size of the cell..
            this.setMinSize(SIZE, SIZE);
            this.setMaxSize(SIZE, SIZE);
            this.setPrefSize(SIZE, SIZE);
            this.setText(" ");      // 1 whitespace means empty

            // Ensure that you can only enter 1-9 in the cells, with a somewhat hacky solution.
            this.textProperty().addListener(
                    (obs, oldValue, newValue) -> {
                        newValue = newValue.strip();
                        // Grab the last character of the textfield (if more than 1)
                        newValue = newValue.length() > 0 ? newValue.substring(newValue.length()-1) : newValue;
                        if (newValue.matches("[1-9]")) {
                            // If new value is between 1-9, it's ok
                            this.setText(newValue);
                        } else {
                            // If not, we'll just put the text to empty.
                            this.setText(" ");
                        }
                    }
            );
        }


    }

    static class Square {
        /**
         * Helper class to help differentiate between cells that have been filled in by the
         * "randomize" method and cells that have been filled in by hand.
         */

        int row, col;
        Square(int row, int col) {
            this.row = row;
            this.col = col;
        }

        public boolean equals(Object obj) {
            if (obj instanceof Square) {
                Square sq = (Square) obj;
                return sq.col == col && sq.row == row;
            }
            return false;
        }
    }

    static class SolverTask extends Task {
        /**
         * This class helps us run the solve()-method at the sudoku-board in a different thread.
         * The Task class is JavaFX's way of doing it.
         */

        @Override
        protected Boolean call() {
            boolean result = sudoku.solve();
            if (isCancelled())
                result = false;
            return result;
        }

        /**
         * This method stops the backend-engine by calling stopSolve() on the engine.
         * However, since this is not in the SudokuSolver-interface, the method might
         * not exist, depending on the solver-engine implementation.
         */
        void stopSolve() {
            try {
                Method stopSolve = Sudoku.class.getDeclaredMethod("stopSolve");
                stopSolve.invoke(sudoku);
            }
            // Not much to do... Just wait for backend to finish.
            catch (NoSuchMethodException e) {}
            catch (IllegalAccessException e) {}
            catch (InvocationTargetException e) {}
        }

        /** Start the thread as daemon, to ensure that it doesn't continue running after main thread is closed */
        void runAsDaemon() {
            Thread thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
        }

    }

    interface Function {
    /** Helper interface */
        void call();
    }

}
