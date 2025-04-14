package com.phasmidsoftware.dsaipg.projects.com.phasmidsoftware.dsaipg.projects.test.tictactoe;

import com.phasmidsoftware.dsaipg.projects.mcts.tictactoe.MCTS;
import com.phasmidsoftware.dsaipg.projects.mcts.tictactoe.TicTacToe;
import com.phasmidsoftware.dsaipg.projects.mcts.tictactoe.TicTacToeNode;
import com.phasmidsoftware.dsaipg.projects.mcts.core.Move;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class MCTSTest {

    @Test
    public void testFindBestMove() {
        TicTacToe game = new TicTacToe(0L); // Use a fixed seed for deterministic behavior
        State<TicTacToe> state = game.start();
        TicTacToeNode rootNode = new TicTacToeNode(state);
        MCTS mcts = new MCTS(rootNode, 500); // 500 iterations for testing
        Move<TicTacToe> bestMove = mcts.findBestMove();
        // Verify that the best move is not null
        assertNotNull("Best move should not be null", bestMove);
    }

    @Test
    public void testPlayFullGameRuntime() {
        // Capture System.out output to verify runtime recording
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        try {
            TicTacToe game = new TicTacToe(0L); // Fixed seed
            MCTS.playFullGame(game);
            String output = outContent.toString();
            // Check that output contains runtime information ("Time taken:")
            assertTrue("Output should contain runtime info", output.contains("Time taken:"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testPlayFullGameOutputContent() {
        // Capture output to verify that key strings are printed during a full game
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        try {
            TicTacToe game = new TicTacToe(0L); // Fixed seed
            MCTS.playFullGame(game);
            String output = outContent.toString();
            // Check that output contains important strings such as "Start!", "Board:" and "Move:"
            assertTrue("Output should contain 'Start!'", output.contains("Start!"));
            assertTrue("Output should contain 'Board:'", output.contains("Board:"));
            assertTrue("Output should contain 'Move:'", output.contains("Move:"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testPlayFullGameWinnerPresent() {
        // Capture output to verify that the final result indicates either a winner or a draw
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        try {
            TicTacToe game = new TicTacToe(0L); // Fixed seed for determinism
            MCTS.playFullGame(game);
            String output = outContent.toString();
            // Check that output includes either "Winner:" or "Draw!"
            boolean containsWinner = output.contains("Winner:");
            boolean containsDraw = output.contains("Draw!");
            assertTrue("Output should indicate a winner or a draw", containsWinner || containsDraw);
        } finally {
            System.setOut(originalOut);
        }
    }
}
