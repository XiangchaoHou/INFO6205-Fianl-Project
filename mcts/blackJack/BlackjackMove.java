package com.phasmidsoftware.dsaipg.projects.mcts.blackJack;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Move;
/**
 * Define the player move operation like hit ot stand
 * hit = draw a card, stand = stop drawing
 */
public class BlackjackMove implements Move<BlackjackGame> {
    // define two possible moves
    public enum Action { HIT, STAND }

    private final Action action;
    private final int player;

    public BlackjackMove(Action action, int player) {
        this.action = action;
        this.player = player;
    }

    // return action hit or stand
    public Action getAction() {
        return action;
    }

    // Returns which player this move belongs to 0 for Player, 1 for Dealer
    @Override
    public int player() {
        return player;
    }

    @Override
    public String toString() {
        return action.toString();
    }
}