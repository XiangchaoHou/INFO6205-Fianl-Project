package com.phasmidsoftware.dsaipg.projects.mcts.blackJack;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Move;
import com.phasmidsoftware.dsaipg.projects.mcts.core.Node;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;

import java.util.*;
/**
 * runs Monte Carlo Tree Search.
 * Select best paths, expand new moves, simulate random games, backpropagate results, pick best move to play.
 */
public class BlackjackMCTS {

    private final double explorationParameter = Math.sqrt(2);
    private final int maxIterations;
    private final Random random;
    private final BlackjackNode root;

    public static void main(String[] args) {
        BlackjackGame game = new BlackjackGame();
        playFullGame(game);
    }

    public static void playFullGame(BlackjackGame game) {
        State<BlackjackGame> currentState = game.start();

        System.out.println("Start Blackjack Game!");
        System.out.println(currentState);

        while (!currentState.isTerminal()) {
            System.out.println("\nPlayer Round");

            BlackjackNode rootNode = new BlackjackNode(currentState);
            BlackjackMCTS mcts = new BlackjackMCTS(rootNode, 500);

            Move<BlackjackGame> bestMove = mcts.findBestMove();

            if (bestMove == null) {
                System.out.println("No possible moves!");
                break;
            }

            System.out.println("Move chosen: " + bestMove);

            currentState = currentState.next(bestMove);
            System.out.println(currentState);
        }

        System.out.println("\nGame Over!");
        if (currentState.winner().isPresent()) {
            int winner = currentState.winner().get();
            System.out.println("Winner: " + (winner == 0 ? "Player" : "Dealer"));
        } else {
            System.out.println("Draw!");
        }
    }

    public BlackjackMCTS(BlackjackNode root, int maxIterations) {
        this.root = root;
        this.maxIterations = maxIterations;
        this.random = new Random();
    }

    public Move<BlackjackGame> findBestMove() {
        if (root.isLeaf()) {
            return null;
        }

        for (int i = 0; i < maxIterations; i++) {
            List<BlackjackNode> path = new ArrayList<>();
            BlackjackNode selectedNode = selectWithPath(root, path);

            BlackjackNode expandedNode = selectedNode;
            if (!selectedNode.isLeaf()) {
                expandedNode = expand(selectedNode);
                path.add(expandedNode);
            }

            int simulationResult = simulate(expandedNode);

            backpropagate(path, simulationResult);
        }

        return getMostVisitedChild(root).getMove();
    }

    private BlackjackNode selectWithPath(BlackjackNode node, List<BlackjackNode> path) {
        BlackjackNode currentNode = node;
        path.add(currentNode);

        while (!currentNode.isLeaf() && currentNode.isFullyExpanded()) {
            BlackjackNode bestChild = getBestChild(currentNode, explorationParameter);
            if (bestChild == null) {
                break;
            }
            currentNode = bestChild;
            path.add(currentNode);
        }

        return currentNode;
    }

    private BlackjackNode expand(BlackjackNode node) {
        Collection<Move<BlackjackGame>> possibleMoves = node.state().moves(node.state().player());

        List<Move<BlackjackGame>> untriedMoves = new ArrayList<>();
        for (Move<BlackjackGame> move : possibleMoves) {
            boolean alreadyTried = false;
            for (Node<BlackjackGame> child : node.children()) {
                Move<BlackjackGame> childMove = ((BlackjackNode) child).getMove();
                if (childMove != null && childMove.equals(move)) {
                    alreadyTried = true;
                    break;
                }
            }
            if (!alreadyTried) {
                untriedMoves.add(move);
            }
        }

        if (untriedMoves.isEmpty()) return node;

        Move<BlackjackGame> move = untriedMoves.get(random.nextInt(untriedMoves.size()));
        State<BlackjackGame> newState = node.state().next(move);
        node.addChild(newState, move);
        return new BlackjackNode(newState, move);
    }

    private int simulate(BlackjackNode node) {
        State<BlackjackGame> currentState = node.state();

        while (!currentState.isTerminal()) {
            Move<BlackjackGame> randomMove = currentState.chooseMove(0);
            currentState = currentState.next(randomMove);
        }

        return evaluateTerminalState(currentState);
    }

    private int evaluateTerminalState(State<BlackjackGame> state) {
        if (!state.isTerminal()) {
            throw new IllegalArgumentException("Not a terminal state");
        }

        if (state.winner().isEmpty()) {
            return 1;
        } else {
            int winner = state.winner().get();
            return (winner == 0) ? 2 : 0;
        }
    }

    private void backpropagate(List<BlackjackNode> path, int result) {
        for (BlackjackNode node : path) {
            node.updateStats(result);
        }
    }

    private BlackjackNode getBestChild(BlackjackNode node, double explorationValue) {
        Collection<Node<BlackjackGame>> children = node.children();

        BlackjackNode bestChild = null;
        double bestValue = Double.NEGATIVE_INFINITY;

        for (Node<BlackjackGame> childNode : children) {
            BlackjackNode child = (BlackjackNode) childNode;

            if (child.getPlayouts() == 0) continue;

            double exploitationTerm = (double) child.getWins() / child.getPlayouts();
            double explorationTerm = explorationValue * Math.sqrt(Math.log(node.getPlayouts()) / child.getPlayouts());
            double uctValue = exploitationTerm + explorationTerm;

            if (uctValue > bestValue) {
                bestValue = uctValue;
                bestChild = child;
            }
        }

        return bestChild;
    }

    private BlackjackNode getMostVisitedChild(BlackjackNode node) {
        Collection<Node<BlackjackGame>> children = node.children();

        BlackjackNode mostVisitedChild = null;
        int mostVisits = -1;

        for (Node<BlackjackGame> childNode : children) {
            BlackjackNode child = (BlackjackNode) childNode;
            if (child.getPlayouts() > mostVisits) {
                mostVisits = child.getPlayouts();
                mostVisitedChild = child;
            }
        }

        return mostVisitedChild;
    }
}
