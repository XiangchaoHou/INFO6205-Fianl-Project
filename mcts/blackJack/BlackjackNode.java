package com.phasmidsoftware.dsaipg.projects.mcts.blackJack;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Node;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;
import com.phasmidsoftware.dsaipg.projects.mcts.core.Move;

import java.util.ArrayList;
import java.util.Collection;

public class BlackjackNode implements Node<BlackjackGame> {
    private final State<BlackjackGame> state;
    private final ArrayList<BlackjackNode> children;
    private int wins;
    private int playouts;
    private final Move<BlackjackGame> move;

    public BlackjackNode(State<BlackjackGame> state) {
        this(state, null);
    }

    public BlackjackNode(State<BlackjackGame> state, Move<BlackjackGame> move) {
        this.state = state;
        this.move = move;
        this.children = new ArrayList<>();
        if (state.isTerminal()) {
            playouts = 1;
            wins = state.winner().isPresent() ? 2 : 1;
        } else {
            playouts = 0;
            wins = 0;
        }
    }

    @Override
    public boolean isLeaf() {
        return state.isTerminal();
    }

    @Override
    public State<BlackjackGame> state() {
        return state;
    }

    @Override
    public boolean white() {
        return state.player() == state.game().opener();
    }

    @Override
    public Collection<Node<BlackjackGame>> children() {
        return new ArrayList<>(children);
    }

    @Override
    public void addChild(State<BlackjackGame> state) {
        BlackjackNode child = new BlackjackNode(state, null);
        children.add(child);
    }

    public void addChild(State<BlackjackGame> state, Move<BlackjackGame> move) {
        BlackjackNode child = new BlackjackNode(state, move);
        children.add(child);
    }

    @Override
    public void backPropagate() {}

    @Override
    public int wins() {
        return wins;
    }

    @Override
    public int playouts() {
        return playouts;
    }

    public Move<BlackjackGame> getMove() {
        return move;
    }

    public int getPlayouts() {
        return playouts;
    }

    public int getWins() {
        return wins;
    }

    public boolean isFullyExpanded() {
        return children.size() >= state.moves(state.player()).size();
    }

    public void updateStats(int result) {
        wins += result;
        playouts++;
    }
}