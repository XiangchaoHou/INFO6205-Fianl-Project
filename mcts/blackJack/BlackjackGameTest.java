package com.phasmidsoftware.dsaipg.projects.mcts.blackJack;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BlackjackGameTest {

    @Test
    void testStartGame() {
        BlackjackGame game = new BlackjackGame();
        BlackjackState state = (BlackjackState) game.start();
        assertNotNull(state);
        assertEquals(2, state.playerHand.size());
        assertEquals(2, state.dealerHand.size());
    }
}
