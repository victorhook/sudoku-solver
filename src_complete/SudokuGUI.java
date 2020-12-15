import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class SudokuGUI extends Application {

    private static Cell[][] cells;
    private static SudokuSolver sudoku;
    private static Text status;
    private static Hyperlink wikiLink;

    private static final String STYLE_SHEET = "style.css";
    private static List<Square> randomNumbers;
    private static List<Square> invalidNumbers;

    private static final Font FONT_BOLD = Font.font ("Courier", FontWeight.BOLD, 25),
                      FONT_NORMAL = Font.font ("Courier", 25);

    private static final Background BACKGROUND_RED = new Background(new BackgroundFill(Color.RED,
                                                            CornerRadii.EMPTY, Insets.EMPTY)),
                            BACKGROUND_NORMAL = new Background(new BackgroundFill(Color.TRANSPARENT,
                                                               CornerRadii.EMPTY, Insets.EMPTY));
    private static final String WIKI_LINK = "https://en.wikipedia.org/wiki/Sudoku";
    private static SolverTask solverTask;
    private static double timer;

    @Override
    public void start(Stage stage) {
        sudoku = new Sudoku();
        randomNumbers = new ArrayList<>();

        // Container frames for all elements
        VBox frame = new VBox();
        GridPane board = new GridPane();
        HBox buttonFrame = new HBox(20);

        // Populate the containers
        fillGrid(board);
        addButtons(buttonFrame);

        // Status label
        VBox statusBox = new VBox();
        status = new Text();
        status.setId("status");
        wikiLink = new Hyperlink();
        wikiLink.setOnAction(e -> getHostServices().showDocument(WIKI_LINK));

        statusBox.getChildren().addAll(status, wikiLink);

        // Add all the container frames to main container.
        frame.getChildren().addAll(statusBox, board, buttonFrame);

        resetFonts();

        // Load scene and start the UI.
        Scene scene = new Scene(frame);
        addStyleSheet(scene);
        stage.setScene(scene);
        stage.setTitle("Backtracking sudoku-solver");
        stage.show();
    }


    /** --- CALLBACKS --- */

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
        // Must ensure task has been created.
        if (solverTask != null && solverTask.isRunning()) {
            // Update UI once the task finishes. Since it's running as a seperate thread
            // this is asynchronous and needs to be done in a callback.
            solverTask.stopSolve();
            solverTask.setOnSucceeded(e -> clearUI());
        } else {
            clearUI();
        }
        wikiLink.setText("");
    }

    /** Callback form the solve-button. */
    private static void solve() {
        List<Square> invalidNumbers = copyUIBoard();
        if (invalidNumbers.size() == 0) {
            startSolving();
            wikiLink.setText("");
        }
        else {
            status.setText("Invalid board. Please follow the rules.");
            wikiLink.setText("Rules of sudoku");
            colorRed(invalidNumbers);
            sudoku.clear();
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


    /** --- LOGIC --- */

    /** Starts the solver-thread and updates the status-label accordingly. */
    private static void startSolving() {
        solverTask = new SolverTask();
        solverTask.setOnSucceeded(solvable -> {
            if (((Task<Boolean>) solvable.getSource()).getValue()) {
                status.setText(String.format("Solved in %.3f seconds", timer / 1000));
                updateUI();
            } else {
                status.setText("Failed to solve sudoku");
            }
            sudoku.clear();
        });
        solverTask.runAsDaemon();

        status.setText("Solving...");
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


    /** --- UI related --- */

    /** Updates all the cells according to the sudoku-board. */
    private static void updateUI() {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                String value = String.valueOf(sudoku.getCell(r, c));
                cells[r][c].setText(value);
            }
        }
    }

    /** Applies the stylesheet to the scene. */
    private void addStyleSheet(Scene scene) {
        String css = this.getClass().getResource(STYLE_SHEET).toExternalForm();
        scene.getStylesheets().add(css);
    }

    /** Helper method */
    private static void clearUI() {
        sudoku.clear();
        updateUI();
        randomNumbers.clear();
        resetFonts();
        status.setText("");
        wikiLink.setText("");
    }

    /** Resets the font to default values. */
    private static void resetFonts() {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                cells[r][c].setFont(FONT_NORMAL);
                cells[r][c].setBackground(BACKGROUND_NORMAL);
            }
        }
    }

    /** Fills the list of squares with red background. */
    private static void colorRed(List<Square> list) {
        for(int i = 0; i < list.size(); i++){
            cells[list.get(i).row][list.get(i).col].setBackground(BACKGROUND_RED);
        }
    }

    /** Helper method to add the buttons to the UI and attach callbacks */
    private void addButtons(HBox buttonFrame) {
        Button btnSolve = new Button("Solve");
        Button btnClear = new Button("Clear");
        Button btnRandomize = new Button("New");
        Button btnFill = new Button("Demo à la Figur 1");
        buttonFrame.getChildren().addAll(btnSolve, btnClear, btnRandomize, btnFill);

        // Attach callbacks
        btnSolve.setOnAction(e -> solve());
        btnClear.setOnAction(e -> clear());
        btnRandomize.setOnAction(e -> randomize());
        btnFill.setOnAction(e -> fillDemo());
    }

    /** Helper methods to build UI */
    private void fillGrid(GridPane board) {
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
    }


    /** --- INNER CLASSES --- */

    /** This class represents a cell on the sudoku-board. */
    private static class Cell extends TextField {
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

    /**
     * Helper class to help differentiate between cells that have been filled in by the
     * "randomize" method and cells that have been filled in by hand.
     */
    private static class Square {
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

    /**
     * This class helps us run the solve()-method at the sudoku-board in a different thread.
     * The Task class is JavaFX's way of doing it.
     */
    private static class SolverTask extends Task {

        @Override
        protected Boolean call() {
            timer = System.currentTimeMillis();
            boolean result = sudoku.solve();
            timer = System.currentTimeMillis() - timer;
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

}
