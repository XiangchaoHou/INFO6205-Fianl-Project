package com.phasmidsoftware.dsaipg.projects.mcts.blackJack;

import com.phasmidsoftware.dsaipg.projects.mcts.core.State;
import java.util.*;

public class BlackjackState implements State<BlackjackGame> {
    final BlackjackGame game;
    public final List<Integer> playerHand;
    public final List<Integer> dealerHand;
    final Deque<Integer> deck;
    public final int player;
    final Random random;

    public BlackjackState(BlackjackGame game, List<Integer> playerHand, List<Integer> dealerHand, Deque<Integer> deck, int player, Random random) {
        this.game = game;
        this.playerHand = playerHand;
        this.dealerHand = dealerHand;
        this.deck = deck;
        this.player = player;
        this.random = random;
    }

    @Override
    public boolean isTerminal() {
        return (handValue(playerHand) > 21) || (handValue(dealerHand) > 21) || (player == 1 && !dealerShouldHit());
    }

    @Override
    public int player() {
        return player;
    }

    @Override
    public BlackjackGame game() {
        return game;
    }

    @Override
    public Random random() {
        return random;
    }

    @Override
    public Collection<com.phasmidsoftware.dsaipg.projects.mcts.core.Move<BlackjackGame>> moves(int player) {
        List<com.phasmidsoftware.dsaipg.projects.mcts.core.Move<BlackjackGame>> moves = new ArrayList<>();
        if (this.player == 0) { // Only allow moves if it's player turn
            if (handValue(playerHand) < 21) {
                moves.add(new BlackjackMove(BlackjackMove.Action.HIT, 0));
            }
            moves.add(new BlackjackMove(BlackjackMove.Action.STAND, 0));
        }
        return moves;
    }

    @Override
    public State<BlackjackGame> next(com.phasmidsoftware.dsaipg.projects.mcts.core.Move<BlackjackGame> move) {
        BlackjackMove blackjackMove = (BlackjackMove) move;
        List<Integer> newPlayerHand = new ArrayList<>(playerHand);
        List<Integer> newDealerHand = new ArrayList<>(dealerHand);
        Deque<Integer> newDeck = new ArrayDeque<>(deck);
        int newPlayer = player;

        if (player == 0) {
            if (blackjackMove.getAction() == BlackjackMove.Action.HIT && !newDeck.isEmpty()) {
                newPlayerHand.add(newDeck.pop());
            } else if (blackjackMove.getAction() == BlackjackMove.Action.STAND) {
                newPlayer = 1; 
                while (dealerShouldHit(newDealerHand) && !newDeck.isEmpty()) {
                    newDealerHand.add(newDeck.pop());
                }
            }
        }

        return new BlackjackState(game, newPlayerHand, newDealerHand, newDeck, newPlayer, random);
    }

    @Override
    public Optional<Integer> winner() {
        if (!isTerminal()) return Optional.empty();

        int playerTotal = handValue(playerHand);
        int dealerTotal = handValue(dealerHand);

        if (playerTotal > 21) return Optional.of(1); 
        if (dealerTotal > 21) return Optional.of(0); 

        return playerTotal > dealerTotal ? Optional.of(0) : (playerTotal < dealerTotal ? Optional.of(1) : Optional.empty());
    }

    private int handValue(List<Integer> hand) {
        int total = 0;
        int aceCount = 0;
        for (int card : hand) {
            int value = Math.min(card, 10);
            total += value;
            if (card == 1) aceCount++;
        }
        while (aceCount > 0 && total + 10 <= 21) {
            total += 10;
            aceCount--;
        }
        return total;
    }

    private boolean dealerShouldHit() {
        return dealerShouldHit(dealerHand);
    }

    private boolean dealerShouldHit(List<Integer> dealerHand) {
        return handValue(dealerHand) < 17;
    }

    @Override
    public String toString() {
        int playerTotal = handValue(playerHand);
        int dealerTotal = handValue(dealerHand);
        return "Player Hand: " + playerHand + " (Total: " + playerTotal + "), Dealer Hand: " + dealerHand + " (Total: " + dealerTotal + ")";
    }
}