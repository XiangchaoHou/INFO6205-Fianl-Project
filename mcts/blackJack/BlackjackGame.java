package com.phasmidsoftware.dsaipg.projects.mcts.blackJack;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Game;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;

import java.util.*;
public class BlackjackGame implements Game<BlackjackGame> {
    @Override
    public State<BlackjackGame> start() {
        List<Integer> deckList = new ArrayList<>();
        for (int i = 1; i <= 13; i++) {
            for (int j = 0; j < 4; j++) {
                deckList.add(i);
            }
        }
        Collections.shuffle(deckList);
        Deque<Integer> deck = new ArrayDeque<>(deckList);

        List<Integer> playerHand = new ArrayList<>();
        List<Integer> dealerHand = new ArrayList<>();

        playerHand.add(deck.pop());
        playerHand.add(deck.pop());
        dealerHand.add(deck.pop());
        dealerHand.add(deck.pop());

        return new BlackjackState(this, playerHand, dealerHand, deck, 0, new Random());
    }

    @Override
    public int opener() {
        return 0;
    }
}