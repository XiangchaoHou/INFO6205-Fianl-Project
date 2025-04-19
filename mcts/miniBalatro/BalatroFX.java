package com.phasmidsoftware.dsaipg.projects.mcts.miniBalatro;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Move;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BalatroFX extends Application {

    private BalatroGame game;
    private State<BalatroGame> currentState;
    private List<Card> selectedCards = new ArrayList<>();
    private List<Card> lastPlayedCards = new ArrayList<>();
    private List<Card> allTableCards = new ArrayList<>();

    private VBox handCardsContainer;
    private HBox tableCardsContainer;
    private TextArea gameLogArea;
    private Label scoreLabel;
    private Label playsLabel;
    private Label discardsLabel;
    private Label deckCountLabel;
    private Button playButton;
    private Button discardButton;
    private Button mctsButton;
    private Button newGameButton;
    private Button viewAllTableCardsButton;
    private ToggleButton autoPlayToggle;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private boolean autoPlayEnabled = false;

    private final double CARD_WIDTH = 100;
    private final double CARD_HEIGHT = 140;
    private final double CARD_SPACING = 10;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #0a6522;"); 

        HBox topInfo = createTopInfoArea();
        root.setTop(topInfo);

        VBox centerArea = new VBox(20);
        centerArea.setAlignment(Pos.CENTER);

        Label tableLabel = new Label("Last Played Cards");
        tableLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        tableLabel.setTextFill(Color.WHITE);

        tableCardsContainer = new HBox(CARD_SPACING);
        tableCardsContainer.setAlignment(Pos.CENTER);
        tableCardsContainer.setMinHeight(CARD_HEIGHT + 20);

        viewAllTableCardsButton = new Button("View All Table Cards");
        viewAllTableCardsButton.setOnAction(e -> showAllTableCards());

        VBox tableSection = new VBox(10);
        tableSection.setAlignment(Pos.CENTER);
        tableSection.getChildren().addAll(tableLabel, tableCardsContainer, viewAllTableCardsButton);

        Label handLabel = new Label("Your Hand");
        handLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        handLabel.setTextFill(Color.WHITE);

        handCardsContainer = new VBox(10);
        handCardsContainer.setAlignment(Pos.CENTER);

        ScrollPane handScrollPane = new ScrollPane(handCardsContainer);
        handScrollPane.setFitToWidth(true);
        handScrollPane.setPrefHeight(CARD_HEIGHT + 40);
        handScrollPane.setStyle("-fx-background: transparent; -fx-background-color: rgba(0, 0, 0, 0.2);");

        VBox handSection = new VBox(5, handLabel, handScrollPane);
        handSection.setAlignment(Pos.CENTER);

        centerArea.getChildren().addAll(tableSection, handSection);
        root.setCenter(centerArea);

        VBox bottomArea = createBottomControls();
        root.setBottom(bottomArea);

        Scene scene = new Scene(root, 900, 700);
        primaryStage.setTitle("Mini Balatro");
        primaryStage.setScene(scene);
        primaryStage.show();
        startNewGame();
    }

    private HBox createTopInfoArea() {
        HBox topInfo = new HBox(20);
        topInfo.setAlignment(Pos.CENTER);
        topInfo.setPadding(new Insets(10));

        scoreLabel = new Label("Score: 0");
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        scoreLabel.setTextFill(Color.WHITE);

        playsLabel = new Label("Plays: 5");
        playsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        playsLabel.setTextFill(Color.WHITE);

        discardsLabel = new Label("Discards: 3");
        discardsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        discardsLabel.setTextFill(Color.WHITE);

        deckCountLabel = new Label("Deck: 44");
        deckCountLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        deckCountLabel.setTextFill(Color.WHITE);

        autoPlayToggle = new ToggleButton("Auto-Play: OFF");
        autoPlayToggle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        autoPlayToggle.setOnAction(e -> toggleAutoPlay());

        Region spacer1 = new Region();
        Region spacer2 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        topInfo.getChildren().addAll(scoreLabel, spacer1, playsLabel, discardsLabel, spacer2, deckCountLabel, autoPlayToggle);

        return topInfo;
    }

    private VBox createBottomControls() {
        VBox bottomArea = new VBox(10);
        bottomArea.setPadding(new Insets(10));

        HBox controls = new HBox(20);
        controls.setAlignment(Pos.CENTER);

        playButton = new Button("Play Selected Cards");
        playButton.setPrefWidth(150);
        playButton.setOnAction(e -> playSelectedCards());

        discardButton = new Button("Discard Selected Cards");
        discardButton.setPrefWidth(150);
        discardButton.setOnAction(e -> discardSelectedCards());

        mctsButton = new Button("Suggest Move (MCTS)");
        mctsButton.setPrefWidth(150);
        mctsButton.setOnAction(e -> suggestMoveWithMCTS());

        newGameButton = new Button("New Game");
        newGameButton.setPrefWidth(100);
        newGameButton.setOnAction(e -> startNewGame());

        controls.getChildren().addAll(playButton, discardButton, mctsButton, newGameButton);

        gameLogArea = new TextArea();
        gameLogArea.setPrefHeight(150);
        gameLogArea.setEditable(false);
        gameLogArea.setWrapText(true);
        VBox.setVgrow(gameLogArea, Priority.ALWAYS);

        Text logLabel = new Text("Game Log:");
        logLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        logLabel.setFill(Color.WHITE);

        bottomArea.getChildren().addAll(controls, logLabel, gameLogArea);

        return bottomArea;
    }

    private void startNewGame() {
        selectedCards.clear();
        lastPlayedCards.clear();
        allTableCards.clear();

        game = new BalatroGame();
        currentState = game.start();

        updateUI();

        logGameEvent("New game started. Make your first move!");

        if (autoPlayEnabled) {
            performAutoPlay();
        }
    }

    private void toggleAutoPlay() {
        autoPlayEnabled = autoPlayToggle.isSelected();
        autoPlayToggle.setText("Auto-Play: " + (autoPlayEnabled ? "ON" : "OFF"));

        if (autoPlayEnabled && !currentState.isTerminal()) {
            performAutoPlay();
        }
    }

    private void performAutoPlay() {
        if (!autoPlayEnabled || currentState.isTerminal()) {
            return;
        }

        logGameEvent("Auto-Play: Calculating next move...");
        setControlsEnabled(false);

        executor.submit(() -> {
            BalatroNode rootNode = new BalatroNode(currentState);
            BalatroMCTS mcts = new BalatroMCTS(rootNode, 1000);
            Move<BalatroGame> bestMove = mcts.findBestMove();

            Platform.runLater(() -> {
                if (bestMove == null) {
                    logGameEvent("Auto-Play: No valid moves found");
                    setControlsEnabled(true);
                    return;
                }

                BalatroMove balatroMove = (BalatroMove) bestMove;
                List<Card> moveCards = balatroMove.getCards();

                selectedCards.clear();

                BalatroState state = (BalatroState) currentState;
                List<Card> playerHand = state.hand;

                List<Card> alreadySelected = new ArrayList<>();

                for (Card moveCard : moveCards) {
                    for (Card handCard : playerHand) {
                        if (!alreadySelected.contains(handCard) &&
                                handCard.getRank() == moveCard.getRank() &&
                                handCard.getSuit() == moveCard.getSuit()) {
                            selectedCards.add(handCard);
                            alreadySelected.add(handCard);
                            break;
                        }
                    }
                }

                updateUI();

                logGameEvent("Auto-Play: " + balatroMove.getAction() + " " + moveCards);

                Timeline timeline = new Timeline(
                        new KeyFrame(Duration.seconds(1), e -> {
                            if (balatroMove.getAction() == BalatroMove.Action.PLAY) {
                                playSelectedCardsWithoutChecks();
                            } else {
                                discardSelectedCardsWithoutChecks();
                            }

                            if (!currentState.isTerminal() && autoPlayEnabled) {
                                Timeline nextMoveTimeline = new Timeline(
                                        new KeyFrame(Duration.seconds(0.5), event -> performAutoPlay())
                                );
                                nextMoveTimeline.play();
                            } else {
                                setControlsEnabled(true);

                                if (currentState.isTerminal()) {
                                    int finalScore = ((BalatroState)currentState).getScore();
                                    logGameEvent("\n=== GAME OVER ===");
                                    logGameEvent("Final Score: " + finalScore);
                                    logGameEvent("Start a new game to play again!");
                                }
                            }
                        })
                );
                timeline.play();
            });
        });
    }

    private void updateUI() {
        BalatroState state = (BalatroState) currentState;

        scoreLabel.setText("Score: " + state.getScore());
        playsLabel.setText("Plays: " + state.remainingPlays);
        discardsLabel.setText("Discards: " + state.remainingDiscards);
        deckCountLabel.setText("Deck: " + state.deck.size());

        updateHandCards(state.hand);

        updateTableCards(lastPlayedCards);

        allTableCards = new ArrayList<>(state.table);

        boolean isTerminal = state.isTerminal();
        playButton.setDisable(selectedCards.isEmpty() || state.remainingPlays <= 0);
        discardButton.setDisable(selectedCards.isEmpty() || state.remainingDiscards <= 0);
        mctsButton.setDisable(isTerminal);
        viewAllTableCardsButton.setDisable(allTableCards.isEmpty());
    }

    private void updateHandCards(List<Card> handCards) {
        handCardsContainer.getChildren().clear();
        if (handCards.isEmpty()) {
            Label emptyLabel = new Label("No cards in hand");
            emptyLabel.setTextFill(Color.WHITE);
            handCardsContainer.getChildren().add(emptyLabel);
            return;
        }

        HBox cardRow = new HBox(CARD_SPACING);
        cardRow.setAlignment(Pos.CENTER);

        for (Card card : handCards) {
            Region cardView = createCardView(card, true);
            cardRow.getChildren().add(cardView);
        }

        handCardsContainer.getChildren().add(cardRow);
    }

    private void updateTableCards(List<Card> tableCards) {
        tableCardsContainer.getChildren().clear();
        if (tableCards.isEmpty()) {
            Label emptyLabel = new Label("No cards played yet");
            emptyLabel.setTextFill(Color.WHITE);
            tableCardsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Card card : tableCards) {
            Region cardView = createCardView(card, false);
            tableCardsContainer.getChildren().add(cardView);
        }
    }

    private void showAllTableCards() {
        if (allTableCards.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Table Cards");
            alert.setHeaderText("No cards on table yet");
            alert.showAndWait();
            return;
        }

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("All Cards on Table");

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-background-color: #0a6522;");

        Label titleLabel = new Label("All Cards on Table");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.WHITE);

        FlowPane cardsPane = new FlowPane(10, 10);
        cardsPane.setAlignment(Pos.CENTER);

        for (Card card : allTableCards) {
            Region cardView = createCardView(card, false);
            cardsPane.getChildren().add(cardView);
        }

        ScrollPane scrollPane = new ScrollPane(cardsPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        scrollPane.setPrefWidth(600);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: rgba(0, 0, 0, 0.2);");

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> dialog.close());

        content.getChildren().addAll(titleLabel, scrollPane, closeButton);

        Scene dialogScene = new Scene(content);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private Region createCardView(Card card, boolean selectable) {
        String cardText = card.getFaceValue() + getUnicodeSuit(card.getSuit());
        Color cardColor = getCardColor(card.getSuit());

        VBox cardView = new VBox(5);
        cardView.setAlignment(Pos.CENTER);
        cardView.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        cardView.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #333333; -fx-border-width: 1;");

        Text topValue = new Text(cardText);
        topValue.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        topValue.setFill(cardColor);

        Text centerValue = new Text(cardText);
        centerValue.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        centerValue.setFill(cardColor);

        cardView.getChildren().addAll(topValue, centerValue);

        if (selectable) {
            boolean isSelected = selectedCards.contains(card);

            if (isSelected) {
                cardView.setStyle(cardView.getStyle() + "-fx-effect: dropshadow(three-pass-box, gold, 10, 0.7, 0, 0); -fx-border-color: gold; -fx-border-width: 2;");
            }

            cardView.setOnMouseClicked(e -> {
                if (selectedCards.contains(card)) {
                    selectedCards.remove(card);
                    cardView.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #333333; -fx-border-width: 1;");
                } else {
                    selectedCards.add(card);
                    cardView.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: gold; -fx-border-width: 2;");

                    DropShadow glow = new DropShadow();
                    glow.setColor(Color.GOLD);
                    glow.setWidth(0);
                    glow.setHeight(0);
                    cardView.setEffect(glow);

                    Timeline timeline = new Timeline(
                            new KeyFrame(Duration.ZERO,
                                    new KeyValue(glow.widthProperty(), 0),
                                    new KeyValue(glow.heightProperty(), 0)),
                            new KeyFrame(Duration.millis(300),
                                    new KeyValue(glow.widthProperty(), 20),
                                    new KeyValue(glow.heightProperty(), 20))
                    );
                    timeline.play();
                }

                BalatroState state = (BalatroState) currentState;
                playButton.setDisable(selectedCards.isEmpty() || state.remainingPlays <= 0);
                discardButton.setDisable(selectedCards.isEmpty() || state.remainingDiscards <= 0);
            });
        }

        return cardView;
    }

    private Color getCardColor(Card.Suit suit) {
        switch (suit) {
            case HEARTS:
            case DIAMONDS:
                return Color.RED;
            default:
                return Color.BLACK;
        }
    }

    private String getUnicodeSuit(Card.Suit suit) {
        switch (suit) {
            case CLUBS:
                return "♣";
            case DIAMONDS:
                return "♦";
            case HEARTS:
                return "♥";
            case SPADES:
                return "♠";
            default:
                return "";
        }
    }

    private void playSelectedCards() {
        if (selectedCards.isEmpty()) {
            logGameEvent("You must select at least one card to play");
            return;
        }

        BalatroState state = (BalatroState) currentState;

        if (state.remainingPlays <= 0) {
            logGameEvent("No remaining plays available");
            return;
        }

        playSelectedCardsWithoutChecks();
    }

    private void playSelectedCardsWithoutChecks() {
        List<Card> cardsToPlay = new ArrayList<>(selectedCards);

        BalatroMove move = new BalatroMove(BalatroMove.Action.PLAY, cardsToPlay, 0);

        BalatroState state = (BalatroState) currentState;
        int previousScore = state.getScore();

        State<BalatroGame> newState = currentState.next(move);
        currentState = newState;

        int newScore = ((BalatroState)currentState).getScore();
        int scoreGained = newScore - previousScore;

        lastPlayedCards = cardsToPlay;

        logGameEvent("Played: " + cardsToPlay + " - Scored: " + scoreGained + " points");

        animateCardsPlayed(cardsToPlay, scoreGained);

        if (currentState.isTerminal()) {
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1.5), e -> {
                int finalScore = ((BalatroState)currentState).getScore();
                logGameEvent("\n=== GAME OVER ===");
                logGameEvent("Final Score: " + finalScore);
                logGameEvent("Start a new game to play again!");
            }));
            timeline.play();
        }

        selectedCards.clear();
        updateUI();
    }

    private void discardSelectedCards() {
        if (selectedCards.isEmpty()) {
            logGameEvent("You must select at least one card to discard");
            return;
        }

        BalatroState state = (BalatroState) currentState;

        if (state.remainingDiscards <= 0) {
            logGameEvent("No remaining discards available");
            return;
        }

        discardSelectedCardsWithoutChecks();
    }

    private void discardSelectedCardsWithoutChecks() {
        List<Card> cardsToDiscard = new ArrayList<>(selectedCards);

        BalatroMove move = new BalatroMove(BalatroMove.Action.DISCARD, cardsToDiscard, 0);

        currentState = currentState.next(move);

        logGameEvent("Discarded: " + cardsToDiscard);

        animateCardsDiscarded(cardsToDiscard);

        selectedCards.clear();
        updateUI();
    }

    private void animateCardsPlayed(List<Card> cards, int scoreGained) {
        StackPane animationPane = new StackPane();
        animationPane.setAlignment(Pos.CENTER);

        Scene scene = handCardsContainer.getScene();
        BorderPane root = (BorderPane) scene.getRoot();
        root.getChildren().add(animationPane);

        Text scoreText = new Text("+" + scoreGained);
        scoreText.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        scoreText.setFill(Color.GOLD);
        scoreText.setStroke(Color.BLACK);
        scoreText.setStrokeWidth(1.5);

        scoreText.setOpacity(0);
        StackPane.setAlignment(scoreText, Pos.CENTER);
        animationPane.getChildren().add(scoreText);

        Timeline scoreAnimation = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(scoreText.opacityProperty(), 0),
                        new KeyValue(scoreText.translateYProperty(), 0)),
                new KeyFrame(Duration.millis(300),
                        new KeyValue(scoreText.opacityProperty(), 1),
                        new KeyValue(scoreText.translateYProperty(), 0)),
                new KeyFrame(Duration.millis(1500),
                        new KeyValue(scoreText.opacityProperty(), 0),
                        new KeyValue(scoreText.translateYProperty(), -100))
        );

        scoreAnimation.setOnFinished(e -> root.getChildren().remove(animationPane));
        scoreAnimation.play();
    }

    private void animateCardsDiscarded(List<Card> cards) {}

    private void suggestMoveWithMCTS() {
        setControlsEnabled(false);
        logGameEvent("Calculating best move with MCTS...");

        executor.submit(() -> {
            BalatroNode rootNode = new BalatroNode(currentState);
            BalatroMCTS mcts = new BalatroMCTS(rootNode, 1000);

            Move<BalatroGame> bestMove = mcts.findBestMove();

            Platform.runLater(() -> {
                if (bestMove == null) {
                    logGameEvent("No valid moves found");
                } else {
                    BalatroMove balatroMove = (BalatroMove) bestMove;
                    List<Card> moveCards = balatroMove.getCards();

                    selectedCards.clear();

                    BalatroState state = (BalatroState) currentState;
                    List<Card> playerHand = state.hand;

                    List<Card> alreadySelected = new ArrayList<>();

                    for (Card moveCard : moveCards) {
                        for (Card handCard : playerHand) {
                            if (!alreadySelected.contains(handCard) &&
                                    handCard.getRank() == moveCard.getRank() &&
                                    handCard.getSuit() == moveCard.getSuit()) {
                                selectedCards.add(handCard);
                                alreadySelected.add(handCard);
                                break;
                            }
                        }
                    }

                    updateUI();

                    logGameEvent("Suggested move: " + balatroMove.getAction() + " " + moveCards);
                }

                setControlsEnabled(true);
            });
        });
    }

    private void setControlsEnabled(boolean enabled) {
        playButton.setDisable(!enabled);
        discardButton.setDisable(!enabled);
        mctsButton.setDisable(!enabled);
        newGameButton.setDisable(!enabled);
        viewAllTableCardsButton.setDisable(!enabled || allTableCards.isEmpty());
        autoPlayToggle.setDisable(!enabled);
    }

    private void logGameEvent(String message) {
        gameLogArea.appendText(message + "\n");
        gameLogArea.setScrollTop(Double.MAX_VALUE);
    }

    @Override
    public void stop() {
        executor.shutdownNow();
    }
}