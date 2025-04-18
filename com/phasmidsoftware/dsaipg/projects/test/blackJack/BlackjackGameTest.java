package com.phasmidsoftware.dsaipg.projects.com.phasmidsoftware.dsaipg.projects.test.blackJack;

import com.phasmidsoftware.dsaipg.projects.mcts.blackJack.BlackjackGame;
import com.phasmidsoftware.dsaipg.projects.mcts.blackJack.BlackjackState;
import org.testng.annotations.Test;
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
