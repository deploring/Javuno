package solar.rpg.javuno.client.views;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.client.controller.ClientGameController;
import solar.rpg.javuno.client.models.ClientGameModel;
import solar.rpg.javuno.client.mvc.JavunoClientMVC;
import solar.rpg.javuno.models.cards.AbstractWildCard;
import solar.rpg.javuno.models.cards.ColoredCard.CardColor;
import solar.rpg.javuno.models.cards.ICard;
import solar.rpg.javuno.models.cards.standard.ReverseCard;
import solar.rpg.javuno.models.cards.standard.SkipCard;
import solar.rpg.javuno.models.game.AbstractGameModel.GameState;
import solar.rpg.javuno.mvc.IView;
import solar.rpg.javuno.mvc.JMVC;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code ViewGame} displays the state of the current game when connected to a server.
 *
 * @author jskinner
 * @since 1.0.0
 */
public class ViewGame implements IView {

    @NotNull
    private final JavunoClientMVC<ViewGame, ClientGameController> mvc;
    @NotNull
    private final JPanel rootPanel;
    @NotNull
    private final JPanel topRow = new JPanel();
    @NotNull
    private final JPanel middleRow = new JPanel();
    @NotNull
    private final JPanel bottomRow = new JPanel();
    @NotNull
    private final JPanel opponentHintsPanel = new JPanel();
    @NotNull
    private final JPanel lobbyButtonsPanel = new JPanel();
    @NotNull
    private final JButton readyButton = new JButton("Ready");
    @NotNull
    private final JButton cancelButton = new JButton("Cancel");
    @NotNull
    private final JButton deckButton = createCardButton("Draw", Color.LIGHT_GRAY, true);
    @NotNull
    private final List<JButton> clientCardButtons = new ArrayList<>();
    @NotNull
    private final JPanel gameActionsPanel = new JPanel();
    @NotNull
    private final JPanel selectColorPanel = new JPanel();
    @NotNull
    private final JButton callUnoButton = new JButton("Call UNO");
    @NotNull
    private final JButton challengeUnoButton = new JButton("Challenge UNO");
    @NotNull
    private final JButton challengeDrawFourButton = new JButton("Challenge Draw Four");
    @NotNull
    private ActionPanelState actionPanelState;
    private int focusedCardIndex;

    /**
     * Constructs a new {@code ViewGame} instance.
     *
     * @param mvc The MVC relationship for this view.
     */
    public ViewGame(@NotNull JavunoClientMVC<ViewGame, ClientGameController> mvc) {
        this.mvc = mvc;
        actionPanelState = ActionPanelState.UNKNOWN;
        focusedCardIndex = -1;

        rootPanel = new JPanel(new GridLayout(3, 1));
        generateUI();
        showLobby();
    }

    /* Lobby Server Events */

    /**
     * Called when a player picks up cards from the draw pile.
     *
     * @param playerName The name of the player who picked up cards.
     * @param cardAmount The amount of cards taken from the draw pile.
     * @param nextTurn   True, if it is now the next player's turn.
     * @throws IllegalArgumentException Cards were inappropriately provided.
     */
    public void onDrawCards(@NotNull String playerName, int cardAmount, boolean self, boolean nextTurn) {
        if (self) refreshCards();
        else refreshGameButtons();

        String message = String.format("> %s has drawn %d card%s from the deck. ",
                                       playerName,
                                       cardAmount,
                                       cardAmount == 1 ? "" : "s");

        if (nextTurn) message += String.format("It is now %s's turn.", getModel().getCurrentPlayerName());

        mvc.logClientEvent(message);
        mvc.getViewInformation().refreshPlayerTable();
    }

    /**
     * Called when a player has played a card.
     *
     * @param playerName The name of the player who played the card.
     * @param self       True, if the current player is this client.
     */
    public void onPlayCard(@NotNull String playerName, boolean self) {
        if (self) refreshCards();
        else refreshGameButtons();
        refreshPlayArea();

        ICard card = getModel().getLastPlayedCard();
        String currentPlayerName = getModel().getCurrentPlayerName();
        String message = String.format("> %s plays a %s. ", playerName, card.getDescription());

        switch (getModel().getCurrentGameState()) {
            case AWAITING_PLAY -> {
                if (card instanceof SkipCard)
                    message += String.format("%s's turn has been skipped. ", getModel().getPreviousPlayer().getName());
                else if (card instanceof ReverseCard)
                    message += "The direction of play has been reversed. ";
                message += String.format("It is now %s's turn.", currentPlayerName);
            }
            case AWAITING_DRAW_FOUR_RESPONSE -> message += String.format(
                    "%s must either challenge this or pick up 4 cards from the draw pile.",
                    currentPlayerName);
            case AWAITING_DRAW_TWO_RESPONSE -> message += String.format(
                    "%s must play another draw two card or pick up 2 cards from the draw pile.",
                    currentPlayerName);
            default -> throw new IllegalStateException(String.format("Unexpected game state %s",
                                                                     getModel().getCurrentGameState()));
        }

        mvc.logClientEvent(message);
        mvc.getViewInformation().refreshPlayerTable();
    }

    /**
     * Called when the server has started a game.
     */
    public void onGameStart() {
        refreshCards();
        refreshPlayArea();

        ICard card = getModel().getLastPlayedCard();
        String playerName = getModel().getCurrentPlayerName();

        switch (getModel().getCurrentGameState()) {
            case AWAITING_PLAY -> {
                if (card instanceof SkipCard)
                    mvc.logClientEvent(String.format(
                            "> The starting player's turn has been skipped and it is now %s's turn.",
                            playerName));
                else if (card instanceof ReverseCard)
                    mvc.logClientEvent("> The initial direction of play has been reversed.");
            }
            case AWAITING_INITIAL_COLOR -> mvc.logClientEvent(String.format(
                    "> %s must pick the color for this wild card.",
                    playerName));
            case AWAITING_DRAW_TWO_RESPONSE -> mvc.logClientEvent(String.format(
                    "> %s must play another draw two card or pick up 2 cards from the draw pile.",
                    playerName));
            default -> throw new IllegalStateException(String.format("Unexpected game state %s",
                                                                     getModel().getCurrentGameState()));
        }
    }

    //TODO: Global message configuration? Probably use a JData XML traverser
    private static final String[] PLAYER_READY_MESSAGES = new String[]{
            "> You have marked yourself as ready to play.",
            "> You are no longer marked as ready to play.",
            "> %s has marked themselves as ready to play.",
            "> %s is no longer marked as ready to play."};

    /**
     * Called when a player in the lobby has changed their ready status.
     *
     * @param playerName The name of the player.
     * @param isReady    True, if the player has marked themselves as ready.
     * @param notify     True, if the user should be notified that a game is starting/no longer starting.
     */
    public void onPlayerReadyChanged(@NotNull String playerName, boolean isReady, boolean notify) {
        boolean isSelf = playerName.equals(mvc.getController().getPlayerName());
        int isReadyOffset = isReady ? 0 : 1;

        if (isSelf) {
            mvc.logClientEvent(PLAYER_READY_MESSAGES[isReadyOffset]);
            setReadyButtons(isReady);
        } else mvc.logClientEvent(String.format(PLAYER_READY_MESSAGES[2 + isReadyOffset], playerName));

        if (notify) {
            if (isReady) mvc.logClientEvent(
                    "> As there are now at least 2 players marked as ready, the game will start in 10 seconds. Mark " +
                    "yourself as ready if you wish to play. There is a maximum of four players per game.");
            else mvc.logClientEvent("> The game will no longer start.");
        }

        mvc.getViewInformation().refreshPlayerTable();
    }

    /**
     * Called when the client has successfully connected to a server.
     */
    public void onConnected() {
        if (mvc.getController().getGameLobbyModel().isInGame()) {
            refreshCards();
            refreshPlayArea();
        } else showLobby();
    }

    /* Getters and Setters */

    @NotNull
    public JPanel getPanel() {
        return rootPanel;
    }

    /* UI Manipulation */

    /**
     * Shows the game action buttons panel.
     *
     * @throws IllegalStateException Already showing game action buttons panel.
     */
    private void showGameActionButtons() {
        if (actionPanelState == ActionPanelState.ACTION_BUTTONS)
            throw new IllegalStateException("Already showing game action buttons panel");
        actionPanelState = ActionPanelState.ACTION_BUTTONS;
        focusedCardIndex = -1;
        swapActionPanel(gameActionsPanel);
    }

    /**
     * Shows the select color panel.
     *
     * @throws IllegalStateException Already selecting color.
     */
    private void showSelectColor(int cardIndex) {
        if (actionPanelState == ActionPanelState.SELECT_COLOR)
            throw new IllegalStateException("Already selecting color");
        actionPanelState = ActionPanelState.SELECT_COLOR;
        focusedCardIndex = cardIndex;
        swapActionPanel(selectColorPanel);
    }

    /**
     * Swaps out the current panel in the 2,1 position on the 3x3 game view grid.
     *
     * @param newPanel The new panel to display.
     */
    private void swapActionPanel(@NotNull JPanel newPanel) {
        if (middleRow.getComponents().length >= 3) middleRow.remove(2);
        middleRow.add(newPanel, 2);
        middleRow.revalidate();
        middleRow.repaint();
    }

    /**
     * Refreshes the state of the play area. This includes the deck, the discard pile, and the game actions.
     */
    private void refreshPlayArea() {
        middleRow.removeAll();

        JPanel deckPanel = new JPanel();
        deckPanel.setLayout(new BoxLayout(deckPanel, BoxLayout.X_AXIS));
        deckPanel.add(Box.createHorizontalGlue());
        deckPanel.add(deckButton);
        deckPanel.add(Box.createHorizontalGlue());
        middleRow.add(deckPanel);

        ICard card = mvc.getController().getGameModel().getLastPlayedCard();
        JPanel discardPanel = new JPanel();
        discardPanel.setLayout(new BoxLayout(discardPanel, BoxLayout.X_AXIS));
        JButton discardButton = createCardButton(card.getSymbol(), card.getDisplayColor(), false);
        discardButton.setEnabled(false);
        discardPanel.add(Box.createHorizontalGlue());
        discardPanel.add(discardButton);
        discardPanel.add(Box.createHorizontalGlue());
        middleRow.add(discardPanel);

        actionPanelState = ActionPanelState.UNKNOWN;
        showGameActionButtons();
    }

    /**
     * Clears out any existing card buttons and re-creates them.
     * This should happen whenever the client plays a card or receives a card.
     */
    private void refreshCards() {
        clientCardButtons.clear();
        bottomRow.removeAll();

        JPanel cardsPanel = new JPanel();
        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.X_AXIS));

        if (mvc.getController().getGameModel().isParticipating()) {
            cardsPanel.add(Box.createHorizontalGlue());
            List<ICard> clientCards = mvc.getController().getGameModel().getClientCards();
            clientCards.stream().map(card -> createCardButton(card.getSymbol(),
                                                              card.getDisplayColor(),
                                                              false)).forEachOrdered(cardButton -> {
                clientCardButtons.add(cardButton);
                cardsPanel.add(cardButton);
                cardsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
            });
            cardsPanel.add(Box.createHorizontalGlue());
        }
        JScrollPane cardsPane = new JScrollPane(cardsPanel);
        cardsPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        refreshGameButtons();

        bottomRow.add(cardsPane, BorderLayout.CENTER);
        bottomRow.revalidate();
        bottomRow.repaint();
    }

    /**
     * Creates a special "JAVUNO" card button to display in the game UI.
     *
     * @param symbol   The symbol to display on the corners of the card.
     * @param color    The display color of the card.
     * @param grayLogo True, if the logo should be displayed as gray by default (otherwise white).
     * @return The button.
     */
    private JButton createCardButton(String symbol, Color color, boolean grayLogo) {
        JButton cardButton = new JButton();
        cardButton.setMinimumSize(new Dimension(90, 180));
        cardButton.setMaximumSize(new Dimension(90, 180));
        cardButton.setLayout(new BorderLayout());
        cardButton.setBackground(color);

        JLabel topLeft = new JLabel(symbol);
        topLeft.setForeground(Color.WHITE);
        JLabel center = new JLabel("<html><em>JAVUNO</em></html>", SwingConstants.CENTER);
        center.setForeground(grayLogo ? Color.GRAY : Color.WHITE);
        JLabel bottomRight = new JLabel(symbol, SwingConstants.RIGHT);
        bottomRight.setForeground(Color.WHITE);

        cardButton.add(topLeft, BorderLayout.PAGE_START);
        cardButton.add(center, BorderLayout.CENTER);
        cardButton.add(bottomRight, BorderLayout.SOUTH);
        return cardButton;
    }

    /**
     * Called when the player selects a card to play.
     *
     * @param cardIndex The index of the card that was selected.
     * @throws IllegalStateException Cord color has already been chosen.
     */
    private void onPlayCardExecute(int cardIndex) {
        ICard card = mvc.getController().getGameModel().getClientCards().get(cardIndex);

        if (card instanceof AbstractWildCard wildCard) {
            if (wildCard.getChosenCardColor() != null)
                throw new IllegalStateException("Card color has already been chosen");
            else {
                if (actionPanelState != ActionPanelState.SELECT_COLOR) showSelectColor(cardIndex);
                else showGameActionButtons();
            }
        } else mvc.getController().playCard(cardIndex);
    }

    /**
     * Refreshes the state of the game buttons, including the client's cards (if applicable).
     */
    private void refreshGameButtons() {
        int i = 0;
        for (ICard card : mvc.getController().getGameModel().getClientCards()) {
            boolean isCurrentPlayer = mvc.getController().isCurrentPlayer();
            boolean isPlayable = isCurrentPlayer && mvc.getController().getGameModel().isCardPlayable(card);
            JButton cardButton = clientCardButtons.get(i);
            cardButton.setEnabled(isPlayable);
            cardButton.getComponent(1).setForeground(isPlayable ? Color.WHITE : Color.GRAY);
            cardButton.setToolTipText(String.format("(%s) %s",
                                                    card.getDescription(),
                                                    isPlayable
                                                    ? "Click to play this card."
                                                    : isCurrentPlayer
                                                      ? "This card cannot be played."
                                                      : "It is not your turn, please wait."));

            for (ActionListener listener : cardButton.getActionListeners())
                cardButton.removeActionListener(listener);

            final int cardIndex = i;
            cardButton.addActionListener((e) -> onPlayCardExecute(cardIndex));
            i++;
        }

        boolean canDrawCards = mvc.getController().canDrawCards();
        deckButton.setEnabled(canDrawCards);
        deckButton.getComponent(1).setForeground(canDrawCards ? Color.WHITE : Color.GRAY);
        deckButton.setToolTipText(canDrawCards
                                  ? "Click to draw your card(s)."
                                  : "You cannot draw a card at the moment.");

        callUnoButton.setEnabled(mvc.getController().canCallUno());
        challengeUnoButton.setEnabled(mvc.getController().canChallengeUno());
        challengeDrawFourButton.setEnabled(getModel().getCurrentGameState() == GameState.AWAITING_DRAW_FOUR_RESPONSE);
    }

    /**
     * Shows the lobby components in the game view.
     * This clears any existing components in the view.
     */
    private void showLobby() {
        topRow.removeAll();
        topRow.add(opponentHintsPanel);
        topRow.add(new JPanel());
        topRow.add(new JPanel());

        middleRow.removeAll();
        middleRow.add(new JPanel());
        middleRow.add(lobbyButtonsPanel);
        setReadyButtons(false);
        middleRow.add(new JPanel());

        rootPanel.revalidate();
        rootPanel.repaint();
    }

    /**
     * Sets the state of the "ready" and "cancel" buttons in the lobby UI.
     *
     * @param ready True, if the player is marked as ready.
     */
    private void setReadyButtons(boolean ready) {
        readyButton.setEnabled(!ready);
        cancelButton.setEnabled(ready);
    }

    /**
     * Called when the player selects a color option from the select color panel.
     *
     * @param chosenColor The color that was chosen by the client.
     * @throws IllegalStateException Not expecting color selection.
     * @throws IllegalStateException Current card is not a wild card.
     */
    private void onSelectColor(@NotNull CardColor chosenColor) {
        if (actionPanelState != ActionPanelState.SELECT_COLOR)
            throw new IllegalStateException("Not expecting color selection");
        ICard card = getModel().getClientCards().get(focusedCardIndex);
        if (!(card instanceof AbstractWildCard))
            throw new IllegalStateException("Current card is not a wild card");
        mvc.getController().playWildCard(focusedCardIndex, chosenColor);
        showGameActionButtons();
    }

    /**
     * Called when the player clicks the "Ready" button in the lobby UI.
     *
     * @throws IllegalStateException Buttons are not enabled correctly.
     */
    private void onMarkSelfReadyExecute() {
        if (!readyButton.isEnabled() || cancelButton.isEnabled())
            throw new IllegalStateException("Buttons are not enabled correctly");
        mvc.getController().markSelfReady();
    }

    /**
     * Called when the player clicks the "Cancel" button in the lobby UI.
     *
     * @throws IllegalStateException Buttons are not enabled correctly.
     */
    private void onUnmarkSelfReadyExecute() {
        if (readyButton.isEnabled() || !cancelButton.isEnabled())
            throw new IllegalStateException("Buttons are not enabled correctly");
        mvc.getController().unmarkSelfReady();
    }

    /**
     * Sets UI component state.
     */
    private void generateUI() {
        /* View components */

        topRow.setLayout(new GridLayout(1, 3));
        topRow.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                "Opponents",
                TitledBorder.LEFT,
                TitledBorder.TOP));

        middleRow.setLayout(new GridLayout(1, 3));
        middleRow.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                "Play Area",
                TitledBorder.LEFT,
                TitledBorder.TOP));

        bottomRow.setLayout(new BorderLayout());
        bottomRow.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                "Your Cards",
                TitledBorder.LEFT,
                TitledBorder.TOP));

        rootPanel.add(topRow);
        rootPanel.add(middleRow);
        rootPanel.add(bottomRow);

        /* Lobby Components */

        opponentHintsPanel.setLayout(new BoxLayout(opponentHintsPanel, BoxLayout.Y_AXIS));
        JLabel opponentHintsHeadingLabel = new JLabel("<html><h2>Opponents</h2></html>");
        JLabel opponentHintsLabel = new JLabel(
                "<html><p align='justify'><em>" +
                "Information about your opponents, such as number of cards, will appear here.</em>" +
                "</p></em></html>");
        opponentHintsPanel.add(opponentHintsHeadingLabel);
        opponentHintsPanel.add(opponentHintsLabel);

        lobbyButtonsPanel.setLayout(new BoxLayout(lobbyButtonsPanel, BoxLayout.Y_AXIS));
        JPanel lobbyButtonsHintsPanel = new JPanel(new BorderLayout());
        JLabel lobbyButtonsHintsLabel = new JLabel(
                "<html><p align='justify'><em>" +
                "The game starts once the first 4 players in the lobby are marked as ready. If there " +
                "are more than 4 players in the lobby, those players will spectate the game." +
                "</p></em></html>");
        lobbyButtonsHintsPanel.add(lobbyButtonsHintsLabel, BorderLayout.NORTH);
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        readyButton.addActionListener((e) -> onMarkSelfReadyExecute());
        cancelButton.addActionListener((e) -> onUnmarkSelfReadyExecute());
        buttonsPanel.add(readyButton);
        buttonsPanel.add(cancelButton);
        lobbyButtonsPanel.add(lobbyButtonsHintsPanel);
        lobbyButtonsPanel.add(buttonsPanel);

        /* Game Components */

        deckButton.addActionListener((e) -> mvc.getController().drawCards());

        gameActionsPanel.setLayout(new BoxLayout(gameActionsPanel, BoxLayout.Y_AXIS));
        callUnoButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        challengeUnoButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        challengeDrawFourButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        gameActionsPanel.add(Box.createVerticalGlue());
        gameActionsPanel.add(callUnoButton);
        gameActionsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        gameActionsPanel.add(challengeUnoButton);
        gameActionsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        gameActionsPanel.add(challengeDrawFourButton);
        gameActionsPanel.add(Box.createVerticalGlue());

        selectColorPanel.setLayout(new GridLayout(4, 1));
        selectColorPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                "Choose Wild Color",
                TitledBorder.LEFT,
                TitledBorder.TOP));

        for (CardColor cardColor : CardColor.values()) {
            JButton colorButton = new JButton(cardColor.getDescription());
            colorButton.setBackground(cardColor.getColor());
            colorButton.setForeground(Color.WHITE);
            colorButton.addActionListener((e) -> onSelectColor(cardColor));
            selectColorPanel.add(colorButton);
        }
    }

    /**
     * Denotes the different states the right-hand box in the middle row can be in.
     */
    private enum ActionPanelState {
        /**
         * Default state.
         */
        UNKNOWN,
        /**
         * Action buttons, e.g. Call Uno, Challenge Uno, etc.
         */
        ACTION_BUTTONS,
        /**
         * Select color (after attempting to play a wild card).
         */
        SELECT_COLOR,
        /**
         * Select color of a wild card that was played as the starting card.
         */
        SELECT_INITIAL_COLOR
    }

    /* MVC */

    @NotNull
    private ClientGameModel getModel() {
        return mvc.getController().getGameModel();
    }

    @NotNull
    @Override
    public JMVC<ViewGame, ClientGameController> getMVC() {
        return mvc;
    }
}
