package com.phasmidsoftware.dsaipg.projects.com.phasmidsoftware.dsaipg.projects.test.blackJack;

import com.phasmidsoftware.dsaipg.projects.mcts.blackJack.BlackjackMove;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BlackjackMoveTest {

    @Test
    void testMoveCreation() {
        BlackjackMove move = new BlackjackMove(BlackjackMove.Action.HIT, 0);
        assertEquals(BlackjackMove.Action.HIT, move.getAction());
        assertEquals(0, move.player());
    }
}