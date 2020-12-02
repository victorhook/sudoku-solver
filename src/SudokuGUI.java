import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SudokuGUI extends Application {

    static Cell cells[][];
    static Sudoku sudoku;

    @Override
    public void start(Stage stage) throws Exception {
        sudoku = new Sudoku();

        VBox frame = new VBox();
        GridPane board = new GridPane();

        cells = new Cell[9][9];
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                Cell cell = new Cell(r, c);
                cells[r][c] = cell;
                board.add(cell, r, c);
            }
        }

        HBox buttonFrame = new HBox();
        Button btnSolve = new Button("Solve");
        Button btnClear = new Button("Clear");
        Button btnRandomize = new Button("Randomize");
        buttonFrame.getChildren().addAll(btnSolve, btnClear, btnRandomize);

        // Attach callbacks
        btnSolve.setOnAction(e -> solve());

        frame.getChildren().addAll(board, buttonFrame);

        Scene scene = new Scene(frame);
        stage.setScene(scene);
        stage.show();
    }

    private static void solve() {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                String value = cells[r][c].getText();
                if (!value.equals("")) {
                    int cell = Integer.parseInt(value);
                    sudoku.setCell(r, c, cell);
                }
            }
        }
        sudoku.solve();
        updateUI();
    }

    private static void updateUI() {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                String value = String.valueOf(sudoku.getCell(r, c));
                cells[r][c].setText(value);
            }
        }
    }

    class Cell extends TextField {
        int row, col;
        final static int SIZE = 40;

        public Cell(int row, int col) {
            this.row = row;
            this.col = col;
            this.setMinSize(SIZE, SIZE);
            this.setMaxSize(SIZE, SIZE);
            this.setPrefSize(SIZE, SIZE);
            this.lengthProperty().addListener(
                    (obs, n1, n2) -> {
                        //System.out.printf("Obs: %s, n1: %s, nr2: %s", obs, n1, n2);
                    }
            );
        }


    }

}
