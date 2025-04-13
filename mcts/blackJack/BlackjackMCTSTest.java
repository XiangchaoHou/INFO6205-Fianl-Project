package com.phasmidsoftware.dsaipg.projects.mcts.blackJack;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BlackjackMCTSTest {

    @Test
    void testFindBestMove() {
        BlackjackGame game = new BlackjackGame();
        BlackjackState initialState = (BlackjackState) game.start();
        BlackjackNode rootNode = new BlackjackNode(initialState);
        BlackjackMCTS mcts = new BlackjackMCTS(rootNode, 100);
        BlackjackMove bestMove = (BlackjackMove) mcts.findBestMove();
        assertNotNull(bestMove);
    }

    @Test
    void testPlayFullGame() {
        BlackjackGame game = new BlackjackGame();
        BlackjackState currentState = (BlackjackState) game.start();

        while (!currentState.isTerminal()) {
            BlackjackNode rootNode = new BlackjackNode(currentState);
            BlackjackMCTS mcts = new BlackjackMCTS(rootNode, 100);
            BlackjackMove bestMove = (BlackjackMove) mcts.findBestMove();
            assertNotNull(bestMove);
            currentState = (BlackjackState) currentState.next(bestMove);
        }

        assertTrue(currentState.isTerminal());
    }
}