package com.phasmidsoftware.dsaipg.projects.mcts.miniBalatro;

/**
 * Represents a standard playing card with rank and suit
 */
public class Card implements Comparable<Card> {

    public enum Suit {
        CLUBS, DIAMONDS, HEARTS, SPADES;

        @Override
        public String toString() {
            switch (this) {
                case CLUBS: return "♣";
                case DIAMONDS: return "♦";
                case HEARTS: return "♥";
                case SPADES: return "♠";
                default: return "";
            }
        }
    }

    private final int rank;  // 1=Ace, 11=Jack, 12=Queen, 13=King
    private final Suit suit;

    public Card(int rank, Suit suit) {
        if (rank < 1 || rank > 13) {
            throw new IllegalArgumentException("Rank must be between 1 and 13");
        }
        this.rank = rank;
        this.suit = suit;
    }

    public int getRank() {
        return rank;
    }

    public Suit getSuit() {
        return suit;
    }

    /**
     * Returns the face value of the card (A, 2-10, J, Q, K)
     */
    public String getFaceValue() {
        switch (rank) {
            case 1: return "A";
            case 11: return "J";
            case 12: return "Q";
            case 13: return "K";
            default: return String.valueOf(rank);
        }
    }

    @Override
    public String toString() {
        return getFaceValue() + suit.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Card other = (Card) obj;
        return rank == other.rank && suit == other.suit;
    }

    @Override
    public int hashCode() {
        return 31 * rank + suit.hashCode();
    }

    @Override
    public int compareTo(Card other) {
        // Primary sort by rank, secondary by suit
        int rankCompare = Integer.compare(this.rank, other.rank);
        if (rankCompare != 0) {
            return rankCompare;
        }
        return this.suit.compareTo(other.suit);
    }
}
