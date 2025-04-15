package com.phasmidsoftware.dsaipg.projects.mcts.miniBalatro;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Game;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;

import java.util.*;

// 定义游戏规则
public class BalatroGame implements Game<BalatroGame> {
    // 初始化游戏
    @Override
    public State<BalatroGame> start() {
        // 创建52张标准扑克牌
        List<Card> deckList = new ArrayList<>();
        for (Card.Suit suit : Card.Suit.values()) {
            for (int rank = 1; rank <= 13; rank++) {
                deckList.add(new Card(rank, suit));
            }
        }
        // 洗牌
        java.util.Collections.shuffle(deckList);
        Deque<Card> deck = new ArrayDeque<>(deckList);

        // 发8张初始手牌
        List<Card> playerHand = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            playerHand.add(deck.pop());
        }
        // 创建初始状态
        // 桌面上没有牌
        List<Card> table = new ArrayList<>();
        return new BalatroState(this, playerHand, table, deck, 5, 3, new Random(), 0);
    }

    @Override
    public int opener() {
        return 0;
    }
}
