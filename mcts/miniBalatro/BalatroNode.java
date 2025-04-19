package com.phasmidsoftware.dsaipg.projects.mcts.miniBalatro;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Move;
import com.phasmidsoftware.dsaipg.projects.mcts.core.Node;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;

import java.util.*;

public class BalatroNode implements Node<BalatroGame> {
    private final State<BalatroGame> state;
    private final ArrayList<BalatroNode> children;
    private int wins;
    private int playouts;
    private final Move<BalatroGame> move;

    public BalatroNode(State<BalatroGame> state) {
        this(state, null);
    }

    public BalatroNode(State<BalatroGame> state, Move<BalatroGame> move) {
        this.state = state;
        this.move = move;
        this.children = new ArrayList<>();

        if (state.isTerminal()) {
            this.playouts = 1;
            this.wins = evaluateTerminalState((BalatroState) state);
        } else {
            this.playouts = 0;
            this.wins = 0;
        }
    }

    private int evaluateTerminalState(BalatroState state) {
        return state.getScore();
    }

    @Override
    public boolean isLeaf() {
        return state.isTerminal();
    }

    @Override
    public State<BalatroGame> state() {
        return state;
    }


    @Override
    public boolean white() {
        return state.player() == state.game().opener();
    }

    public Collection<Node<BalatroGame>> children() {
        return new ArrayList<>(children);
    }
    @Override
    public void addChild(State<BalatroGame> state) {
        BalatroNode child = new BalatroNode(state, null);
        children.add(child);
    }

    public void addChild(State<BalatroGame> state, Move<BalatroGame> move) {
        BalatroNode child = new BalatroNode(state, move);
        children.add(child);
    }

    @Override
    public void backPropagate() {
        if (isLeaf()) return;

        int totalWins = 0;
        int totalPlayouts = 0;

        for (BalatroNode child : children) {
            child.backPropagate();
            totalWins += child.wins();
            totalPlayouts += child.playouts();
        }

        this.wins = totalWins;
        this.playouts = totalPlayouts;
    }

    @Override
    public int wins() {
        return wins;
    }

    @Override
    public int playouts() {
        return playouts;
    }

    public Move<BalatroGame> getMove() {
        return move;
    }

    public boolean isFullyExpanded() {
        return children.size() >= state.moves(state.player()).size();
    }

    public void updateStats(int result) {
        wins += result;
        playouts++;
    }
}
