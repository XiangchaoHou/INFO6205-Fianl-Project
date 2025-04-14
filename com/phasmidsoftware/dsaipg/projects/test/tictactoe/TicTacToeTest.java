package com.phasmidsoftware.dsaipg.projects.com.phasmidsoftware.dsaipg.projects.test.tictactoe;

import com.phasmidsoftware.dsaipg.projects.mcts.core.State;
import com.phasmidsoftware.dsaipg.projects.mcts.tictactoe.TicTacToe;
import org.junit.Test;
import java.util.Optional;
import static org.junit.Assert.*;

public class TicTacToeTest {

    @Test
    public void testRunGame() {
        long seed = 0L;
        // Use a fixed seed to ensure deterministic game outcomes
        TicTacToe game = new TicTacToe(seed);
        State<TicTacToe> state = game.runGame();
        Optional<Integer> winner = state.winner();
        if (winner.isPresent()) {
            assertEquals("Winner should be player X.", Integer.valueOf(TicTacToe.X), winner.get());
        } else {
            fail("There should be a winner.");
        }
    }
}
