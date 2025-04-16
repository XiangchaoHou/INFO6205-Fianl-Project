package com.phasmidsoftware.dsaipg.projects.com.phasmidsoftware.dsaipg.projects.test.miniBalatro;

import com.phasmidsoftware.dsaipg.projects.mcts.miniBalatro.BalatroGame;
import com.phasmidsoftware.dsaipg.projects.mcts.miniBalatro.BalatroMCTS;
import com.phasmidsoftware.dsaipg.projects.mcts.core.Move;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;
import com.phasmidsoftware.dsaipg.projects.mcts.miniBalatro.BalatroNode;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.*;

public class BalatroMCTSTest {

    @Test
    public void testPlayGameTimingOutput() {
        // Capture System.out to verify timing line
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));

        try {
            BalatroGame game = new BalatroGame();
            BalatroMCTS.playGame(game);

            String log = out.toString();
            // 必须包含 "Time taken:"
            assertTrue("Output should contain 'Time taken:'", log.contains("Time taken:"));
        } finally {
            System.setOut(original);
        }
    }

    @Test
    public void testPlayGameBasicFlow() {
        // 验证关键输出项
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));

        try {
            BalatroGame game = new BalatroGame();
            BalatroMCTS.playGame(game);

            String log = out.toString();
            assertTrue("Should announce start", log.contains("Starting Balatro Decision Game!"));
            assertTrue("Should indicate new rounds", log.contains("=== New Round ==="));
            assertTrue("Should print final score", log.contains("Final Score:"));
        } finally {
            System.setOut(original);
        }
    }

    @Test
    public void testFindBestMoveNotNull() {
        BalatroGame game = new BalatroGame();
        State<BalatroGame> state = game.start();
        // 构造根节点
        BalatroNode root = new BalatroNode(state);
        BalatroMCTS mcts = new BalatroMCTS(root, 200);
        Move<BalatroGame> move = mcts.findBestMove();
        assertNotNull("Best move should not be null", move);
    }
}
