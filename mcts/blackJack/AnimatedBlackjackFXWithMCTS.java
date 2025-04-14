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

/**
 * Enhanced JavaFX frontend for the Blackjack MCTS AI system
 * Includes animations for cards and game flow
 * Fixed version with improved AI mode and game flow
 */
public class AnimatedBlackjackFXWithMCTS extends Application {

    private BlackjackGame game;
    private BlackjackState currentState;

    // Game statistics
    private int gamesPlayed = 0;
    private int playerWins = 0;
    private int dealerWins = 0;
    private int ties = 0;

    // AI control
    private boolean aiPlayerMode = false;
    private int aiThinkingTime = 500; // milliseconds

    // UI Components
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

    // Animation controller
    private BlackjackCardAnimations animations;

    // Animation and game state flags
    private AtomicBoolean animationInProgress = new AtomicBoolean(false);
    private AtomicBoolean aiMoveInProgress = new AtomicBoolean(false);
    private AtomicBoolean gameInProgress = new AtomicBoolean(false);

    // Background thread for AI processing
    private ExecutorService executorService;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        executorService = Executors.newSingleThreadExecutor();

        // Initialize the game and animations
        game = new BlackjackGame();
        animations = new BlackjackCardAnimations();

        // Create the main layout with an overlay for animations
        StackPane root = new StackPane();

        BorderPane gameLayout = new BorderPane();
        gameLayout.setPadding(new Insets(20));
        gameLayout.setStyle("-fx-background-color: darkgreen;");

        // Animation overlay (transparent pane on top of everything for animations)
        animationOverlay = new Pane();
        animationOverlay.setMouseTransparent(true); // Let mouse events pass through

        // Title and stats area (top)
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

        // Game area (center)
        VBox gameArea = new VBox(15);
        gameArea.setAlignment(Pos.CENTER);

        // Dealer area
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

        // Game status area
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

        // Player area
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

        // Add all areas to the game area
        gameArea.getChildren().addAll(dealerArea, statusArea, playerArea);
        gameLayout.setCenter(gameArea);

        // Control buttons (bottom)
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

        // AI Mode toggle
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

        // Add everything to the root stack pane
        root.getChildren().addAll(gameLayout, animationOverlay);

        // Create the scene
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Animated Blackjack with MCTS AI");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Setup animation deck position (center of screen initially)
        scene.widthProperty().addListener((obs, oldVal, newVal) ->
                animations.setDeckPosition(newVal.doubleValue() / 2, 0));

        scene.heightProperty().addListener((obs, oldVal, newVal) ->
                animations.setDeckPosition(scene.getWidth() / 2, 0));

        animations.setDeckPosition(scene.getWidth() / 2, 0);

        // Start the initial game after a short delay
        Platform.runLater(this::startNewGame);
    }

    private void toggleAIMode() {
        // Only allow toggling when not in the middle of a game animation or AI move
        if (animationInProgress.get() || aiMoveInProgress.get()) {
            // Revert toggle if pressed during animation
            aiModeToggle.setSelected(!aiModeToggle.isSelected());
            return;
        }

        aiPlayerMode = aiModeToggle.isSelected();
        aiModeToggle.setText("AI Player: " + (aiPlayerMode ? "ON" : "OFF"));

        // If toggling on mid-game and it's player's turn, let AI make a move
        if (aiPlayerMode && gameInProgress.get() && !currentState.isTerminal() && currentState.player() == 0) {
            makeAIMove();
        }
    }

    private void startNewGame() {
        // Prevent starting a new game if animation or AI move is in progress
        if (animationInProgress.get() || aiMoveInProgress.get()) {
            return;
        }

        // Set flags to indicate a new game is starting and animation is in progress
        gameInProgress.set(true);
        animationInProgress.set(true);

        // Reset game state text
        gameStatusText.setText("");

        // First, collect any existing cards with animation
        CompletableFuture<Void> collectPlayerCards = animations.animateCollectCards(playerCardsBox, null);
        CompletableFuture<Void> collectDealerCards = animations.animateCollectCards(dealerCardsBox, null);

        // When both animations complete, setup the new game
        CompletableFuture.allOf(collectPlayerCards, collectDealerCards).thenRun(() -> {
            Platform.runLater(() -> {
                // Start a new game
                currentState = (BlackjackState) game.start();

                // Deal initial cards with animation
                dealInitialCards();
            });
        });
    }

    private void dealInitialCards() {
        // Deal player cards first
        animations.animateDealMultipleCards(
                currentState.playerHand,
                playerCardsBox,
                cardValue -> BlackjackCardAnimations.createCardView(cardValue),
                () -> {
                    // Update player score
                    updatePlayerScore();

                    // Then deal dealer cards (first card face up, second card face down)
                    if (!currentState.dealerHand.isEmpty()) {
                        // Deal first dealer card face up
                        animations.animateDealCard(
                                BlackjackCardAnimations.createCardView(currentState.dealerHand.get(0)),
                                dealerCardsBox,
                                () -> {
                                    // If there's a second card, deal it face down
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
        // Enable/disable appropriate buttons
        hitButton.setDisable(false);
        standButton.setDisable(aiPlayerMode); // Disable hit/stand if AI is playing
        newGameButton.setDisable(false);

        // Update dealer score display (only showing first card)
        dealerScoreText.setText("Score: ?");

        // Clear animation flag now that setup is complete
        animationInProgress.set(false);

        // If AI mode is on, let the AI play after a short delay
        if (aiPlayerMode && !currentState.isTerminal()) {
            // Small delay before AI move for better UX
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
        // Prevent multiple AI moves at once
        if (animationInProgress.get() || aiMoveInProgress.get() || currentState.isTerminal()) {
            return;
        }

        // Set flag to indicate AI is thinking
        aiMoveInProgress.set(true);

        // Disable buttons while AI is thinking
        hitButton.setDisable(true);
        standButton.setDisable(true);
        newGameButton.setDisable(true);

        // Show thinking indicator
        aiThinkingLabel.setVisible(true);

        // Use a background thread for AI processing
        executorService.submit(() -> {
            // Create MCTS tree and find best move
            BlackjackNode rootNode = new BlackjackNode(currentState);
            BlackjackMCTS mcts = new BlackjackMCTS(rootNode, 1000); // More iterations for better decisions
            BlackjackMove bestMove = (BlackjackMove) mcts.findBestMove();

            // Simulate AI thinking time for UX purposes
            try {
                Thread.sleep(aiThinkingTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Update UI on JavaFX thread
            Platform.runLater(() -> {
                // Hide thinking indicator
                aiThinkingLabel.setVisible(false);

                if (bestMove != null) {
                    // Apply the AI's chosen move
                    if (bestMove.getAction() == BlackjackMove.Action.HIT) {
                        gameStatusText.setText("AI chooses to HIT");
                        // Clear AI move flag before calling playerHit, which will handle the move
                        aiMoveInProgress.set(false);
                        playerHit();
                    } else {
                        gameStatusText.setText("AI chooses to STAND");
                        // Clear AI move flag before calling playerStand, which will handle the move
                        aiMoveInProgress.set(false);
                        playerStand();
                    }
                } else {
                    // No valid move available, clear AI flag
                    aiMoveInProgress.set(false);
                    // Re-enable new game button
                    newGameButton.setDisable(false);
                }
            });
        });
    }

    private void playerHit() {
        // Prevent actions if animation or AI move is in progress
        if (animationInProgress.get() || (!aiPlayerMode && aiMoveInProgress.get())) {
            return;
        }

        // Set animation flag to block further actions
        animationInProgress.set(true);

        // Execute the HIT move
        BlackjackMove move = new BlackjackMove(BlackjackMove.Action.HIT, 0);

        // Get current and next state to find the new card
        BlackjackState previousState = currentState;
        currentState = (BlackjackState) currentState.next(move);

        // Find the new card (the last card in the new hand)
        int newCardValue = currentState.playerHand.get(currentState.playerHand.size() - 1);

        // Animate the new card being dealt
        animations.animateDealCard(
                BlackjackCardAnimations.createCardView(newCardValue),
                playerCardsBox,
                () -> {
                    // Update player score
                    updatePlayerScore();

                    // Clear animation flag
                    animationInProgress.set(false);

                    // Check if the game is over after player's move
                    if (currentState.isTerminal()) {
                        handleGameOver();
                    } else if (aiPlayerMode) {
                        // If game continues and AI mode is on, make another AI move
                        // Small delay for better UX
                        executorService.submit(() -> {
                            try {
                                Thread.sleep(300);
                                Platform.runLater(this::makeAIMove);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        });
                    }
                }
        );
    }

    private void playerStand() {
        // Prevent actions if animation or AI move is in progress
        if (animationInProgress.get() || (!aiPlayerMode && aiMoveInProgress.get())) {
            return;
        }

        // Set animation flag to block further actions
        animationInProgress.set(true);

        // Execute the STAND move
        BlackjackMove move = new BlackjackMove(BlackjackMove.Action.STAND, 0);
        BlackjackState previousState = currentState;
        currentState = (BlackjackState) currentState.next(move);

        // First reveal dealer's hidden card
        if (dealerCardsBox.getChildren().size() > 1) {
            animations.animateFlipCard(
                    dealerCardsBox,
                    1, // second card (index 1)
                    previousState.dealerHand.get(1),
                    () -> {
                        // Then deal any additional cards the dealer took
                        dealRemainingDealerCards(previousState.dealerHand.size());
                    }
            );
        } else {
            dealRemainingDealerCards(previousState.dealerHand.size());
        }
    }

    private void dealRemainingDealerCards(int startingCount) {
        // Get any new cards dealer took
        List<Integer> newDealerCards = currentState.dealerHand.subList(
                startingCount, currentState.dealerHand.size());

        if (!newDealerCards.isEmpty()) {
            // Animate dealing new dealer cards one by one
            dealNextDealerCard(newDealerCards, 0);
        } else {
            finishDealerTurn();
        }
    }

    // Helper method to deal dealer cards one by one
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
        // Update dealer score showing all cards
        updateDealerScore(true);

        // Clear animation flag
        animationInProgress.set(false);

        // Check if the game is over
        if (currentState.isTerminal()) {
            handleGameOver();
        }
    }

    private void handleGameOver() {
        // Game is over, update stats and display result
        gamesPlayed++;
        gameInProgress.set(false);

        int playerTotal = calculateHandValue(currentState.playerHand);
        int dealerTotal = calculateHandValue(currentState.dealerHand);

        if (playerTotal > 21) {
            // Player busts
            dealerWins++;
            gameStatusText.setText("You bust! Dealer wins.");
            gameStatusText.setFill(Color.RED);
        } else if (dealerTotal > 21) {
            // Dealer busts
            playerWins++;
            gameStatusText.setText("Dealer busts! You win!");
            gameStatusText.setFill(Color.GREEN);
            animations.animateWin(animationOverlay);
        } else if (playerTotal > dealerTotal) {
            // Player has higher score
            playerWins++;
            gameStatusText.setText("You win with " + playerTotal + " points!");
            gameStatusText.setFill(Color.GREEN);
            animations.animateWin(animationOverlay);
        } else if (dealerTotal > playerTotal) {
            // Dealer has higher score
            dealerWins++;
            gameStatusText.setText("Dealer wins with " + dealerTotal + " points.");
            gameStatusText.setFill(Color.RED);
        } else {
            // Tie
            ties++;
            gameStatusText.setText("It's a tie at " + playerTotal + " points.");
            gameStatusText.setFill(Color.YELLOW);
        }

        // Update statistics display
        statsText.setText(String.format("Games: %d | Player Wins: %d | Dealer Wins: %d | Ties: %d",
                gamesPlayed, playerWins, dealerWins, ties));

        // Disable game action buttons
        hitButton.setDisable(true);
        standButton.setDisable(true);

        // Always enable new game button
        newGameButton.setDisable(false);
    }

    private int calculateHandValue(List<Integer> hand) {
        int total = 0;
        int aceCount = 0;

        for (int card : hand) {
            int value = Math.min(card, 10);
            total += value;
            if (card == 1) aceCount++;
        }

        // Handle aces as 11 if it doesn't bust
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
            // If not revealing all, just show the value of the first card
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
        // Shutdown the executor service
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}