/*
 * Copyright (c) 2024. Robin Hillyard
 */

package com.phasmidsoftware.dsaipg.projects.mcts.tictactoe;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Move;
import com.phasmidsoftware.dsaipg.projects.mcts.core.Node;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;

import java.util.*;

public class MCTS {
    private final double explorationParameter = Math.sqrt(2);
    private final int maxIterations;
    private final Random random;
    private final Node<TicTacToe> root;

    public static void main(String[] args) {
        TicTacToe game = new TicTacToe();
        playFullGame(game);
    }

    public static void playFullGame(TicTacToe game) {
        long startTime = System.currentTimeMillis();

        State<TicTacToe> currentState = game.start();
        int currentPlayer = game.opener();

        System.out.println("Start!");
        System.out.println("Board:");
        System.out.println(currentState);

        while (!currentState.isTerminal()) {
            System.out.println("\nPlayer " + (currentPlayer == TicTacToe.X ? "X" : "O") + " Round");

            TicTacToeNode rootNode = new TicTacToeNode(currentState);
            MCTS mcts = new MCTS(rootNode, 1000);

            Move<TicTacToe> bestMove = mcts.findBestMove();
            currentState = currentState.next(bestMove);

            System.out.println("Move: " + bestMove);
            System.out.println("Board:");
            System.out.println(currentState);

            currentPlayer = 1 - currentPlayer;
        }

        long endTime = System.currentTimeMillis(); 
        System.out.println("\nGame Over!");
        System.out.println("Time taken: " + (endTime - startTime) + " ms");

        if (currentState.winner().isPresent()) {
            int winner = currentState.winner().get();
            System.out.println("Winner: Player " + (winner == TicTacToe.X ? "X" : "O"));
        } else {
            System.out.println("Draw!");
        }
    }

    public MCTS(Node<TicTacToe> root, int maxIterations) {
        this.root = root;
        this.maxIterations = maxIterations;
        this.random = new Random();
    }

    public MCTS(Node<TicTacToe> root) {
        this(root, 1000);
    }

    public Move<TicTacToe> findBestMove() {
        if (root.isLeaf()) {
            return null;
        }

        for (int i = 0; i < maxIterations; i++) {
            List<TicTacToeNode> path = new ArrayList<>();
            TicTacToeNode selectedNode = selectWithPath(root, path);

            TicTacToeNode expandedNode = selectedNode;
            if (!selectedNode.isLeaf() && !selectedNode.isFullyExpanded()) {
                expandedNode = expand(selectedNode);
                path.add(expandedNode);
            }

            int simulationResult = simulate(expandedNode);

            backpropagate(path, simulationResult);
        }

        return getMostVisitedChild(root).getMove();
    }

    private TicTacToeNode selectWithPath(Node<TicTacToe> node, List<TicTacToeNode> path) {
        TicTacToeNode currentNode = (TicTacToeNode) node;
        path.add(currentNode);

        if (currentNode.isLeaf() || !currentNode.isFullyExpanded()) {
            return currentNode;
        }

        TicTacToeNode bestChild = getBestChild(currentNode, explorationParameter);
        return selectWithPath(bestChild, path);
    }

    private TicTacToeNode expand(TicTacToeNode node) {
        Collection<Move<TicTacToe>> possibleMoves = node.state().moves(node.state().player());
        List<Move<TicTacToe>> unexpandedMoves = new ArrayList<>();

        for (Move<TicTacToe> move : possibleMoves) {
            if (!node.hasChildForMove(move)) {
                unexpandedMoves.add(move);
            }
        }

        if (unexpandedMoves.isEmpty()) {
            return node;
        }

        Move<TicTacToe> move = unexpandedMoves.get(random.nextInt(unexpandedMoves.size()));
        State<TicTacToe> newState = node.state().next(move);
        TicTacToeNode newNode = new TicTacToeNode(newState, move);
        node.addChildNode(newNode);
        return newNode;
    }

    private int simulate(TicTacToeNode node) {
        if (node.isLeaf()) {
            return evaluateTerminalState(node.state());
        }

        State<TicTacToe> currentState = node.state();
        int currentPlayer = currentState.player();

        while (!currentState.isTerminal()) {
            Move<TicTacToe> randomMove = currentState.chooseMove(currentPlayer);
            currentState = currentState.next(randomMove);
            currentPlayer = 1 - currentPlayer;
        }

        return evaluateTerminalState(currentState);
    }

    private int evaluateTerminalState(State<TicTacToe> state) {
        if (!state.isTerminal()) {
            throw new IllegalArgumentException("Not a terminal state");
        }

        if (state.winner().isEmpty()) {
            return 1; 
        } else {
            int winner = state.winner().get();
            int rootPlayer = root.state().player();
            return (winner == rootPlayer) ? 2 : 0;
        }
    }

    private void backpropagate(List<TicTacToeNode> path, int result) {
        for (int i = path.size() - 1; i >= 0; i--) {
            TicTacToeNode node = path.get(i);
            node.updateStats(result);
        }
    }

    private TicTacToeNode getBestChild(Node<TicTacToe> node, double explorationValue) {
        TicTacToeNode parentNode = (TicTacToeNode) node;
        Collection<TicTacToeNode> children = parentNode.getChildren();

        TicTacToeNode bestChild = null;
        double bestValue = Double.NEGATIVE_INFINITY;

        for (TicTacToeNode child : children) {
            if (child.getPlayouts() == 0) {
                continue;
            }
            double exploitationTerm = (double) child.getWins() / child.getPlayouts();
            double explorationTerm = explorationValue * Math.sqrt(Math.log(parentNode.getPlayouts()) / child.getPlayouts());
            double uctValue = exploitationTerm + explorationTerm;
            if (uctValue > bestValue) {
                bestValue = uctValue;
                bestChild = child;
            }
        }

        if (bestChild == null && !children.isEmpty()) {
            bestChild = children.iterator().next();
        }

        return bestChild;
    }

    private TicTacToeNode getMostVisitedChild(Node<TicTacToe> node) {
        TicTacToeNode parentNode = (TicTacToeNode) node;
        Collection<TicTacToeNode> children = parentNode.getChildren();

        if (children.isEmpty()) {
            throw new IllegalStateException("No child");
        }

        TicTacToeNode mostVisitedChild = null;
        int mostVisits = -1;

        for (TicTacToeNode child : children) {
            if (child.getPlayouts() > mostVisits) {
                mostVisits = child.getPlayouts();
                mostVisitedChild = child;
            }
        }

        return mostVisitedChild;
    }
}
