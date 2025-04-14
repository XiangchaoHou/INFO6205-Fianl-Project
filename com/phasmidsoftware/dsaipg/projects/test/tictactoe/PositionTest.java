package com.phasmidsoftware.dsaipg.projects.com.phasmidsoftware.dsaipg.projects.test.tictactoe;

import com.phasmidsoftware.dsaipg.projects.mcts.tictactoe.Position;
import org.junit.Test;
import java.util.List;
import java.util.Optional;
import static org.junit.Assert.*;

public class PositionTest {

    @Test(expected = RuntimeException.class)
    public void testMove_InvalidOccupiedCell() {
        String grid = "X . .\n. O .\n. . X";
        Position target = Position.parsePosition(grid, 1);
        // Attempting to move on an already occupied cell (0,0)
        target.move(1, 0, 0);
    }

    @Test(expected = RuntimeException.class)
    public void testMove_ConsecutiveMoves() {
        String grid = "X X 0\nX O 0\nX X 0";
        Position target = Position.parsePosition(grid, 1);
        target.move(1, 0, 0);
    }

    @Test(expected = RuntimeException.class)
    public void testMove_InvalidIndex() {
        String grid = "X . .\n. O .\n. . X";
        Position target = Position.parsePosition(grid, 1);
        // Attempt to move on a cell that is not allowed based on game rules
        target.move(1, 0, 1);
    }

    @Test
    public void testMove_Valid() {
        String grid = "X . .\n. O .\n. . X";
        Position target = Position.parsePosition(grid, 1);
        Position moved = target.move(0, 0, 1);
        String expectedGrid = grid.replaceFirst("\\.", "O"); // replace first occurrence of "." with "O"
        Position expected = Position.parsePosition(expectedGrid, 0);
        assertEquals("The board after a valid move should match the expected state.", expected, moved);
    }

    @Test
    public void testMoves() {
        String grid = "X . .\n. O .\n. . X";
        Position target = Position.parsePosition(grid, 1);
        List<int[]> moves = target.moves(0);
        assertEquals("There should be 6 available moves.", 6, moves.size());
        assertArrayEquals("Move 0 should be at (0, 1).", new int[]{0, 1}, moves.get(0));
        assertArrayEquals("Move 1 should be at (0, 2).", new int[]{0, 2}, moves.get(1));
        assertArrayEquals("Move 2 should be at (1, 0).", new int[]{1, 0}, moves.get(2));
        assertArrayEquals("Move 3 should be at (1, 2).", new int[]{1, 2}, moves.get(3));
        assertArrayEquals("Move 4 should be at (2, 0).", new int[]{2, 0}, moves.get(4));
        assertArrayEquals("Move 5 should be at (2, 1).", new int[]{2, 1}, moves.get(5));
    }

    @Test
    public void testReflect() {
        // Add appropriate test implementation for reflect method if needed
    }

    @Test
    public void testRotate() {
        // Add appropriate test implementation for rotate method if needed
    }

    @Test
    public void testWinner_NoWinner() {
        String grid = "X . .\n. O .\n. . X";
        Position target = Position.parsePosition(grid, 1);
        assertTrue("There should be no winner.", target.winner().isEmpty());
    }

    @Test
    public void testWinner_PlayerX() {
        String grid = "X . 0\nX O .\nX . 0";
        Position target = Position.parsePosition(grid, 1);
        Optional<Integer> winner = target.winner();
        assertTrue("There should be a winner.", winner.isPresent());
        assertEquals("Player X should be the winner.", Integer.valueOf(1), winner.get());
    }

    @Test
    public void testWinner_PlayerO() {
        String grid = "0 . X\n0 X .\nO . X";
        Position target = Position.parsePosition(grid, 0);
        Optional<Integer> winner = target.winner();
        assertTrue("There should be a winner.", winner.isPresent());
        assertEquals("Player O should be the winner.", Integer.valueOf(0), winner.get());
    }

    @Test
    public void testProjectRow() {
        String grid = "X . 0\nX O .\nX . 0";
        Position target = Position.parsePosition(grid, 1);
        assertArrayEquals("Row 0 should be as expected.", new int[]{1, -1, 0}, target.projectRow(0));
        assertArrayEquals("Row 1 should be as expected.", new int[]{1, 0, -1}, target.projectRow(1));
        assertArrayEquals("Row 2 should be as expected.", new int[]{1, -1, 0}, target.projectRow(2));
    }

    @Test
    public void testProjectCol() {
        String grid = "X . 0\nX O .\nX . 0";
        Position target = Position.parsePosition(grid, 1);
        assertArrayEquals("Column 0 should be as expected.", new int[]{1, 1, 1}, target.projectCol(0));
        assertArrayEquals("Column 1 should be as expected.", new int[]{-1, 0, -1}, target.projectCol(1));
        assertArrayEquals("Column 2 should be as expected.", new int[]{0, -1, 0}, target.projectCol(2));
    }

    @Test
    public void testProjectDiag() {
        String grid = "X . 0\nX O .\nX . 0";
        Position target = Position.parsePosition(grid, 1);
        assertArrayEquals("Primary diagonal should be as expected.", new int[]{1, 0, 0}, target.projectDiag(true));
        assertArrayEquals("Secondary diagonal should be as expected.", new int[]{1, 0, 0}, target.projectDiag(false));
    }

    @Test
    public void testParseCell() {
        assertEquals("0 should parse to 0.", 0, Position.parseCell("0"));
        assertEquals("O should parse to 0.", 0, Position.parseCell("O"));
        assertEquals("o should parse to 0.", 0, Position.parseCell("o"));
        assertEquals("X should parse to 1.", 1, Position.parseCell("X"));
        assertEquals("x should parse to 1.", 1, Position.parseCell("x"));
        assertEquals("1 should parse to 1.", 1, Position.parseCell("1"));
        assertEquals("'.' should parse to -1.", -1, Position.parseCell("."));
        assertEquals("Other characters should parse to -1.", -1, Position.parseCell("a"));
    }

    @Test
    public void testThreeInARow() {
        String grid = "X . 0\nX O .\nX . 0";
        Position target = Position.parsePosition(grid, 1);
        assertTrue("There should be three in a row.", target.threeInARow());
    }

    @Test
    public void testFull() {
        assertFalse("The board should not be full.", Position.parsePosition("X . 0\nX O .\nX . 0", 1).full());
        assertTrue("The board should be full.", Position.parsePosition("X X 0\nX O 0\nX X 0", 1).full());
    }

    @Test
    public void testRender() {
        String grid = "X . .\n. O .\n. . X";
        Position target = Position.parsePosition(grid, 1);
        assertEquals("The render output should match the grid.", grid, target.render());
    }

    @Test
    public void testToString() {
        Position target = Position.parsePosition("X . .\n. O .\n. . X", 1);
        String expected = "1,-1,-1\n-1,0,-1\n-1,-1,1";
        assertEquals("The toString output should match the expected string.", expected, target.toString());
    }
}
