/*
 * Copyright (c) 2024. Robin Hillyard
 */

package com.phasmidsoftware.dsaipg.projects.mcts.tictactoe;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Move;
import com.phasmidsoftware.dsaipg.projects.mcts.core.Node;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class MCTS {

    private static final int ITERATIONS = 1000;
    private static final double EXPLORATION_CONSTANT = Math.sqrt(2);

    public static void main(String[] args) {
        // 用默认构造器初始化游戏，根节点对应起始状态
        TicTacToe game = new TicTacToe();
        TicTacToeNode root = new TicTacToeNode(game.new TicTacToeState());
        MCTS mcts = new MCTS(root);

        // 进行若干次模拟
        for (int i = 0; i < ITERATIONS; i++) {
            List<TicTacToeNode> path = new ArrayList<>();
            TicTacToeNode leaf = select(root, path);
            int simulationScore = simulate(leaf.state());
            backpropagate(path, simulationScore);
        }

        // 从根节点选择胜率最高的子节点作为最佳走法
        TicTacToeNode bestChild = bestChild(root);
        System.out.println("建议走法: " + bestChild.getMove());
        System.out.println("扩展后的局面:\n" + bestChild.state().toString());
    }

    private final TicTacToeNode root;

    public MCTS(TicTacToeNode root) {
        this.root = root;
    }

    // 选择阶段：递归沿着最高 UCT 值的子节点前进，直到到达叶节点或未完全扩展的节点
    private static TicTacToeNode select(TicTacToeNode node, List<TicTacToeNode> path) {
        path.add(node);
        if (node.state().isTerminal()) {
            return node;
        }
        if (!node.isFullyExpanded()) {
            return expand(node, path);
        } else {
            // 选择 UCT 值最大的子节点
            TicTacToeNode best = null;
            double bestValue = -Double.MAX_VALUE;
            for (TicTacToeNode child : node.getChildren()) {
                double uctValue = child.getWinRate() +
                        EXPLORATION_CONSTANT * Math.sqrt(Math.log(node.getPlayouts() + 1) / (child.getPlayouts() + 1));
                if (uctValue > bestValue) {
                    bestValue = uctValue;
                    best = child;
                }
            }
            if (best == null) {
                return node;
            }
            return select(best, path);
        }
    }

    // 扩展阶段：为当前节点中尚未探索的走法扩展出一个新子节点
    private static TicTacToeNode expand(TicTacToeNode node, List<TicTacToeNode> path) {
        Collection<Move<TicTacToe>> moves = node.state().moves(node.state().player());
        for (Move<TicTacToe> move : moves) {
            if (!node.hasChildForMove(move)) {
                State<TicTacToe> newState = node.state().next(move);
                TicTacToeNode child = new TicTacToeNode(newState, move);
                node.addChildNode(child);
                path.add(child);
                return child;
            }
        }
        // 如果所有走法均已扩展，则随机返回一个子节点
        return node.getChildren().iterator().next();
    }

    // 模拟阶段：从给定状态开始随机模拟直到游戏结束，并返回得分
    private static int simulate(State<TicTacToe> state) {
        State<TicTacToe> simState = state;
        int simulationPlayer = simState.player();
        Random rand = new Random();
        while (!simState.isTerminal()) {
            Collection<Move<TicTacToe>> moves = simState.moves(simState.player());
            if (moves.isEmpty()) break;
            int index = rand.nextInt(moves.size());
            Move<TicTacToe> move = moves.stream().skip(index).findFirst().get();
            simState = simState.next(move);
        }
        Optional<Integer> winner = simState.winner();
        // 得分规则：赢 2 分，平局 1 分，输 0 分
        if (winner.isPresent()) {
            if (winner.get() == simulationPlayer) return 2;
            else return 0;
        }
        return 1;
    }

    // 反向传播：沿着选择路径更新每个节点的统计数据
    private static void backpropagate(List<TicTacToeNode> path, int simulationScore) {
        for (TicTacToeNode node : path) {
            node.updateStats(simulationScore);
        }
    }

    // 从某节点中选出平均胜率最高的子节点
    private static TicTacToeNode bestChild(TicTacToeNode node) {
        TicTacToeNode best = null;
        double bestRate = -Double.MAX_VALUE;
        for (TicTacToeNode child : node.getChildren()) {
            double rate = child.getWinRate();
            if (rate > bestRate) {
                bestRate = rate;
                best = child;
            }
        }
        return best;
    }
}
