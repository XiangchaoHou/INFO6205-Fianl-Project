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

public class BlackjackCardAnimations {

    private static final int DEAL_DURATION_MS = 300;
    private static final int FLIP_DURATION_MS = 400;
    private static final int COLLECT_DURATION_MS = 500;

    private double deckSourceX = 400;
    private double deckSourceY = 300;

    public static VBox createCardView(int cardValue) {
        VBox cardView = new VBox(5);
        cardView.setPrefSize(80, 120);
        cardView.setMinSize(80, 120);
        cardView.setAlignment(Pos.CENTER);
        cardView.setPadding(new Insets(5));
        cardView.setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, new CornerRadii(8), BorderWidths.DEFAULT)));
        cardView.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(8), Insets.EMPTY)));

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

        Text topValue = new Text(displayValue);
        topValue.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        StackPane topPane = new StackPane(topValue);
        StackPane.setAlignment(topValue, Pos.TOP_LEFT);

        String[] suits = {"♠", "♥", "♦", "♣"};
        String suit = suits[(cardValue * 17) % suits.length];

        Color suitColor = (suit.equals("♥") || suit.equals("♦")) ? Color.RED : Color.BLACK;
        topValue.setFill(suitColor);

        Text centerSuit = new Text(suit);
        centerSuit.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        centerSuit.setFill(suitColor);

        Text bottomValue = new Text(displayValue);
        bottomValue.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        bottomValue.setFill(suitColor);
        bottomValue.setRotate(180);
        StackPane bottomPane = new StackPane(bottomValue);
        StackPane.setAlignment(bottomValue, Pos.BOTTOM_RIGHT);

        cardView.getChildren().addAll(topPane, centerSuit, bottomPane);

        return cardView;
    }

    public static VBox createCardBackView() {
        VBox cardBack = new VBox();
        cardBack.setPrefSize(80, 120);
        cardBack.setMinSize(80, 120);
        cardBack.setAlignment(Pos.CENTER);
        cardBack.setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, new CornerRadii(8), BorderWidths.DEFAULT)));

        cardBack.setBackground(new Background(new BackgroundFill(
                Color.valueOf("#1a3c6e"), new CornerRadii(8), Insets.EMPTY)));

        GridPane pattern = new GridPane();
        pattern.setAlignment(Pos.CENTER);
        pattern.setHgap(5);
        pattern.setVgap(5);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 2; j++) {
                Text symbol = new Text("♠♣");
                symbol.setFont(Font.font("Arial", 14));
                symbol.setFill(Color.valueOf("#d4af37"));
                pattern.add(symbol, j, i);
            }
        }

        cardBack.getChildren().add(pattern);
        return cardBack;
    }

    public void setDeckPosition(double x, double y) {
        this.deckSourceX = x;
        this.deckSourceY = y;
    }

    public CompletableFuture<Void> animateDealCard(Node cardView, Pane targetContainer, Runnable onFinished) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        double targetX = targetContainer.getLayoutX() + targetContainer.getWidth() / 2;
        double targetY = targetContainer.getLayoutY() + targetContainer.getHeight() / 2;

        cardView.setScaleX(0.5);
        cardView.setScaleY(0.5);
        cardView.setOpacity(0.9);
        cardView.setTranslateX(deckSourceX - targetX);
        cardView.setTranslateY(deckSourceY - targetY);

        targetContainer.getChildren().add(cardView);

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

        ParallelTransition transition = new ParallelTransition(
                translateTransition, scaleTransition, fadeTransition
        );

        transition.setOnFinished(event -> {
            if (onFinished != null) {
                onFinished.run();
            }
            future.complete(null);
        });

        transition.play();

        return future;
    }

    public CompletableFuture<Void> animateDealMultipleCards(List<Integer> cards,
                                                            HBox targetContainer,
                                                            java.util.function.Function<Integer, Node> createCardFunc,
                                                            Runnable onAllFinished) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        targetContainer.getChildren().clear();

        if (cards.isEmpty()) {
            if (onAllFinished != null) {
                onAllFinished.run();
            }
            future.complete(null);
            return future;
        }

        dealNextCard(cards, 0, targetContainer, createCardFunc, () -> {
            if (onAllFinished != null) {
                onAllFinished.run();
            }
            future.complete(null);
        });

        return future;
    }

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
            Timeline delay = new Timeline(new KeyFrame(Duration.millis(150), e -> {
                dealNextCard(cards, index + 1, targetContainer, createCardFunc, onAllFinished);
            }));
            delay.play();
        });
    }

    public CompletableFuture<Void> animateFlipCard(HBox container, int cardIndex, int cardValue, Runnable onFinished) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        if (container.getChildren().size() <= cardIndex) {
            future.completeExceptionally(new IndexOutOfBoundsException("Card index out of bounds"));
            return future;
        }

        Node cardBack = container.getChildren().get(cardIndex);
        VBox cardFront = createCardView(cardValue);

        cardBack.setRotationAxis(new Point3D(0, 1, 0));

        RotateTransition rotateOut = new RotateTransition(Duration.millis(FLIP_DURATION_MS / 2), cardBack);
        rotateOut.setFromAngle(0);
        rotateOut.setToAngle(90);
        rotateOut.setInterpolator(Interpolator.EASE_BOTH);

        rotateOut.setOnFinished(event -> {
            container.getChildren().set(cardIndex, cardFront);

            cardFront.setRotationAxis(new Point3D(0, 1, 0));
            cardFront.setRotate(90);

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

    public CompletableFuture<Void> animateCollectCards(Pane container, Runnable onFinished) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        if (container.getChildren().isEmpty()) {
            if (onFinished != null) {
                onFinished.run();
            }
            future.complete(null);
            return future;
        }

        ParallelTransition parallelTransition = new ParallelTransition();

        for (Node card : container.getChildren()) {
            double angle = Math.random() * 360;
            double distance = 500;
            double targetX = Math.cos(Math.toRadians(angle)) * distance;
            double targetY = Math.sin(Math.toRadians(angle)) * distance;

            TranslateTransition bounceUp = new TranslateTransition(Duration.millis(100), card);
            bounceUp.setByY(-20);
            bounceUp.setCycleCount(2);
            bounceUp.setAutoReverse(true);

            TranslateTransition moveAway = new TranslateTransition(Duration.millis(COLLECT_DURATION_MS), card);
            moveAway.setToX(targetX);
            moveAway.setToY(targetY);
            moveAway.setInterpolator(Interpolator.EASE_IN);

            RotateTransition rotate = new RotateTransition(Duration.millis(COLLECT_DURATION_MS), card);
            rotate.setByAngle(angle > 180 ? 360 : -360);

            FadeTransition fade = new FadeTransition(Duration.millis(COLLECT_DURATION_MS), card);
            fade.setToValue(0);

            ParallelTransition cardFlyAway = new ParallelTransition(moveAway, rotate, fade);

            SequentialTransition cardAnimation = new SequentialTransition(bounceUp, cardFlyAway);

            parallelTransition.getChildren().add(cardAnimation);
        }

        parallelTransition.setOnFinished(event -> {
            container.getChildren().clear();

            if (onFinished != null) {
                onFinished.run();
            }
            future.complete(null);
        });

        parallelTransition.play();

        return future;
    }

    public void animateWin(Pane container) {
        for (int i = 0; i < 30; i++) {
            createFireworkParticle(container);
        }
    }

    private void createFireworkParticle(Pane container) {
        Region particle = new Region();
        particle.setPrefSize(8, 8);
        particle.setBackground(new Background(new BackgroundFill(
                Color.color(Math.random(), Math.random(), Math.random()),
                new CornerRadii(4), Insets.EMPTY)));

        double centerX = container.getWidth() / 2;
        double centerY = container.getHeight() / 2;

        container.getChildren().add(particle);
        particle.setManaged(false);
        particle.setLayoutX(centerX);
        particle.setLayoutY(centerY);

        double angle = Math.random() * 360;
        double distance = 100 + Math.random() * 150;
        double targetX = Math.cos(Math.toRadians(angle)) * distance;
        double targetY = Math.sin(Math.toRadians(angle)) * distance;

        TranslateTransition moveTransition = new TranslateTransition(
                Duration.millis(1000 + Math.random() * 1000), particle);
        moveTransition.setToX(targetX);
        moveTransition.setToY(targetY);
        moveTransition.setInterpolator(Interpolator.SPLINE(0.1, 0.8, 0.3, 1.0));

        FadeTransition fadeTransition = new FadeTransition(
                Duration.millis(1000 + Math.random() * 1000), particle);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);
        fadeTransition.setDelay(Duration.millis(500 + Math.random() * 500));

        ParallelTransition animation = new ParallelTransition(moveTransition, fadeTransition);

        animation.setOnFinished(e -> container.getChildren().remove(particle));
        animation.play();
    }
}