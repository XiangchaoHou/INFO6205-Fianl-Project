package com.phasmidsoftware.dsaipg.projects.mcts.blackJack;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Move;

public class BlackjackMove implements Move<BlackjackGame> {
    public enum Action { HIT, STAND }

    private final Action action;
    private final int player;

    public BlackjackMove(Action action, int player) {
        this.action = action;
        this.player = player;
    }

    public Action getAction() {
        return action;
    }

    @Override
    public int player() {
        return player;
    }

    @Override
    public String toString() {
        return action.toString();
    }
}