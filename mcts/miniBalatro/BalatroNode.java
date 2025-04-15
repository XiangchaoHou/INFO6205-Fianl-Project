package com.phasmidsoftware.dsaipg.projects.mcts.miniBalatro;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Move;
import com.phasmidsoftware.dsaipg.projects.mcts.core.Node;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;

import java.util.*;

// MCTS树节点
public class BalatroNode implements Node<BalatroGame> {
    // 当前状态
    // 子节点列表
    // 获胜次数
    // 模拟次数
    // 导致此节点的行动
    private final State<BalatroGame> state;
    private final ArrayList<BalatroNode> children;
    private int wins;
    private int playouts;
    private final Move<BalatroGame> move;

    // 构造函数
    public BalatroNode(State<BalatroGame> state) {
        this(state, null);
    }

    public BalatroNode(State<BalatroGame> state, Move<BalatroGame> move) {
        this.state = state;
        this.move = move;
        this.children = new ArrayList<>();

        // If this is a terminal state, initialize with its evaluation
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
    // 添加子节点方法
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
        // For leaf nodes, wins and playouts are already set
        if (isLeaf()) return;

        // For non-leaf nodes with children, aggregate children's stats
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

    /**
     * Get the move that led to this node
     */
    public Move<BalatroGame> getMove() {
        return move;
    }

    /**
     * Check if all possible moves from this state have been tried
     */
    public boolean isFullyExpanded() {
        return children.size() >= state.moves(state.player()).size();
    }

    /**
     * Update the node's statistics based on a simulation result
     */
    public void updateStats(int result) {
        wins += result;
        playouts++;
    }
}
