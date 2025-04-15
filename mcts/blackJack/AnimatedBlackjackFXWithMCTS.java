package com.phasmidsoftware.dsaipg.projects.mcts.blackJack;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class AnimatedBlackjackFXWithMCTS extends Application {

    private BlackjackGame game;
    private BlackjackState currentState;

    private int gamesPlayed = 0;
    private int playerWins = 0;
    private int dealerWins = 0;
    private int ties = 0;

    private boolean aiPlayerMode = false;
    private int aiThinkingTime = 500;

    private HBox playerCardsBox;
    private HBox dealerCardsBox;
    private Text playerScoreText;
    private Text dealerScoreText;
    private Button hitButton;
    private Button standButton;
    private Button newGameButton;
    private ToggleButton aiModeToggle;
    private Text gameStatusText;
    private Label aiThinkingLabel;
    private Text statsText;
    private Pane animationOverlay;

    private BlackjackCardAnimations animations;

    private AtomicBoolean animationInProgress = new AtomicBoolean(false);
    private AtomicBoolean aiMoveInProgress = new AtomicBoolean(false);
    private AtomicBoolean gameInProgress = new AtomicBoolean(false);

    private ExecutorService executorService;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        executorService = Executors.newSingleThreadExecutor();

        game = new BlackjackGame();
        animations = new BlackjackCardAnimations();

        StackPane root = new StackPane();

        BorderPane gameLayout = new BorderPane();
        gameLayout.setPadding(new Insets(20));
        gameLayout.setStyle("-fx-background-color: darkgreen;");

        animationOverlay = new Pane();
        animationOverlay.setMouseTransparent(true);

        VBox topArea = new VBox(10);
        topArea.setAlignment(Pos.CENTER);

        Text titleText = new Text("Animated Blackjack with MCTS AI");
        titleText.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleText.setFill(Color.WHITE);

        statsText = new Text("Games: 0 | Player Wins: 0 | Dealer Wins: 0 | Ties: 0");
        statsText.setFont(Font.font("Arial", 14));
        statsText.setFill(Color.LIGHTGRAY);

        topArea.getChildren().addAll(titleText, statsText);
        topArea.setPadding(new Insets(0, 0, 20, 0));
        gameLayout.setTop(topArea);

        VBox gameArea = new VBox(15);
        gameArea.setAlignment(Pos.CENTER);

        VBox dealerArea = new VBox(10);
        dealerArea.setAlignment(Pos.CENTER);

        Label dealerLabel = new Label("Dealer's Cards");
        dealerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        dealerLabel.setTextFill(Color.WHITE);

        dealerCardsBox = new HBox(10);
        dealerCardsBox.setAlignment(Pos.CENTER);

        dealerScoreText = new Text("Score: ?");
        dealerScoreText.setFont(Font.font("Arial", 16));
        dealerScoreText.setFill(Color.WHITE);

        dealerArea.getChildren().addAll(dealerLabel, dealerCardsBox, dealerScoreText);

        HBox statusArea = new HBox(10);
        statusArea.setAlignment(Pos.CENTER);
        statusArea.setPadding(new Insets(15));

        gameStatusText = new Text("");
        gameStatusText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        gameStatusText.setFill(Color.YELLOW);

        aiThinkingLabel = new Label("AI is thinking...");
        aiThinkingLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        aiThinkingLabel.setTextFill(Color.ORANGE);
        aiThinkingLabel.setVisible(false);

        statusArea.getChildren().addAll(gameStatusText, aiThinkingLabel);

        VBox playerArea = new VBox(10);
        playerArea.setAlignment(Pos.CENTER);

        Label playerLabel = new Label("Your Cards");
        playerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        playerLabel.setTextFill(Color.WHITE);

        playerCardsBox = new HBox(10);
        playerCardsBox.setAlignment(Pos.CENTER);

        playerScoreText = new Text("Score: 0");
        playerScoreText.setFont(Font.font("Arial", 16));
        playerScoreText.setFill(Color.WHITE);

        playerArea.getChildren().addAll(playerLabel, playerCardsBox, playerScoreText);

        gameArea.getChildren().addAll(dealerArea, statusArea, playerArea);
        gameLayout.setCenter(gameArea);

        VBox controlArea = new VBox(15);
        controlArea.setAlignment(Pos.CENTER);
        controlArea.setPadding(new Insets(20, 0, 0, 0));

        HBox controlsBox = new HBox(20);
        controlsBox.setAlignment(Pos.CENTER);

        hitButton = new Button("HIT");
        hitButton.setPrefSize(100, 40);
        hitButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        hitButton.setOnAction(e -> playerHit());

        standButton = new Button("STAND");
        standButton.setPrefSize(100, 40);
        standButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        standButton.setOnAction(e -> playerStand());

        newGameButton = new Button("NEW GAME");
        newGameButton.setPrefSize(150, 40);
        newGameButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        newGameButton.setOnAction(e -> startNewGame());

        controlsBox.getChildren().addAll(hitButton, standButton, newGameButton);

        HBox aiControlBox = new HBox(10);
        aiControlBox.setAlignment(Pos.CENTER);
        aiControlBox.setPadding(new Insets(10, 0, 0, 0));

        aiModeToggle = new ToggleButton("AI Player: OFF");
        aiModeToggle.setPrefSize(150, 30);
        aiModeToggle.setFont(Font.font("Arial", 14));
        aiModeToggle.setOnAction(e -> toggleAIMode());

        Label aiExplanationLabel = new Label("When ON, AI will play for you using MCTS algorithm");
        aiExplanationLabel.setFont(Font.font("Arial", 14));
        aiExplanationLabel.setTextFill(Color.LIGHTGRAY);

        aiControlBox.getChildren().addAll(aiModeToggle, aiExplanationLabel);

        controlArea.getChildren().addAll(controlsBox, aiControlBox);
        gameLayout.setBottom(controlArea);

        root.getChildren().addAll(gameLayout, animationOverlay);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Animated Blackjack with MCTS AI");
        primaryStage.setScene(scene);
        primaryStage.show();

        scene.widthProperty().addListener((obs, oldVal, newVal) ->
                animations.setDeckPosition(newVal.doubleValue() / 2, 0));

        scene.heightProperty().addListener((obs, oldVal, newVal) ->
                animations.setDeckPosition(scene.getWidth() / 2, 0));

        animations.setDeckPosition(scene.getWidth() / 2, 0);

        Platform.runLater(this::startNewGame);
    }

    private void updateButtonStates() {
        Platform.runLater(() -> {
            boolean isAnimating = animationInProgress.get();
            boolean isAiThinking = aiMoveInProgress.get();
            boolean isGameOver = currentState != null && currentState.isTerminal();

            hitButton.setDisable(isAnimating || isAiThinking || isGameOver || aiPlayerMode);
            standButton.setDisable(isAnimating || isAiThinking || isGameOver || aiPlayerMode);

            newGameButton.setDisable(isAnimating || isAiThinking);

            aiModeToggle.setDisable(isAnimating || isAiThinking);
        });
    }

    private void toggleAIMode() {
        if (animationInProgress.get() || aiMoveInProgress.get()) {
            aiModeToggle.setSelected(!aiModeToggle.isSelected());
            return;
        }

        aiPlayerMode = aiModeToggle.isSelected();
        aiModeToggle.setText("AI Player: " + (aiPlayerMode ? "ON" : "OFF"));

        updateButtonStates();

        if (aiPlayerMode && gameInProgress.get() && !currentState.isTerminal() && currentState.player() == 0) {
            makeAIMove();
        }
    }

    private void startNewGame() {
        if (animationInProgress.get() || aiMoveInProgress.get()) {
            return;
        }

        gameInProgress.set(true);
        animationInProgress.set(true);

        updateButtonStates();

        gameStatusText.setText("");

        CompletableFuture<Void> collectPlayerCards = animations.animateCollectCards(playerCardsBox, null);
        CompletableFuture<Void> collectDealerCards = animations.animateCollectCards(dealerCardsBox, null);

        CompletableFuture.allOf(collectPlayerCards, collectDealerCards).thenRun(() -> {
            Platform.runLater(() -> {
                currentState = (BlackjackState) game.start();

                dealInitialCards();
            });
        });
    }

    private void dealInitialCards() {
        animations.animateDealMultipleCards(
                currentState.playerHand,
                playerCardsBox,
                cardValue -> BlackjackCardAnimations.createCardView(cardValue),
                () -> {
                    updatePlayerScore();

                    if (!currentState.dealerHand.isEmpty()) {
                        animations.animateDealCard(
                                BlackjackCardAnimations.createCardView(currentState.dealerHand.get(0)),
                                dealerCardsBox,
                                () -> {
                                    if (currentState.dealerHand.size() > 1) {
                                        animations.animateDealCard(
                                                BlackjackCardAnimations.createCardBackView(),
                                                dealerCardsBox,
                                                this::finishGameSetup
                                        );
                                    } else {
                                        finishGameSetup();
                                    }
                                }
                        );
                    } else {
                        finishGameSetup();
                    }
                }
        );
    }

    private void finishGameSetup() {
        dealerScoreText.setText("Score: ?");

        animationInProgress.set(false);

        updateButtonStates();

        if (aiPlayerMode && !currentState.isTerminal()) {
            executorService.submit(() -> {
                try {
                    Thread.sleep(500);
                    Platform.runLater(this::makeAIMove);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }

    private void makeAIMove() {
        if (animationInProgress.get() || aiMoveInProgress.get() || currentState.isTerminal()) {
            return;
        }

        aiMoveInProgress.set(true);

        updateButtonStates();

        aiThinkingLabel.setVisible(true);

        executorService.submit(() -> {
            BlackjackNode rootNode = new BlackjackNode(currentState);
            BlackjackMCTS mcts = new BlackjackMCTS(rootNode, 1000);
            BlackjackMove bestMove = (BlackjackMove) mcts.findBestMove();

            try {
                Thread.sleep(aiThinkingTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Platform.runLater(() -> {
                aiThinkingLabel.setVisible(false);

                if (bestMove != null) {
                    if (bestMove.getAction() == BlackjackMove.Action.HIT) {
                        gameStatusText.setText("AI chooses to HIT");
                        aiMoveInProgress.set(false);

                        updateButtonStates();

                        playerHit();
                    } else {
                        gameStatusText.setText("AI chooses to STAND");
                        aiMoveInProgress.set(false);

                        updateButtonStates();

                        playerStand();
                    }
                } else {
                    aiMoveInProgress.set(false);

                    updateButtonStates();
                }
            });
        });
    }

    private void playerHit() {
        if (animationInProgress.get() || aiMoveInProgress.get() || (aiPlayerMode && !gameStatusText.getText().contains("AI chooses"))) {
            return;
        }

        animationInProgress.set(true);

        updateButtonStates();

        BlackjackMove move = new BlackjackMove(BlackjackMove.Action.HIT, 0);

        BlackjackState previousState = currentState;
        currentState = (BlackjackState) currentState.next(move);

        int newCardValue = currentState.playerHand.get(currentState.playerHand.size() - 1);

        if (gameStatusText.getText().contains("AI chooses")) {
            Platform.runLater(() -> gameStatusText.setText(""));
        }

        animations.animateDealCard(
                BlackjackCardAnimations.createCardView(newCardValue),
                playerCardsBox,
                () -> {
                    updatePlayerScore();

                    animationInProgress.set(false);

                    if (currentState.isTerminal()) {
                        handleGameOver();
                    } else if (aiPlayerMode) {
                        executorService.submit(() -> {
                            try {
                                Thread.sleep(300);
                                Platform.runLater(this::makeAIMove);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        });
                    } else {
                        updateButtonStates();
                    }
                }
        );
    }

    private void playerStand() {
        if (animationInProgress.get() || aiMoveInProgress.get() || (aiPlayerMode && !gameStatusText.getText().contains("AI chooses"))) {
            return;
        }

        animationInProgress.set(true);

        updateButtonStates();

        if (gameStatusText.getText().contains("AI chooses")) {
            Platform.runLater(() -> gameStatusText.setText(""));
        }

        BlackjackMove move = new BlackjackMove(BlackjackMove.Action.STAND, 0);
        BlackjackState previousState = currentState;
        currentState = (BlackjackState) currentState.next(move);

        if (dealerCardsBox.getChildren().size() > 1) {
            animations.animateFlipCard(
                    dealerCardsBox,
                    1,
                    previousState.dealerHand.get(1),
                    () -> {
                        dealRemainingDealerCards(previousState.dealerHand.size());
                    }
            );
        } else {
            dealRemainingDealerCards(previousState.dealerHand.size());
        }
    }

    private void dealRemainingDealerCards(int startingCount) {
        List<Integer> newDealerCards = currentState.dealerHand.subList(
                startingCount, currentState.dealerHand.size());

        if (!newDealerCards.isEmpty()) {
            dealNextDealerCard(newDealerCards, 0);
        } else {
            finishDealerTurn();
        }
    }

    private void dealNextDealerCard(List<Integer> cards, int index) {
        if (index >= cards.size()) {
            finishDealerTurn();
            return;
        }

        animations.animateDealCard(
                BlackjackCardAnimations.createCardView(cards.get(index)),
                dealerCardsBox,
                () -> dealNextDealerCard(cards, index + 1)
        );
    }

    private void finishDealerTurn() {
        updateDealerScore(true);

        animationInProgress.set(false);

        if (currentState.isTerminal()) {
            handleGameOver();
        } else {
            updateButtonStates();
        }
    }

    private void handleGameOver() {
        gamesPlayed++;
        gameInProgress.set(false);

        int playerTotal = calculateHandValue(currentState.playerHand);
        int dealerTotal = calculateHandValue(currentState.dealerHand);

        if (playerTotal > 21) {
            dealerWins++;
            gameStatusText.setText("You bust! Dealer wins.");
            gameStatusText.setFill(Color.RED);
        } else if (dealerTotal > 21) {
            playerWins++;
            gameStatusText.setText("Dealer busts! You win!");
            gameStatusText.setFill(Color.GREEN);
            animations.animateWin(animationOverlay);
        } else if (playerTotal > dealerTotal) {
            playerWins++;
            gameStatusText.setText("You win with " + playerTotal + " points!");
            gameStatusText.setFill(Color.GREEN);
            animations.animateWin(animationOverlay);
        } else if (dealerTotal > playerTotal) {
            dealerWins++;
            gameStatusText.setText("Dealer wins with " + dealerTotal + " points.");
            gameStatusText.setFill(Color.RED);
        } else {
            ties++;
            gameStatusText.setText("It's a tie at " + playerTotal + " points.");
            gameStatusText.setFill(Color.YELLOW);
        }

        statsText.setText(String.format("Games: %d | Player Wins: %d | Dealer Wins: %d | Ties: %d",
                gamesPlayed, playerWins, dealerWins, ties));

        updateButtonStates();
    }

    private int calculateHandValue(List<Integer> hand) {
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

    private void updatePlayerScore() {
        int playerScore = calculateHandValue(currentState.playerHand);
        playerScoreText.setText("Score: " + playerScore);
    }

    private void updateDealerScore(boolean revealAll) {
        if (revealAll) {
            int dealerScore = calculateHandValue(currentState.dealerHand);
            dealerScoreText.setText("Score: " + dealerScore);
        } else {
            if (!currentState.dealerHand.isEmpty()) {
                int firstCardValue = Math.min(currentState.dealerHand.get(0), 10);
                dealerScoreText.setText("Score: " + firstCardValue + "+?");
            } else {
                dealerScoreText.setText("Score: ?");
            }
        }
    }

    @Override
    public void stop() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}