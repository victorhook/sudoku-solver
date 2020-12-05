#include <stdio.h>
#include <time.h>
#include <stdlib.h>
#include <string.h>


int valid(const int board[9][9], int row, int col, int value)
{
    // Horizontal & Vertical
    for (int i = 0; i < 9; i++)
    {
        if (board[row][i] == value || board[i][col] == value)
            return 0;
    }


    int start_row = 3 * (row / 3);
    int start_col = 3 *  (col / 3);

    // 3x3 grid
    for (int i = start_row; i < start_row+3; i++)
    {
        for (int j = start_col; j < start_col+3; j++)
        {
            if (board[i][j] == value)
                return 0;
        }
    }

    return 1;
}


int solve(int board[9][9], int row, int col)
{

    if (col == 9)   // End of row
    {
        if (row == 8)       // Base case
            return 1;
        row++;
        col = 0;
    }

    if (board[row][col] == 0) {
        for (int i = 1; i < 10; i++)
        {
            if (valid(board, row, col, i))
            {
                board[row][col] = i;
                if (solve(board, row, col+1))
                    return 1;
                board[row][col] = 0;
            }
        }
    } else {
        return solve(board, row, col+1);
    }

    return 0;
}


void gen(int board[9][9])
{
    const int CELLS_TO_FILL = 16;
    int filled = 0;
    int r = 0, c = 0;

    while (filled < CELLS_TO_FILL) {
        int value = rand() % 9;
        r = rand() % 8;
        c = rand() % 8;

        if (valid(board, r, c, value)) {
            board[r][c] = value;
            filled++;
        }
    }
}


void pprint(const int board[9][9])
{
    for (int i = 0; i < 9; i++)
    {
        printf("|");
        for (int j = 0; j < 9; j++)
        {
            printf(" %d ", board[i][j]);
        }
        printf("|\n");
    }
    printf("\n");
}

void fillboard(const char *boardString, int board[9][9]) {
    int r = 0, c = 0;
    while (*boardString) {
        board[r][c] = *boardString++ - 48;
        if (c == 8) {
            r++;
            c = 0;
        } else {
            c++;
        }
    }
}


void stringify(const int board[9][9]) {
    for (int r = 0; r < 9; r++) {
        for (int c = 0; c < 9; c++) {
            putchar(board[r][c] + 48);
        }
    }
}


int main(int argc, char *argv[]) {

    srand(1337);
    int board[9][9];

    if (argc > 1)
        fillboard(argv[1], board);
    else {
        for(int n = 0; n < 9; ++n)
            memset(board[n], 0, sizeof(int) * 9);
        gen(board);
    }

    //printf("Board:\n");
    //pprint(board);

    int ok = solve(board, 0, 0);
    if (ok) {
        stringify(board);       // Write to stdout
        return 0;
        //printf("Solution found\n");
        //pprint(board);
    } else
        //printf("No solution found!\n");
        return 1;
}
