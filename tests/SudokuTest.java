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

    /**
     * Tries to solve an empty soduku.
     */
    @org.junit.jupiter.api.Test
    void solveEmpty() {
        assertTrue(sudoku.solve(), "Empty sudoku board cannot be solved.");
    }

    /**
     * Tries to set a cell that breaks the rule of Sudoku that states that two identical numbers
     * cannot be put in the same column.
     */
    @org.junit.jupiter.api.Test
    void throwableCellSameRow() {
        sudoku.setCell(1,1,3);
        assertThrows(IllegalArgumentException.class,() -> sudoku.setCell(1,2, 3));
    }

    /**
     * Tries to set a cell that breaks the rule of Sudoku that states that two identical numbers
     * cannot be put in the same row.
     */
    @org.junit.jupiter.api.Test
    void throwableCellSameCol() {
        sudoku.setCell(1,1,3);
        assertThrows(IllegalArgumentException.class,() ->
                sudoku.setCell(2,1, 3));
    }

    /**
     * Tests if it is possible to put two identical numbers in the same region and
     * asserts it is possible to do so in two different ones.
     */
    @org.junit.jupiter.api.Test
    void getThrowableCellSameRegion(){
        sudoku.setCell(1, 1, 6);
        sudoku.setCell(5, 4, 6);
        assertEquals(6, sudoku.getCell(5, 4));
        assertThrows(IllegalArgumentException.class, () ->
                sudoku.setCell(0, 2, 6));
    }
    @org.junit.jupiter.api.Test
    void setCell() {
        sudoku.setCell(1,1, 6);
        assertEquals(6, sudoku.getCell(1, 1), "Fel värde i rutan.");
    }


    @org.junit.jupiter.api.Test
    void setCellOutOfBounds(){
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
        sudoku.toString();

        boolean flag = sudoku.solve();
            assertTrue(flag, "Sudoku cannot be solved.");

    }
}
