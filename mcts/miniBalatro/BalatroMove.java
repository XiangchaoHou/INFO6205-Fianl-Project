package com.phasmidsoftware.dsaipg.projects.mcts.miniBalatro;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Move;

import java.util.*;

// 定义玩家行动
public class BalatroMove implements Move<BalatroGame> {
    public enum Action { PLAY, DISCARD }

    private final Action action;
    private final List<Card> cards; // 要出或丢的牌
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
        return new ArrayList<>(cards);  // 始终返回防御性副本
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

        // 使用新列表进行比较以避免并发修改
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
