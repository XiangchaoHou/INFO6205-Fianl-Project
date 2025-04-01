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
    private final Move<TicTacToe> move; // 记录从父状态到本状态的走法，根节点为 null

    // 用于根节点构造
    public TicTacToeNode(State<TicTacToe> state) {
        this(state, null);
    }

    // 构造时指定状态和走法
    public TicTacToeNode(State<TicTacToe> state, Move<TicTacToe> move) {
        this.state = state;
        this.move = move;
        this.children = new ArrayList<>();
        if (state.isTerminal()) {
            playouts = 1;
            if (state.winner().isPresent())
                wins = 2; // 胜局
            else
                wins = 1; // 平局
        } else {
            playouts = 0;
            wins = 0;
        }
    }

    // 返回本节点对应的走法
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

    // 返回所有子节点
    public Collection<TicTacToeNode> getChildren() {
        return children;
    }

    // 添加一个子节点
    @Override
    public void addChild(State<TicTacToe> state) {
        TicTacToeNode newNode = new TicTacToeNode(state);
        addChildNode(newNode);
    }

    public void addChildNode(TicTacToeNode child) {
        children.add(child);
    }



    // 用于反向传播时更新本节点的统计数据
    public void updateStats(int score) {
        wins += score;
        playouts += 1;
    }

    // 获取当前胜局数
    public int getWins() {
        return wins;
    }

    // 获取当前模拟次数
    public int getPlayouts() {
        return playouts;
    }

    // 返回平均胜率（得分 / 模拟次数）
    public double getWinRate() {
        if (playouts == 0) return 0;
        return (double) wins / playouts;
    }

    // 判断当前节点是否已将所有可能走法扩展完毕
    public boolean isFullyExpanded() {
        int possibleMoves = state().moves(state().player()).size();
        return children.size() >= possibleMoves;
    }

    // 判断该节点是否已存在对应走法的子节点
    public boolean hasChildForMove(Move<TicTacToe> move) {
        for (TicTacToeNode child : children) {
            if (child.getMove() != null && child.getMove().equals(move))
                return true;
        }
        return false;
    }

    // 以下方法保留原有骨架，可根据需要调整
    @Override
    public void backPropagate() {
        // 在本实现中我们使用 updateStats() 完成反向传播，不再调用此方法
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
