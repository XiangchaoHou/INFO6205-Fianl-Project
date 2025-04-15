package com.phasmidsoftware.dsaipg.projects.mcts.miniBalatro;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Move;
import com.phasmidsoftware.dsaipg.projects.mcts.core.Node;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;

import java.util.*;

// 蒙特卡洛树搜索实现
public class BalatroMCTS {
    // 探索参数
    // 最大迭代次数
    // 根节点
    // 随机数生成器
    private final double explorationParameter = Math.sqrt(2);
    private final int maxIterations;
    private final Random random;
    private final BalatroNode root;

    public static void main(String[] args) {
        // 创建游戏并开始
        BalatroGame game = new BalatroGame();
        playGame(game);
    }

    // 游戏主循环
    public static void playGame(BalatroGame game) {
        // 初始化游戏状态
        // 显示初始状态
        // 循环直到游戏结束:
        //   使用MCTS寻找最佳行动
        //   执行行动
        //   更新并显示状态
        // 显示最终结果
        State<BalatroGame> currentState = game.start();

        System.out.println("Starting Balatro Decision Game!");
        System.out.println(currentState);

        while (!currentState.isTerminal()) {
            System.out.println("\n=== New Round ===");

            BalatroNode rootNode = new BalatroNode(currentState);
            BalatroMCTS mcts = new BalatroMCTS(rootNode, 500); // Adjust iterations for performance

//            System.out.println("Cards in hand: " + ((BalatroState)currentState).hand);

            Move<BalatroGame> bestMove = mcts.findBestMove();

            if (bestMove == null) {
                System.out.println("No possible moves!");
                break;
            }

            System.out.println("MCTS chose move: " + bestMove);

            int previousScore = ((BalatroState)currentState).getScore();

            currentState = currentState.next(bestMove);

            int newScore = ((BalatroState)currentState).getScore();
            int moveScore = newScore - previousScore;

            System.out.println("Move score: +" + moveScore + " points");
            System.out.println("Total score: " + newScore);
            System.out.println(currentState);
        }

        System.out.println("\nGame Over!");
        System.out.println("Final Score: " + ((BalatroState)currentState).getScore());

        analyzeResult((BalatroState)currentState);
    }

    public BalatroMCTS(BalatroNode root, int maxIterations) {
        this.root = root;
        this.maxIterations = maxIterations;
        this.random = new Random();
    }

    private static void analyzeResult(BalatroState state) {
        System.out.println("\n=== Game Analysis ===");
        System.out.println("Cards on table: " + state.table);
        System.out.println("Cards in hand: " + state.hand);
        System.out.println("Cards in deck: " + state.deck.size());
        System.out.println("Total plays made: " + (5 - state.remainingPlays));
        System.out.println("Total discards made: " + (3 - state.remainingDiscards));
        System.out.println("Final score: " + state.getScore());
    }

    // 寻找最佳行动
    public Move<BalatroGame> findBestMove() {
        // 执行MCTS迭代
        // 返回访问次数最多的子节点对应的行动
        if (root.isLeaf()) {
            return null;
        }

        for (int i = 0; i < maxIterations; i++) {
            List<BalatroNode> path = new ArrayList<>();
            BalatroNode selectedNode = selectWithPath(root, path);

            BalatroNode expandedNode = selectedNode;
            if (!selectedNode.isLeaf()) {
                expandedNode = expand(selectedNode);
                path.add(expandedNode);
            }

            int simulationResult = simulate(expandedNode);

            backpropagate(path, simulationResult);
        }

        return getMostVisitedChild(root).getMove();
    }

    private BalatroNode getMostVisitedChild(BalatroNode node) {
        Collection<Node<BalatroGame>> children = node.children();

        BalatroNode mostVisitedChild = null;
        int mostVisits = -1;

        for (Node<BalatroGame> childNode : children) {
            BalatroNode child = (BalatroNode) childNode;
            if (child.playouts() > mostVisits) {
                mostVisits = child.playouts();
                mostVisitedChild = child;
            }
        }

        return mostVisitedChild;
    }



    // 选择阶段：选择最有前途的节点
    private BalatroNode selectWithPath(BalatroNode node, List<BalatroNode> path) {
        BalatroNode currentNode = node;
        path.add(currentNode);

        while (!currentNode.isLeaf() && currentNode.isFullyExpanded()) {
            BalatroNode bestChild = getBestChild(currentNode, explorationParameter);
            if (bestChild == null) {
                break;
            }
            currentNode = bestChild;
            path.add(currentNode);
        }

        return currentNode;
    }

    private BalatroNode getBestChild(BalatroNode node, double explorationValue) {
        Collection<Node<BalatroGame>> children = node.children();

        BalatroNode bestChild = null;
        double bestValue = Double.NEGATIVE_INFINITY;

        for (Node<BalatroGame> childNode : children) {
            BalatroNode child = (BalatroNode) childNode;

            if (child.playouts() == 0) continue;

            // UCT公式: 开发项 + 探索项
            double exploitationTerm = (double) child.wins() / child.playouts();
            double explorationTerm = explorationValue *
                    Math.sqrt(Math.log(node.playouts()) / child.playouts());
            double uctValue = exploitationTerm + explorationTerm;

            if (uctValue > bestValue) {
                bestValue = uctValue;
                bestChild = child;
            }
        }

        return bestChild;
    }

    private BalatroNode expand(BalatroNode node) {
        Collection<Move<BalatroGame>> possibleMoves = node.state().moves(node.state().player());

        // 创建一个不可变的移动列表副本
        List<Move<BalatroGame>> untriedMoves = new ArrayList<>();

        for (Move<BalatroGame> move : possibleMoves) {
            boolean alreadyTried = false;

            // 获取已尝试过的移动列表的快照
            List<Node<BalatroGame>> childrenSnapshot = new ArrayList<>(node.children());

            for (Node<BalatroGame> childNode : childrenSnapshot) {
                BalatroNode child = (BalatroNode) childNode;
                Move<BalatroGame> childMove = child.getMove();

                // 安全比较，确保不会修改原始列表
                if (childMove != null && moveEquals(childMove, move)) {
                    alreadyTried = true;
                    break;
                }
            }

            if (!alreadyTried) {
                untriedMoves.add(move);
            }
        }

        if (untriedMoves.isEmpty()) return node;

        // 选择随机未尝试的移动
        Move<BalatroGame> move = untriedMoves.get(random.nextInt(untriedMoves.size()));
        State<BalatroGame> newState = node.state().next(move);

        // 添加新子节点
        node.addChild(newState, move);
        return new BalatroNode(newState, move);
    }

    // 辅助方法进行安全比较
    private boolean moveEquals(Move<BalatroGame> move1, Move<BalatroGame> move2) {
        if (move1 == move2) return true;
        if (move1 == null || move2 == null) return false;

        BalatroMove bMove1 = (BalatroMove) move1;
        BalatroMove bMove2 = (BalatroMove) move2;

        if (bMove1.getAction() != bMove2.getAction() || bMove1.player() != bMove2.player())
            return false;

        // 创建防御性副本再比较
        List<Card> cards1 = new ArrayList<>(bMove1.getCards());
        List<Card> cards2 = new ArrayList<>(bMove2.getCards());

        return cards1.equals(cards2);
    }

    private int simulate(BalatroNode node) {
        State<BalatroGame> currentState = node.state();

        // 随机模拟直到终止状态
        while (!currentState.isTerminal()) {
            Move<BalatroGame> randomMove = currentState.chooseMove(currentState.player());
            currentState = currentState.next(randomMove);
        }

        // 返回终止状态的累积分数
        return ((BalatroState) currentState).getScore();
    }

    /**
     * Backpropagation phase: update statistics of all nodes in the path
     */
    private void backpropagate(List<BalatroNode> path, int result) {
        for (BalatroNode node : path) {
            node.updateStats(result);
        }
    }
}
