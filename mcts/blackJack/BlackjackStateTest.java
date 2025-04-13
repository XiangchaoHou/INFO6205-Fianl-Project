package com.phasmidsoftware.dsaipg.projects.mcts.blackJack;
import org.junit.jupiter.api.Test;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BlackjackStateTest {

    @Test
    void testStartState() {
        BlackjackGame game = new BlackjackGame();
        BlackjackState state = (BlackjackState) game.start();
        assertEquals(2, state.playerHand.size());
        assertEquals(2, state.dealerHand.size());
        assertFalse(state.isTerminal());
    }

    @Test
    void testPlayerHit() {
        BlackjackGame game = new BlackjackGame();
        BlackjackState state = (BlackjackState) game.start();
        BlackjackMove move = new BlackjackMove(BlackjackMove.Action.HIT, 0);
        BlackjackState nextState = (BlackjackState) state.next(move);
        assertTrue(nextState.playerHand.size() > state.playerHand.size());
    }

    @Test
    void testPlayerStand() {
        BlackjackGame game = new BlackjackGame();
        BlackjackState state = (BlackjackState) game.start();
        BlackjackMove move = new BlackjackMove(BlackjackMove.Action.STAND, 0);
        BlackjackState nextState = (BlackjackState) state.next(move);
        assertEquals(1, nextState.player);
    }

    @Test
    void testWinnerPlayerBusts() {
        BlackjackGame game = new BlackjackGame();
        Deque<Integer> deck = new ArrayDeque<>();
        deck.push(10);
        deck.push(10);
        deck.push(5);
        BlackjackState state = new BlackjackState(game, List.of(10, 10), List.of(5, 6), deck, 0, new java.util.Random());
        BlackjackMove move = new BlackjackMove(BlackjackMove.Action.HIT, 0);
        BlackjackState nextState = (BlackjackState) state.next(move);
        assertTrue(nextState.isTerminal());
        assertEquals(1, nextState.winner().orElse(-1));
    }

    @Test
    void testWinnerDealerBusts() {
        BlackjackGame game = new BlackjackGame();
        Deque<Integer> deck = new ArrayDeque<>();
        deck.push(10); // Dealer will draw a 10

        // Dealer has 16 to start, draws 10 → busts
        BlackjackState state = new BlackjackState(game, List.of(10, 5), List.of(9, 7), deck, 0, new java.util.Random());
        BlackjackMove move = new BlackjackMove(BlackjackMove.Action.STAND, 0);
        BlackjackState nextState = (BlackjackState) state.next(move);
        assertTrue(nextState.isTerminal());
        assertEquals(0, nextState.winner().orElse(-1));
    }
}

