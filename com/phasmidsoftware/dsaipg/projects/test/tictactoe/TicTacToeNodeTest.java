package com.phasmidsoftware.dsaipg.projects.com.phasmidsoftware.dsaipg.projects.test.tictactoe;

import com.phasmidsoftware.dsaipg.projects.mcts.tictactoe.Position;
import com.phasmidsoftware.dsaipg.projects.mcts.tictactoe.TicTacToe;
import com.phasmidsoftware.dsaipg.projects.mcts.tictactoe.TicTacToeNode;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;
import org.junit.Test;
import static org.junit.Assert.*;

public class TicTacToeNodeTest {

    @Test
    public void testWinsAndPlayoutsForTerminalNode() {
        // Create a terminal state (winning board) for testing.
        TicTacToe.TicTacToeState terminalState =
                new TicTacToe().new TicTacToeState(Position.parsePosition("X . 0\nX O .\nX . 0", TicTacToe.X));
        TicTacToeNode node = new TicTacToeNode(terminalState);
        // For a terminal state, the node should be a leaf.
        assertTrue("Terminal node should be a leaf", node.isLeaf());
        // If a winner is present, wins should be 2; otherwise (draw) wins should be 1.
        if (terminalState.winner().isPresent()) {
            assertEquals("Wins should be 2 for a winning terminal state", 2, node.wins());
        } else {
            assertEquals("Wins should be 1 for a terminal draw state", 1, node.wins());
        }
        // Terminal nodes have 1 playout.
        assertEquals("Playouts for a terminal node should be 1", 1, node.playouts());
    }

    @Test
    public void testStateMethod() {
        TicTacToe.TicTacToeState state = new TicTacToe().new TicTacToeState();
        TicTacToeNode node = new TicTacToeNode(state);
        assertEquals("The state method should return the node's state", state, node.state());
    }

    @Test
    public void testWhiteMethod() {
        TicTacToe.TicTacToeState state = new TicTacToe().new TicTacToeState();
        TicTacToeNode node = new TicTacToeNode(state);
        // Assuming the opener is X, node.white() should return true.
        assertTrue("Node should represent a white move (opening player)", node.white());
    }

    @Test
    public void testChildrenMethod() {
        // Create a non-terminal starting state.
        TicTacToe.TicTacToeState state = new TicTacToe().new TicTacToeState(
                Position.parsePosition(". . .\n. . .\n. . .", TicTacToe.blank)
        );
        TicTacToeNode parent = new TicTacToeNode(state);
        // Initially, children collection should be empty.
        assertEquals("Initially, children should be empty", 0, parent.getChildren().size());

        // Create a child state by performing a move.
        State<TicTacToe> childState = state.next(state.chooseMove(state.player()));
        TicTacToeNode childNode = new TicTacToeNode(childState);
        parent.addChildNode(childNode);

        // Now, children collection should have one node.
        assertEquals("After adding one child, children size should be 1", 1, parent.getChildren().size());
    }

    @Test
    public void testAddChildMethod() {
        // Create a non-terminal starting state.
        TicTacToe.TicTacToeState state = new TicTacToe().new TicTacToeState(
                Position.parsePosition(". . .\n. . .\n. . .", TicTacToe.blank)
        );
        TicTacToeNode parent = new TicTacToeNode(state);
        int initialChildCount = parent.getChildren().size();

        // Create a child state using parent's next move.
        State<TicTacToe> childState = state.next(state.chooseMove(state.player()));
        // Use the addChild(State<TicTacToe>) method.
        parent.addChild(childState);

        assertEquals("After adding a child via addChild, children count should increase by 1",
                initialChildCount + 1, parent.getChildren().size());
    }

    @Test
    public void testBackPropagateUsingUpdateStats() {
        // Test the updateStats mechanism for back propagation.
        TicTacToe.TicTacToeState state = new TicTacToe().new TicTacToeState();
        TicTacToeNode node = new TicTacToeNode(state);
        int initialWins = node.getWins();
        int initialPlayouts = node.getPlayouts();

        // Simulate two updates: one win (score = 2) and one draw (score = 1).
        node.updateStats(2);
        node.updateStats(1);

        assertEquals("Wins should be updated correctly", initialWins + 2 + 1, node.getWins());
        assertEquals("Playouts should be updated correctly", initialPlayouts + 2, node.getPlayouts());
    }

    @Test
    public void testGetWinRate() {
        // Test the win rate calculation.
        TicTacToe.TicTacToeState state = new TicTacToe().new TicTacToeState();
        TicTacToeNode node = new TicTacToeNode(state);
        // Update stats: one win (2 points), one loss (0 points), and one draw (1 point).
        node.updateStats(2);
        node.updateStats(0);
        node.updateStats(1);
        double expectedWinRate = (double) (2 + 0 + 1) / 3;
        assertEquals("Win rate should be computed correctly", expectedWinRate, node.getWinRate(), 0.0001);
    }
}
