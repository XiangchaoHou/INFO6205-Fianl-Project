package com.phasmidsoftware.dsaipg.projects.mcts.blackJack;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Handles card animations for the Blackjack game
 * Includes animations for dealing cards, flipping cards, and collecting cards
 */
public class BlackjackCardAnimations {

    // Animation durations
    private static final int DEAL_DURATION_MS = 300;
    private static final int FLIP_DURATION_MS = 400;
    private static final int COLLECT_DURATION_MS = 500;

    // Source positions for dealing animations
    private double deckSourceX = 400;
    private double deckSourceY = 300;

    /**
     * Creates a stylish card representation
     */
    public static VBox createCardView(int cardValue) {
        // Create a stylish card representation
        VBox cardView = new VBox(5);
        cardView.setPrefSize(80, 120);
        cardView.setMinSize(80, 120);
        cardView.setAlignment(Pos.CENTER);
        cardView.setPadding(new Insets(5));
        cardView.setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, new CornerRadii(8), BorderWidths.DEFAULT)));
        cardView.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(8), Insets.EMPTY)));

        // Card value
        String displayValue;
        if (cardValue == 1) {
            displayValue = "A";
        } else if (cardValue == 11) {
            displayValue = "J";
        } else if (cardValue == 12) {
            displayValue = "Q";
        } else if (cardValue == 13) {
            displayValue = "K";
        } else {
            displayValue = String.valueOf(cardValue);
        }

        // Top-left value
        Text topValue = new Text(displayValue);
        topValue.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        StackPane topPane = new StackPane(topValue);
        StackPane.setAlignment(topValue, Pos.TOP_LEFT);

        // Suit (we'll use a random but consistent suit based on card value)
        String[] suits = {"♠", "♥", "♦", "♣"};
        String suit = suits[(cardValue * 17) % suits.length]; // Pseudo-random but consistent suit

        // Set color based on suit
        Color suitColor = (suit.equals("♥") || suit.equals("♦")) ? Color.RED : Color.BLACK;
        topValue.setFill(suitColor);

        // Center suit symbol
        Text centerSuit = new Text(suit);
        centerSuit.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        centerSuit.setFill(suitColor);

        // Bottom-right value (inverted)
        Text bottomValue = new Text(displayValue);
        bottomValue.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        bottomValue.setFill(suitColor);
        bottomValue.setRotate(180);
        StackPane bottomPane = new StackPane(bottomValue);
        StackPane.setAlignment(bottomValue, Pos.BOTTOM_RIGHT);

        // Add elements to card
        cardView.getChildren().addAll(topPane, centerSuit, bottomPane);

        return cardView;
    }

    /**
     * Creates a card back view (for hidden cards)
     */
    public static VBox createCardBackView() {
        // Create a stylish card back
        VBox cardBack = new VBox();
        cardBack.setPrefSize(80, 120);
        cardBack.setMinSize(80, 120);
        cardBack.setAlignment(Pos.CENTER);
        cardBack.setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, new CornerRadii(8), BorderWidths.DEFAULT)));

        // Use a Blue gradient background
        cardBack.setBackground(new Background(new BackgroundFill(
                Color.valueOf("#1a3c6e"), new CornerRadii(8), Insets.EMPTY)));

        // Create a pattern
        GridPane pattern = new GridPane();
        pattern.setAlignment(Pos.CENTER);
        pattern.setHgap(5);
        pattern.setVgap(5);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 2; j++) {
                Text symbol = new Text("♠♣");
                symbol.setFont(Font.font("Arial", 14));
                symbol.setFill(Color.valueOf("#d4af37")); // Gold color
                pattern.add(symbol, j, i);
            }
        }

        cardBack.getChildren().add(pattern);
        return cardBack;
    }

    /**
     * Sets the deck position for animations
     */
    public void setDeckPosition(double x, double y) {
        this.deckSourceX = x;
        this.deckSourceY = y;
    }

    /**
     * Animates dealing a card from the deck to a target container
     * @param cardView The card to animate
     * @param targetContainer The container to add the card to
     * @param onFinished Action to perform when animation completes
     * @return A CompletableFuture that completes when the animation finishes
     */
    public CompletableFuture<Void> animateDealCard(Node cardView, Pane targetContainer, Runnable onFinished) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        // Get target position (center of the targetContainer)
        double targetX = targetContainer.getLayoutX() + targetContainer.getWidth() / 2;
        double targetY = targetContainer.getLayoutY() + targetContainer.getHeight() / 2;

        // Configure initial position and scale
        cardView.setScaleX(0.5);
        cardView.setScaleY(0.5);
        cardView.setOpacity(0.9);
        cardView.setTranslateX(deckSourceX - targetX);
        cardView.setTranslateY(deckSourceY - targetY);

        // Add to target container
        targetContainer.getChildren().add(cardView);

        // Create animation components
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(DEAL_DURATION_MS), cardView);
        translateTransition.setToX(0);
        translateTransition.setToY(0);
        translateTransition.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(DEAL_DURATION_MS), cardView);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);
        scaleTransition.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition fadeTransition = new FadeTransition(Duration.millis(DEAL_DURATION_MS), cardView);
        fadeTransition.setToValue(1.0);

        // Combine animations
        ParallelTransition transition = new ParallelTransition(
                translateTransition, scaleTransition, fadeTransition
        );

        // Set up completion callback
        transition.setOnFinished(event -> {
            if (onFinished != null) {
                onFinished.run();
            }
            future.complete(null);
        });

        // Start the animation
        transition.play();

        return future;
    }

    /**
     * Animates dealing multiple cards in sequence
     * @param cards List of card values to be dealt
     * @param targetContainer Container to add cards to
     * @param createCardFunc Function to create a card view from a value
     * @param onAllFinished Action to perform when all animations complete
     * @return A CompletableFuture that completes when all animations finish
     */
    public CompletableFuture<Void> animateDealMultipleCards(List<Integer> cards,
                                                            HBox targetContainer,
                                                            java.util.function.Function<Integer, Node> createCardFunc,
                                                            Runnable onAllFinished) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        // Clear container first
        targetContainer.getChildren().clear();

        // If no cards to deal, complete immediately
        if (cards.isEmpty()) {
            if (onAllFinished != null) {
                onAllFinished.run();
            }
            future.complete(null);
            return future;
        }

        // Deal cards sequentially
        dealNextCard(cards, 0, targetContainer, createCardFunc, () -> {
            if (onAllFinished != null) {
                onAllFinished.run();
            }
            future.complete(null);
        });

        return future;
    }

    /**
     * Helper method to deal cards recursively
     */
    private void dealNextCard(List<Integer> cards, int index, HBox targetContainer,
                              java.util.function.Function<Integer, Node> createCardFunc,
                              Runnable onAllFinished) {
        if (index >= cards.size()) {
            if (onAllFinished != null) {
                onAllFinished.run();
            }
            return;
        }

        Node cardView = createCardFunc.apply(cards.get(index));
        animateDealCard(cardView, targetContainer, () -> {
            // Deal next card after delay
            Timeline delay = new Timeline(new KeyFrame(Duration.millis(150), e -> {
                dealNextCard(cards, index + 1, targetContainer, createCardFunc, onAllFinished);
            }));
            delay.play();
        });
    }

    /**
     * Animates flipping a card from back to face-up
     * @param container The container holding the card
     * @param cardIndex Index of the card to flip
     * @param cardValue Value of the card to reveal
     * @param onFinished Action to perform when animation completes
     * @return A CompletableFuture that completes when the animation finishes
     */
    public CompletableFuture<Void> animateFlipCard(HBox container, int cardIndex, int cardValue, Runnable onFinished) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        if (container.getChildren().size() <= cardIndex) {
            future.completeExceptionally(new IndexOutOfBoundsException("Card index out of bounds"));
            return future;
        }

        Node cardBack = container.getChildren().get(cardIndex);
        VBox cardFront = createCardView(cardValue);

        // Set rotation axis for 3D effect
        cardBack.setRotationAxis(new Point3D(0, 1, 0));

        // Create the first half of flip animation
        RotateTransition rotateOut = new RotateTransition(Duration.millis(FLIP_DURATION_MS / 2), cardBack);
        rotateOut.setFromAngle(0);
        rotateOut.setToAngle(90);
        rotateOut.setInterpolator(Interpolator.EASE_BOTH);

        rotateOut.setOnFinished(event -> {
            // Replace card back with card front at the halfway point
            container.getChildren().set(cardIndex, cardFront);

            // Set up the front card to continue the rotation
            cardFront.setRotationAxis(new Point3D(0, 1, 0));
            cardFront.setRotate(90);

            // Create the second half of flip animation
            RotateTransition rotateIn = new RotateTransition(Duration.millis(FLIP_DURATION_MS / 2), cardFront);
            rotateIn.setFromAngle(90);
            rotateIn.setToAngle(0);
            rotateIn.setInterpolator(Interpolator.EASE_BOTH);

            rotateIn.setOnFinished(evt -> {
                if (onFinished != null) {
                    onFinished.run();
                }
                future.complete(null);
            });

            rotateIn.play();
        });

        rotateOut.play();

        return future;
    }

    /**
     * Animates collecting cards (moving them off screen)
     * @param container Container with cards to collect
     * @param onFinished Action to perform when animation completes
     * @return A CompletableFuture that completes when the animation finishes
     */
    public CompletableFuture<Void> animateCollectCards(Pane container, Runnable onFinished) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        if (container.getChildren().isEmpty()) {
            if (onFinished != null) {
                onFinished.run();
            }
            future.complete(null);
            return future;
        }

        // Create a parallel animation for all cards
        ParallelTransition parallelTransition = new ParallelTransition();

        for (Node card : container.getChildren()) {
            // Random direction for the card to move off-screen
            double angle = Math.random() * 360;
            double distance = 500;
            double targetX = Math.cos(Math.toRadians(angle)) * distance;
            double targetY = Math.sin(Math.toRadians(angle)) * distance;

            // Create "bounce" animation
            TranslateTransition bounceUp = new TranslateTransition(Duration.millis(100), card);
            bounceUp.setByY(-20);
            bounceUp.setCycleCount(2);
            bounceUp.setAutoReverse(true);

            // Create move off screen animation
            TranslateTransition moveAway = new TranslateTransition(Duration.millis(COLLECT_DURATION_MS), card);
            moveAway.setToX(targetX);
            moveAway.setToY(targetY);
            moveAway.setInterpolator(Interpolator.EASE_IN);

            // Create rotation animation
            RotateTransition rotate = new RotateTransition(Duration.millis(COLLECT_DURATION_MS), card);
            rotate.setByAngle(angle > 180 ? 360 : -360);

            // Create fade animation
            FadeTransition fade = new FadeTransition(Duration.millis(COLLECT_DURATION_MS), card);
            fade.setToValue(0);

            // Combine move, rotate and fade
            ParallelTransition cardFlyAway = new ParallelTransition(moveAway, rotate, fade);

            // Combine bounce and fly away
            SequentialTransition cardAnimation = new SequentialTransition(bounceUp, cardFlyAway);

            parallelTransition.getChildren().add(cardAnimation);
        }

        parallelTransition.setOnFinished(event -> {
            // Clear the container after animation
            container.getChildren().clear();

            if (onFinished != null) {
                onFinished.run();
            }
            future.complete(null);
        });

        parallelTransition.play();

        return future;
    }

    /**
     * Creates a "win" animation with celebratory effects
     * @param container Container to animate within
     */
    public void animateWin(Pane container) {
        // Create celebratory particles
        for (int i = 0; i < 30; i++) {
            createFireworkParticle(container);
        }
    }

    /**
     * Creates an individual firework particle for the win animation
     */
    private void createFireworkParticle(Pane container) {
        // Create a small colored circle
        Region particle = new Region();
        particle.setPrefSize(8, 8);
        particle.setBackground(new Background(new BackgroundFill(
                Color.color(Math.random(), Math.random(), Math.random()),
                new CornerRadii(4), Insets.EMPTY)));

        // Random position near center
        double centerX = container.getWidth() / 2;
        double centerY = container.getHeight() / 2;

        // Add to container
        container.getChildren().add(particle);
        particle.setManaged(false);
        particle.setLayoutX(centerX);
        particle.setLayoutY(centerY);

        // Random direction
        double angle = Math.random() * 360;
        double distance = 100 + Math.random() * 150;
        double targetX = Math.cos(Math.toRadians(angle)) * distance;
        double targetY = Math.sin(Math.toRadians(angle)) * distance;

        // Movement animation
        TranslateTransition moveTransition = new TranslateTransition(
                Duration.millis(1000 + Math.random() * 1000), particle);
        moveTransition.setToX(targetX);
        moveTransition.setToY(targetY);
        moveTransition.setInterpolator(Interpolator.SPLINE(0.1, 0.8, 0.3, 1.0));

        // Fade animation
        FadeTransition fadeTransition = new FadeTransition(
                Duration.millis(1000 + Math.random() * 1000), particle);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);
        fadeTransition.setDelay(Duration.millis(500 + Math.random() * 500));

        // Combine animations
        ParallelTransition animation = new ParallelTransition(moveTransition, fadeTransition);

        animation.setOnFinished(e -> container.getChildren().remove(particle));
        animation.play();
    }
}