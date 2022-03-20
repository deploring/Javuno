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
public class ViewGameOld implements IView {

    @NotNull
    private final JavunoClientMVC<ViewGameOld, ClientGameController> mvc;
    @NotNull
    private final JPanel rootPanel;
    @NotNull
    private final JPanel topRow = new JPanel();
    private TitledBorder topRowBorder;
    @NotNull
    private final JPanel bottomRow = new JPanel();
    private TitledBorder bottomRowBorder;
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
    public ViewGameOld(@NotNull JavunoClientMVC<ViewGameOld, ClientGameController> mvc) {
        this.mvc = mvc;
        actionPanelState = ActionPanelState.UNKNOWN;
        focusedCardIndex = -1;

        rootPanel = new JPanel(new GridLayout(2, 1));
        generateUI();
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

        switch (getModel().getGameState()) {
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
                                                                     getModel().getGameState()));
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

        switch (getModel().getGameState()) {
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
                                                                     getModel().getGameState()));
        }
    }

    /**
     * Called when the client has successfully connected to a server.
     */
    public void onConnected() {
        if (mvc.getController().getGameLobbyModel().isInGame()) {
            refreshCards();
            refreshPlayArea();
        }
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
        if (topRow.getComponents().length >= 3) topRow.remove(2);
        topRow.add(newPanel, 2);
        topRow.revalidate();
        topRow.repaint();
    }

    /**
     * Refreshes the state of the play area. This includes the deck, the discard pile, and the game actions.
     */
    private void refreshPlayArea() {
        topRow.removeAll();

        JPanel deckPanel = new JPanel();
        deckPanel.setLayout(new BoxLayout(deckPanel, BoxLayout.X_AXIS));
        deckPanel.add(Box.createHorizontalGlue());
        deckPanel.add(deckButton);
        deckPanel.add(Box.createHorizontalGlue());
        topRow.add(deckPanel);

        ICard card = mvc.getController().getGameModel().getLastPlayedCard();
        JPanel discardPanel = new JPanel();
        discardPanel.setLayout(new BoxLayout(discardPanel, BoxLayout.X_AXIS));
        JButton discardButton = createCardButton(card.getSymbol(), card.getDisplayColor(), false);
        discardButton.setEnabled(false);
        discardPanel.add(Box.createHorizontalGlue());
        discardPanel.add(discardButton);
        discardPanel.add(Box.createHorizontalGlue());
        topRow.add(discardPanel);

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
        boolean isCurrentPlayer = mvc.getController().isCurrentPlayer();

        Color color = isCurrentPlayer ? Color.RED : Color.BLACK;
        topRowBorder.setBorder(BorderFactory.createLineBorder(color));
        topRowBorder.setTitleColor(color);

        int i = 0;
        for (ICard card : mvc.getController().getGameModel().getClientCards()) {
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
        challengeDrawFourButton.setEnabled(getModel().getGameState() == GameState.AWAITING_DRAW_FOUR_RESPONSE);
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
     * Sets UI component state.
     */
    private void generateUI() {
        /* View components */
        topRow.setLayout(new GridLayout(1, 2));
        topRowBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                "Play Area",
                TitledBorder.LEFT,
                TitledBorder.TOP);
        topRow.setBorder(topRowBorder);

        bottomRow.setLayout(new BorderLayout());
        bottomRowBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                "Your Cards",
                TitledBorder.LEFT,
                TitledBorder.TOP);
        bottomRow.setBorder(bottomRowBorder);

        rootPanel.add(topRow);
        rootPanel.add(bottomRow);

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
    public JMVC<ViewGameOld, ClientGameController> getMVC() {
        return mvc;
    }
}
