package com.phasmidsoftware.dsaipg.projects.mcts.miniBalatro;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Move;

import java.util.*;

public class BalatroMove implements Move<BalatroGame> {
    public enum Action { PLAY, DISCARD }

    private final Action action;
    private final List<Card> cards; 
    private final int player;

    public BalatroMove(Action action, List<Card> cards, int player) {
        this.action = action;
        this.cards = cards;
        this.player = player;
    }

    public Action getAction() {
        return action;
    }

    public List<Card> getCards() {
        return new ArrayList<>(cards);
    }

    @Override
    public int player() {
        return player;
    }

    public String toString() {
        return action + " " + cards;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        BalatroMove other = (BalatroMove) obj;

        List<Card> thisList = new ArrayList<>(this.getCards());
        List<Card> otherList = new ArrayList<>(other.getCards());

        return action == other.action &&
                thisList.equals(otherList) &&
                player == other.player;
    }

    @Override
    public int hashCode() {
        int result = action.hashCode();
        result = 31 * result + cards.hashCode();
        result = 31 * result + player;
        return result;
    }
}
