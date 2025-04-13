package com.phasmidsoftware.dsaipg.projects.mcts.blackJack;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BlackjackNodeTest {

    @Test
    void testWinsAndPlayouts() {
        BlackjackGame game = new BlackjackGame();
        BlackjackState state = (BlackjackState) game.start();
        BlackjackNode node = new BlackjackNode(state);
        assertEquals(0, node.wins());
        assertEquals(0, node.playouts());
        node.updateStats(2);
        assertEquals(2, node.wins());
        assertEquals(1, node.playouts());
    }

    @Test
    void testIsFullyExpanded() {
        BlackjackGame game = new BlackjackGame();
        BlackjackState state = (BlackjackState) game.start();
        BlackjackNode node = new BlackjackNode(state);
        assertFalse(node.isFullyExpanded());
        for (var move : state.moves(state.player())) {
            node.addChild(state.next(move), move);
        }
        assertTrue(node.isFullyExpanded());
    }

    @Test
    void testLeafNode() {
        BlackjackGame game = new BlackjackGame();
        BlackjackState state = (BlackjackState) game.start();
        BlackjackNode node = new BlackjackNode(state);
        if (state.isTerminal()) {
            assertTrue(node.isLeaf());
        } else {
            assertFalse(node.isLeaf());
        }
    }
}