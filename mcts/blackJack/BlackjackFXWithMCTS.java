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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Enhanced JavaFX frontend for the Blackjack MCTS AI system
 * Includes AI player mode and game statistics
 */
public class BlackjackFXWithMCTS extends Application {

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

    // Background thread for AI processing
    private ExecutorService executorService;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        executorService = Executors.newSingleThreadExecutor();

        // Initialize the game
        game = new BlackjackGame();

        // Create the main layout
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: darkgreen;");

        // Title and stats area (top)
        VBox topArea = new VBox(10);
        topArea.setAlignment(Pos.CENTER);

        Text titleText = new Text("Blackjack with MCTS AI");
        titleText.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleText.setFill(Color.WHITE);

        statsText = new Text("Games: 0 | Player Wins: 0 | Dealer Wins: 0 | Ties: 0");
        statsText.setFont(Font.font("Arial", 14));
        statsText.setFill(Color.LIGHTGRAY);

        topArea.getChildren().addAll(titleText, statsText);
        topArea.setPadding(new Insets(0, 0, 20, 0));
        root.setTop(topArea);

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
        root.setCenter(gameArea);

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
        root.setBottom(controlArea);

        // Create the scene
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Blackjack with MCTS AI");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Start the initial game
        startNewGame();
    }

    private void toggleAIMode() {
        aiPlayerMode = aiModeToggle.isSelected();
        aiModeToggle.setText("AI Player: " + (aiPlayerMode ? "ON" : "OFF"));

        // If toggling on mid-game and it's player's turn, let AI make a move
        if (aiPlayerMode && !currentState.isTerminal() && currentState.player() == 0) {
            makeAIMove();
        }
    }

    private void startNewGame() {
        // Reset the UI
        playerCardsBox.getChildren().clear();
        dealerCardsBox.getChildren().clear();
        gameStatusText.setText("");

        // Start a new game
        currentState = (BlackjackState) game.start();

        // Update the UI with initial cards
        updateUI(false);

        // Enable/disable appropriate buttons
        hitButton.setDisable(false);
        standButton.setDisable(false);
        newGameButton.setDisable(false);

        // If AI mode is on, let the AI play
        if (aiPlayerMode) {
            makeAIMove();
        }
    }

    private void makeAIMove() {
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
                Thread.sleep(aiThinkingTime * 2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Update UI on JavaFX thread
            Platform.runLater(() -> {
                if (bestMove != null) {
                    // Apply the AI's chosen move
                    if (bestMove.getAction() == BlackjackMove.Action.HIT) {
                        gameStatusText.setText("AI chooses to HIT");
                        playerHit();
                    } else {
                        gameStatusText.setText("AI chooses to STAND");
                        playerStand();
                    }
                }

                // Hide thinking indicator
                aiThinkingLabel.setVisible(false);

                // Re-enable buttons if game is not over and not in AI mode
                if (!currentState.isTerminal()) {
                    hitButton.setDisable(aiPlayerMode);
                    standButton.setDisable(aiPlayerMode);
                }
                newGameButton.setDisable(false);
            });
        });
    }

    private void playerHit() {
        // Execute the HIT move
        BlackjackMove move = new BlackjackMove(BlackjackMove.Action.HIT, 0);
        currentState = (BlackjackState) currentState.next(move);

        // Update the UI
        updateUI(false);

        // Check if the game is over after player's move
        if (currentState.isTerminal()) {
            handleGameOver();
        } else if (aiPlayerMode) {
            // If game continues and AI mode is on, make another AI move
            makeAIMove();
        }
    }

    private void playerStand() {
        // Execute the STAND move
        BlackjackMove move = new BlackjackMove(BlackjackMove.Action.STAND, 0);
        currentState = (BlackjackState) currentState.next(move);

        // Update the UI with all cards revealed
        updateUI(true);

        // Check if the game is over
        if (currentState.isTerminal()) {
            handleGameOver();
        }
    }

    private void handleGameOver() {
        // Game is over, update stats and display result
        gamesPlayed++;

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
        } else if (playerTotal > dealerTotal) {
            // Player has higher score
            playerWins++;
            gameStatusText.setText("You win with " + playerTotal + " points!");
            gameStatusText.setFill(Color.GREEN);
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

    private void updateUI(boolean revealDealerCards) {
        // Update player cards and score
        playerCardsBox.getChildren().clear();
        for (Integer card : currentState.playerHand) {
            playerCardsBox.getChildren().add(createCardView(card));
        }
        int playerScore = calculateHandValue(currentState.playerHand);
        playerScoreText.setText("Score: " + playerScore);

        // Update dealer cards and score
        dealerCardsBox.getChildren().clear();
        if (revealDealerCards || currentState.isTerminal()) {
            // Show all dealer cards
            for (Integer card : currentState.dealerHand) {
                dealerCardsBox.getChildren().add(createCardView(card));
            }
            int dealerScore = calculateHandValue(currentState.dealerHand);
            dealerScoreText.setText("Score: " + dealerScore);
        } else {
            // Show only the first dealer card, hide the rest
            if (!currentState.dealerHand.isEmpty()) {
                dealerCardsBox.getChildren().add(createCardView(currentState.dealerHand.get(0)));
                for (int i = 1; i < currentState.dealerHand.size(); i++) {
                    dealerCardsBox.getChildren().add(createCardBackView());
                }
                dealerScoreText.setText("Score: ?");
            }
        }
    }

    private VBox createCardView(int cardValue) {
        // Create a stylish card representation
        VBox cardView = new VBox(5);
        cardView.setPrefSize(80, 120);
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

    private VBox createCardBackView() {
        // Create a stylish card back
        VBox cardBack = new VBox();
        cardBack.setPrefSize(80, 120);
        cardBack.setAlignment(Pos.CENTER);
        cardBack.setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, new CornerRadii(8), BorderWidths.DEFAULT)));

        // Create a more interesting pattern for card back
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

    @Override
    public void stop() {
        // Shutdown the executor service
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}