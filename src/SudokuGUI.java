import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class SudokuGUI extends Application {

    static Cell[][] cells;
    static Sudoku sudoku;
    static Label status;

    static final String STYLE_SHEET = "style.css";
    static List<Square> randomNumbers;

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
        Button btnRandomize = new Button("Randomize");
        buttonFrame.getChildren().addAll(btnSolve, btnClear, btnRandomize);

        // Status label
        HBox statusBox = new HBox();
        status = new Label();
        statusBox.getChildren().add(status);

        // Attach callbacks
        btnSolve.setOnAction(e -> solve());
        btnClear.setOnAction(e -> clear());
        btnRandomize.setOnAction(e -> randomize());

        frame.getChildren().addAll(statusBox, board, buttonFrame);

        Scene scene = new Scene(frame);

        addStyleSheet(scene);
        stage.setScene(scene);
        stage.show();
    }

    private void addStyleSheet(Scene scene) {
        String css = this.getClass().getResource(STYLE_SHEET).toExternalForm();
        scene.getStylesheets().add(css);
    }

    private void randomize() {
        sudoku.clear();
        sudoku.randomize();
        fillRandomNumbers();
        status.setText("");
        updateUI();
    }

    private static void fillRandomNumbers() {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                int res = sudoku.getCell(r, c);
                if (res > 0)
                    randomNumbers.add(new Square(r, c));
            }
        }
    }

    private static void clear() {

        Function clearUI = () -> {
            sudoku.clear();
            updateUI();
            randomNumbers.clear();
            status.setText("");
        };

        if (solverTask.isRunning()) {
            // Update UI once the task finishes. Since it's running as a seperate thread
            // this is asyncrhonous and needs to be done in a callback.
            solverTask.stopSolve();
            solverTask.setOnSucceeded(e -> clearUI.call());
        } else {
            clearUI.call();
        }
    }

    private static void copyUIBoard() {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (!randomNumbers.contains(new Square(r, c))) {
                    String value = cells[r][c].getText();
                    if (!value.equals(" ")) {
                        int cell = Integer.parseInt(value);
                        sudoku.setCell(r, c, cell);
                    }
                }
            }
        }
    }

    private static void solve() {
        copyUIBoard();
        startSolving();
    }

    private static void startSolving() {
        solverTask = new SolverTask();
        solverTask.setOnSucceeded(solvable -> {
            if (((Task<Boolean>) solvable.getSource()).getValue()) {
                status.setText("Solved");
                updateUI();
            } else {
                status.setText("Failed to solve sudoku");
            }
        });
        solverTask.runAsDaemon();

        status.setText("Solving...");
    }

    private static void updateUI() {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                String value = String.valueOf(sudoku.getCell(r, c));
                cells[r][c].setText(value);
            }
        }
    }

    static class Cell extends TextField {
        int row, col;
        final static int SIZE = 70;

        public Cell(int row, int col) {
            this.row = row;
            this.col = col;
            this.setMinSize(SIZE, SIZE);
            this.setMaxSize(SIZE, SIZE);
            this.setPrefSize(SIZE, SIZE);
            this.setText(" ");      // 1 whitespace means empty

            // Ensure that you can only enter 1-9 in the cells, with a somewhat hacky solution.
            this.textProperty().addListener(
                    (obs, oldValue, newValue) -> {
                        // Grab the last character of the textfield
                        newValue = newValue.strip();
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

        @Override
        protected Boolean call() {
            boolean result = sudoku.solve();
            if (isCancelled())
                result = false;
            return result;
        }

        void stopSolve() {
            sudoku.stopSolve();
        }

        void runAsDaemon() {
            Thread thread = new Thread(this);
            thread.setDaemon(true);                      // Don't want it to continue after main program exits.
            thread.start();
        }

    }

    interface Function {
        void call();
    }

}
