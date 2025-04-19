package com.phasmidsoftware.dsaipg.projects.mcts.miniBalatro;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Move;
import com.phasmidsoftware.dsaipg.projects.mcts.core.Node;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;

import java.util.*;

public class BalatroMCTS {
    private final double explorationParameter = Math.sqrt(2);
    private final int maxIterations;
    private final Random random;
    private final BalatroNode root;

    public static void main(String[] args) {
        BalatroGame game = new BalatroGame();
//        benchmarkBalatro();
        playGame(game);
    }

    public static void playGame(BalatroGame game) {
        long startTime = System.currentTimeMillis();  
        State<BalatroGame> currentState = game.start();

        System.out.println("Starting Balatro Decision Game!");
        System.out.println(currentState);

        while (!currentState.isTerminal()) {
            System.out.println("\n=== New Round ===");
            BalatroNode rootNode = new BalatroNode(currentState);
            BalatroMCTS mcts = new BalatroMCTS(rootNode, 500);

            Move<BalatroGame> bestMove = mcts.findBestMove();
            if (bestMove == null) {
                System.out.println("No possible moves!");
                break;
            }

            System.out.println("MCTS chose move: " + bestMove);
            int previousScore = ((BalatroState)currentState).getScore();
            currentState = currentState.next(bestMove);
            int newScore = ((BalatroState)currentState).getScore();
            System.out.println("Move score: +" + (newScore - previousScore) + " points");
            System.out.println("Total score: " + newScore);
            System.out.println(currentState);
        }

        System.out.println("\nGame Over!");
        System.out.println("Final Score: " + ((BalatroState)currentState).getScore());

        analyzeResult((BalatroState)currentState);

        long endTime = System.currentTimeMillis();    
        System.out.println("Time taken: " + (endTime - startTime) + " ms");
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

    public Move<BalatroGame> findBestMove() {
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

        List<Move<BalatroGame>> untriedMoves = new ArrayList<>();

        for (Move<BalatroGame> move : possibleMoves) {
            if (move == null) continue; 

            boolean alreadyTried = false;

            List<Node<BalatroGame>> childrenSnapshot = new ArrayList<>(node.children());

            for (Node<BalatroGame> childNode : childrenSnapshot) {
                BalatroNode child = (BalatroNode) childNode;
                Move<BalatroGame> childMove = child.getMove();

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

        Move<BalatroGame> move = untriedMoves.get(random.nextInt(untriedMoves.size()));
        State<BalatroGame> newState = node.state().next(move);

        node.addChild(newState, move);
        return new BalatroNode(newState, move);
    }

    private boolean moveEquals(Move<BalatroGame> move1, Move<BalatroGame> move2) {
        if (move1 == move2) return true;
        if (move1 == null || move2 == null) return false;

        if (!(move1 instanceof BalatroMove) || !(move2 instanceof BalatroMove)) {
            return false;
        }

        BalatroMove bMove1 = (BalatroMove) move1;
        BalatroMove bMove2 = (BalatroMove) move2;

        if (bMove1.getAction() != bMove2.getAction() || bMove1.player() != bMove2.player())
            return false;

        List<Card> cards1 = new ArrayList<>(bMove1.getCards());
        List<Card> cards2 = new ArrayList<>(bMove2.getCards());

        return cards1.equals(cards2);
    }

    private int simulate(BalatroNode node) {
        State<BalatroGame> currentState = node.state();

        while (!currentState.isTerminal()) {
            Move<BalatroGame> randomMove = currentState.chooseMove(currentState.player());
            currentState = currentState.next(randomMove);
        }

        return ((BalatroState) currentState).getScore();
    }

    private void backpropagate(List<BalatroNode> path, int result) {
        for (BalatroNode node : path) {
            node.updateStats(result);
        }
    }

    public static void benchmarkBalatro() {
        int[] iterationLimits = {50, 100, 200, 400};
        int numRuns = 20;

        for (int iter : iterationLimits) {
            long totalTime = 0;
            int totalScore = 0;

            for (int i = 0; i < numRuns; i++) {
                BalatroGame game = new BalatroGame();
                State<BalatroGame> state = game.start();
                long start = System.currentTimeMillis();

                while (!state.isTerminal()) {
                    BalatroNode rootNode = new BalatroNode(state);
                    BalatroMCTS mcts = new BalatroMCTS(rootNode, iter);
                    Move<BalatroGame> move = mcts.findBestMove();
                    if (move == null) break;
                    state = state.next(move);
                }

                long end = System.currentTimeMillis();
                totalTime += (end - start);

                int finalScore = ((BalatroState) state).getScore();
                totalScore += finalScore;
            }

            double avgTime = totalTime / (double) numRuns;
            double avgScore = totalScore / (double) numRuns;

            System.out.printf("Iterations: %d | Avg Time: %.2f ms | Avg Score: %.2f\n",
                    iter, avgTime, avgScore);
        }
    }
}