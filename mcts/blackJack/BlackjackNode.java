package com.phasmidsoftware.dsaipg.projects.mcts.blackJack;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Node;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;
import com.phasmidsoftware.dsaipg.projects.mcts.core.Move;

import java.util.ArrayList;
import java.util.Collection;
/**
 * Represents the tree node for MCTS
 * Wraps a BlackjackState. - Tracks children, move, wins, and playouts. Used in MCTS tree expansion and selection.
 */
public class BlackjackNode implements Node<BlackjackGame> {
    /**
     * state: The current game situation at this node (the BlackjackState).
     * children: The list of child nodes (nodes created by making moves from this state).
     * wins: Number of wins seen from this node in simulations.
     * playouts: Number of simulations (games played from this node).
     * move: The move that led to this node from its parent.
     */
    private final State<BlackjackGame> state;
    private final ArrayList<BlackjackNode> children;
    private int wins;
    private int playouts;
    private final Move<BlackjackGame> move;

    // Creates a node with a given state and no move (for root node).
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

    // Custom method to add child with move
    public void addChild(State<BlackjackGame> state, Move<BlackjackGame> move) {
        BlackjackNode child = new BlackjackNode(state, move);
        children.add(child);
    }

    @Override
    public void backPropagate() {
        // Not used manually.
    }

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