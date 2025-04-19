package com.phasmidsoftware.dsaipg.projects.mcts.miniBalatro;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Game;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;

import java.util.*;

public class BalatroGame implements Game<BalatroGame> {
    @Override
    public State<BalatroGame> start() {
        List<Card> deckList = new ArrayList<>();
        for (Card.Suit suit : Card.Suit.values()) {
            for (int rank = 1; rank <= 13; rank++) {
                deckList.add(new Card(rank, suit));
            }
        }
        java.util.Collections.shuffle(deckList);
        Deque<Card> deck = new ArrayDeque<>(deckList);

        List<Card> playerHand = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            playerHand.add(deck.pop());
        }
        List<Card> table = new ArrayList<>();
        return new BalatroState(this, playerHand, table, deck, 5, 3, new Random(), 0);
    }

    @Override
    public int opener() {
        return 0;
    }
}
