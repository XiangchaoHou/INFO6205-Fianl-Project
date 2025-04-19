/*
 * Copyright (c) 2024. Robin Hillyard
 */

package com.phasmidsoftware.dsaipg.projects.mcts.tictactoe;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Node;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;
import com.phasmidsoftware.dsaipg.projects.mcts.core.Move;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class TicTacToeNode implements Node<TicTacToe> {

    private final State<TicTacToe> state;
    private final ArrayList<TicTacToeNode> children;
    private int wins;
    private int playouts;
    private final Move<TicTacToe> move;

    public TicTacToeNode(State<TicTacToe> state) {
        this(state, null);
    }

    public TicTacToeNode(State<TicTacToe> state, Move<TicTacToe> move) {
        this.state = state;
        this.move = move;
        this.children = new ArrayList<>();
        if (state.isTerminal()) {
            playouts = 1;
            if (state.winner().isPresent())
                wins = 2; 
            else
                wins = 1; 
        } else {
            playouts = 0;
            wins = 0;
        }
    }

    public Move<TicTacToe> getMove() {
        return move;
    }

    @Override
    public boolean isLeaf() {
        return state().isTerminal();
    }

    @Override
    public State<TicTacToe> state() {
        return state;
    }

    public Collection<TicTacToeNode> getChildren() {
        return children;
    }

    @Override
    public void addChild(State<TicTacToe> state) {
        TicTacToeNode newNode = new TicTacToeNode(state);
        addChildNode(newNode);
    }

    public void addChildNode(TicTacToeNode child) {
        children.add(child);
    }

    public void updateStats(int score) {
        wins += score;
        playouts += 1;
    }

    public int getWins() {
        return wins;
    }

    public int getPlayouts() {
        return playouts;
    }

    public double getWinRate() {
        if (playouts == 0) return 0;
        return (double) wins / playouts;
    }

    public boolean isFullyExpanded() {
        int possibleMoves = state().moves(state().player()).size();
        return children.size() >= possibleMoves;
    }

    public boolean hasChildForMove(Move<TicTacToe> move) {
        for (TicTacToeNode child : children) {
            if (child.getMove() != null && child.getMove().equals(move))
                return true;
        }
        return false;
    }

    @Override
    public void backPropagate() {
    }

    @Override
    public int wins() {
        return wins;
    }

    @Override
    public int playouts() {
        return playouts;
    }

    @Override
    public boolean white() {
        return state().player() == state().game().opener();
    }

    @Override
    public Collection<Node<TicTacToe>> children() {
        return new ArrayList<>(children);
    }

    @Override
    public String toString() {
        return "TicTacToeNode{" +
                "state=" + state +
                ", move=" + move +
                ", wins=" + wins +
                ", playouts=" + playouts +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TicTacToeNode that)) return false;
        return Objects.equals(state, that.state) &&
                Objects.equals(move, that.move);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, move);
    }
}