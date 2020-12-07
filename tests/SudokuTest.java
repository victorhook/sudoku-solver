import static org.junit.jupiter.api.Assertions.*;

class SudokuTest {
    private SudokuSolver sudoku = new Sudoku();
    @org.junit.jupiter.api.BeforeEach
    void setUp() {

    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        sudoku.clear();
    }

    @org.junit.jupiter.api.Test
    void solveUnsolvable() {
        sudoku.setCell(1,1, 4);
        sudoku.setCell(8, 1, 7);
        sudoku.setCell(5, 1, 4);
        assertFalse(sudoku.solve(), "The sudoku should not be solvable!");
    }

    @org.junit.jupiter.api.Test
    void solveEmpty() {
        assertTrue(sudoku.solve(), "Empty sudoku board cannot be solved.");
    }

    @org.junit.jupiter.api.Test
    void setCell() {
        sudoku.setCell(1,1, 6);
        assertEquals(6, sudoku.getCell(1, 1), "Fel värde i rutan.");
    }

    @org.junit.jupiter.api.Test
    void setThrowableCell(){
        assertThrows(IllegalArgumentException.class, () ->
                sudoku.setCell(10, 3, 7)
        );
    }

    @org.junit.jupiter.api.Test
    void getCell() {
        sudoku.setCell(3, 3 , 7);
        assertEquals(7, sudoku.getCell(3,3), "setCell() does not work.");
    }

    @org.junit.jupiter.api.Test
    void testFig1() {
        sudoku.setCell(0, 2, 8);
        sudoku.setCell(0, 5, 9);
        sudoku.setCell(0, 7, 6);
        sudoku.setCell(0, 8, 2);
        sudoku.setCell(1, 8, 5);
        sudoku.setCell(2, 0, 1);
        sudoku.setCell(2, 2, 2);
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

            assertTrue(sudoku.solve(), "Sudoku cannot be solved.");

    }
}
