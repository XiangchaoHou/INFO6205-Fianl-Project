package com.phasmidsoftware.dsaipg.projects.mcts.miniBalatro;

import com.phasmidsoftware.dsaipg.projects.mcts.blackJack.BlackjackGame;
import com.phasmidsoftware.dsaipg.projects.mcts.core.Move;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;
import java.util.*;
import java.util.stream.Collectors;

// 表示游戏状态
public class BalatroState implements State<BalatroGame> {
    // 游戏对象
    // 玩家手牌
    // 桌面上的牌
    // 牌库
    // 剩余出牌次数
    // 剩余丢弃次数
    // 随机数生成器

    private static final int HIGH_CARD_SCORE = 1;
    private static final int PAIR_SCORE = 5;
    private static final int TWO_PAIR_SCORE = 20;
    private static final int THREE_OF_A_KIND_SCORE = 35;
    private static final int STRAIGHT_SCORE = 55;
    private static final int FLUSH_SCORE = 85;
    private static final int FULL_HOUSE_SCORE = 150;
    private static final int FOUR_OF_A_KIND_SCORE = 300;
    private static final int STRAIGHT_FLUSH_SCORE = 500;
    private static final int ROYAL_FLUSH_SCORE = 800;

    private static final double ALL_SUITS_BONUS = 1.2;

    final BalatroGame game;
    final List<Card> hand;
    final List<Card> table;
    final Deque<Card> deck;
    final int remainingPlays;
    final int remainingDiscards;
    final Random random;
    final int player;
    final int accumulatedScore;

    // 构造函数
    public BalatroState(BalatroGame game, List<Card> hand, List<Card> table, Deque<Card> deck, int remainingPlays, int remainingDiscards, Random random, int accumulatedScore) {
        this.game = game;
        this.hand = hand;
        this.table = table;
        this.deck = deck;
        this.remainingPlays = remainingPlays;
        this.remainingDiscards = remainingDiscards;
        this.random = random;
        this.player = 0;
        this.accumulatedScore = accumulatedScore;
    }

    @Override
    public int player() {
        return player;
    }

    @Override
    public Optional<Integer> winner() {
        return null;
    }

    @Override
    public BalatroGame game() {
        return game;
    }

    @Override
    public Random random() {
        return random;
    }

    @Override
    public boolean isTerminal() {
        return remainingPlays == 0;
    }

    @Override
    public Collection<Move<BalatroGame>> moves(int player) {
        List<Move<BalatroGame>> possibleMoves = new ArrayList<>();

        // Add play moves if we have plays remaining
        if (remainingPlays > 0) {
            // Generate all possible combinations of cards to play
            for (int i = 1; i <= Math.min(5, hand.size()); i++) {
                generateCombinations(hand, i, new ArrayList<>(), 0, possibleMoves, BalatroMove.Action.PLAY);
            }
        }

        // Add discard moves if we have discards remaining
        if (remainingDiscards > 0) {
            // Generate all possible combinations of cards to discard
            for (int i = 1; i <= Math.min(3, hand.size()); i++) {
                generateCombinations(hand, i, new ArrayList<>(), 0, possibleMoves, BalatroMove.Action.DISCARD);
            }
        }

        return possibleMoves;
    }

    private void generateCombinations(List<Card> cards, int k, List<Card> current, int start, List<Move<BalatroGame>> moves, BalatroMove.Action action) {
        if (current.size() == k) {
            moves.add(new BalatroMove(action, new ArrayList<>(current), player));
            return;
        }

        for (int i = start; i < cards.size(); i++) {
            current.add(cards.get(i));
            generateCombinations(cards, k, current, i + 1, moves, action);
            current.remove(current.size() - 1);
        }
    }

    @Override
    public State<BalatroGame> next(Move<BalatroGame> move) {
        BalatroMove balatroMove = (BalatroMove) move;
        List<Card> cardsList = balatroMove.getCards();
        List<Card> newPlayerHand = new ArrayList<>(hand);
        List<Card> newTableCards = new ArrayList<>(table);
        Deque<Card> newDeck = new ArrayDeque<>(deck);
        int newPlaysRemaining = remainingPlays;
        int newDiscardsRemaining = remainingDiscards;
        int newAccumulatedScore = accumulatedScore; // 初始设为当前累积分数

        if (balatroMove.getAction() == BalatroMove.Action.PLAY) {
            // 计算出牌前的桌面状态

            // 出牌: 从手牌移除，添加到桌面
            for (Card card : cardsList) {
                newPlayerHand.remove(card);
                newTableCards.add(card);
            }
            newPlaysRemaining--;

            // 计算这次出牌所得的分数
            int moveScore = evaluatePlayScore(cardsList);

            // 累加到总分
            newAccumulatedScore += moveScore;

            // 抽新牌
            drawCards(newPlayerHand, cardsList.size(), newDeck);
        } else if (balatroMove.getAction() == BalatroMove.Action.DISCARD) {
            // 丢弃牌不计分
            for (Card card : cardsList) {
                newPlayerHand.remove(card);
            }
            newDiscardsRemaining--;

            // 抽新牌
            drawCards(newPlayerHand, cardsList.size(), newDeck);
        }



        return new BalatroState(
                game,
                newPlayerHand,
                newTableCards,
                newDeck,
                newPlaysRemaining,
                newDiscardsRemaining,
                random,
                newAccumulatedScore
        );
    }

    private void drawCards(List<Card> hand, int count, Deque<Card> deck) {
        for (int i = 0; i < count && !deck.isEmpty(); i++) {
            hand.add(deck.pop());
        }
    }

    public int getScore() {
        return accumulatedScore;
    }

    /**
     * 计算单张牌的分数（根据与桌面牌的组合）
     */
    /**
     * 计算单次出牌的得分
     * @param playedCards 本次出的牌
     * @return 本次出牌所得分数
     */
    /**
     * 计算单次出牌的得分
     * @param playedCards 本次出的牌
     * @return 本次出牌所得分数
     */
    private int evaluatePlayScore(List<Card> playedCards) {
        if (playedCards == null || playedCards.isEmpty()) {
            return 0;
        }

        // 检查集合内是否有null元素
        if (playedCards.contains(null)) {
            // 过滤掉null元素
            playedCards = playedCards.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (playedCards.isEmpty()) {
                return 0;
            }
        }

        int score = 0;

        // 按点数分组
        Map<Integer, List<Card>> rankGroups = playedCards.stream()
                .collect(Collectors.groupingBy(Card::getRank));

        // 首先检查是否是皇家同花顺(最高级别的牌型)
        if (isRoyalFlush(playedCards)) {
            score += ROYAL_FLUSH_SCORE;
            return score; // 直接返回，不需要检查其他牌型
        }

        // 检查是否为同花顺
        if (playedCards.size() >= 5 && isSequential(playedCards) && isSameFlush(playedCards)) {
            score += STRAIGHT_FLUSH_SCORE;
            return score; // 直接返回，不需要检查其他牌型
        }

        // 检查是否为四条
        boolean hasFourOfAKind = rankGroups.values().stream().anyMatch(list -> list.size() == 4);
        if (hasFourOfAKind) {
            score += FOUR_OF_A_KIND_SCORE;
            // 添加其他不属于四条的单牌分数
            int singleCards = playedCards.size() - 4;
            score += singleCards * HIGH_CARD_SCORE;
            return score;
        }

        // 检查是否为葫芦（三条+对子）
        if (rankGroups.size() == 2 && playedCards.size() == 5) {
            boolean hasPair = rankGroups.values().stream().anyMatch(list -> list.size() == 2);
            boolean hasThree = rankGroups.values().stream().anyMatch(list -> list.size() == 3);
            if (hasPair && hasThree) {
                score += FULL_HOUSE_SCORE;
                return score;
            }
        }

        // 检查是否为同花
        if (playedCards.size() > 4 && isSameFlush(playedCards)) {
            score += FLUSH_SCORE;
            // 添加单牌分数
            score += playedCards.size() * HIGH_CARD_SCORE;
            return score;
        }

        // 检查是否为顺子
        if (playedCards.size() >= 5 && isSequential(playedCards)) {
            score += STRAIGHT_SCORE;
            // 添加单牌分数
            score += playedCards.size() * HIGH_CARD_SCORE;
            return score;
        }

        // 检查是否为三条
        boolean hasThreeOfAKind = rankGroups.values().stream().anyMatch(list -> list.size() == 3);
        if (hasThreeOfAKind) {
            score += THREE_OF_A_KIND_SCORE;
            // 添加其他不属于三条的单牌分数
            for (List<Card> cards : rankGroups.values()) {
                if (cards.size() != 3) {
                    score += cards.size() * HIGH_CARD_SCORE;
                }
            }
            return score;
        }

        // 检查是否为两对
        if (rankGroups.size() >= 2) {
            long pairCount = rankGroups.values().stream().filter(list -> list.size() == 2).count();
            if (pairCount >= 2) {
                score += TWO_PAIR_SCORE;
                // 添加不属于两对的单牌分数
                for (List<Card> cards : rankGroups.values()) {
                    if (cards.size() != 2) {
                        score += cards.size() * HIGH_CARD_SCORE;
                    }
                }
                return score;
            }
        }

        // 检查是否为一对
        boolean hasPair = rankGroups.values().stream().anyMatch(list -> list.size() == 2);
        if (hasPair) {
            score += PAIR_SCORE;
            // 添加其他不属于对子的单牌分数
            for (List<Card> cards : rankGroups.values()) {
                if (cards.size() != 2) {
                    score += cards.size() * HIGH_CARD_SCORE;
                }
            }
            return score;
        }

        // 如果没有任何特殊牌型，则每张牌都按高牌计分
        score += playedCards.size() * HIGH_CARD_SCORE;

        return score;
    }

    /**
     * 检查牌是否为顺子
     */
    private boolean isSequential(List<Card> cards) {
        if (cards.size() < 5) return false;

        // 提取点数并排序
        List<Integer> ranks = cards.stream()
                .map(Card::getRank)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // 特殊处理A可以作为最低或最高
        boolean hasAce = ranks.contains(1);
        if (hasAce) {
            // 创建两个列表：一个将A当作1，一个将A当作14
            List<Integer> aceLowRanks = new ArrayList<>(ranks);
            List<Integer> aceHighRanks = new ArrayList<>(ranks);
            aceHighRanks.remove(Integer.valueOf(1)); // 移除A作为1点
            aceHighRanks.add(14); // 添加A作为14点

            return isConsecutive(aceLowRanks) || isConsecutive(aceHighRanks);
        }

        return isConsecutive(ranks);
    }

    /**
     * 检查一组排序好的点数是否连续
     */
    private boolean isConsecutive(List<Integer> sortedRanks) {
        // 移除重复项
        List<Integer> distinct = new ArrayList<>(new HashSet<>(sortedRanks));
        Collections.sort(distinct);

        // 检查序列长度是否至少为3
        if (distinct.size() < 5) return false;

        for (int i = 1; i < distinct.size(); i++) {
            if (distinct.get(i) != distinct.get(i-1) + 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查牌是否为同花
     */
    private boolean isSameFlush(List<Card> cards) {
        if (cards.size() < 3) return false;

        Card.Suit firstSuit = cards.get(0).getSuit();
        return cards.stream().allMatch(c -> c.getSuit() == firstSuit);
    }

    /**
     * 检查是否为皇家同花顺
     */
    private boolean isRoyalFlush(List<Card> cards) {
        if (cards.size() < 5 || !isSameFlush(cards)) return false;

        Set<Integer> ranks = cards.stream()
                .map(Card::getRank)
                .collect(Collectors.toSet());

        return ranks.contains(1) && ranks.contains(10) &&
                ranks.contains(11) && ranks.contains(12) &&
                ranks.contains(13);
    }

    @Override
    public String toString() {
        return "Cards in hand: " + hand +
                "\nRemaining plays: " + remainingPlays +
                "\nRemaining discards: " + remainingDiscards +
                "\nAccumulated score: " + accumulatedScore;
    }
}
