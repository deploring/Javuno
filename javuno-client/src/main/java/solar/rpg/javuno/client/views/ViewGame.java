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
import solar.rpg.javuno.models.game.AbstractGameModel;
import solar.rpg.javuno.mvc.IView;
import solar.rpg.javuno.mvc.JMVC;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ViewGame implements IView {
    //TODO: Some way of indicating that it is the client player's turn.

    @NotNull
    private final JavunoClientMVC<ViewGame, ClientGameController> mvc;

    private JPanel rootPanel;
    private JPanel drawPilePanel;
    private JPanel discardPilePanel;
    private JPanel actionPanel;
    private JPanel gameButtonsPanel;
    private JButton callUnoButton;
    private JButton challengeUnoButton;
    private JButton challengeDrawFourButton;
    private JPanel playAreaPanel;
    private JScrollPane clientCardsPane;
    private final JPanel selectColorPanel;

    private final ViewCard drawPileCardView;
    private final ViewCard discardPileCardView;

    @NotNull
    private final List<ViewCard> clientCardViews;
    private int focusedCardIndex;
    private boolean isSelectingColor;

    public ViewGame(@NotNull JavunoClientMVC<ViewGame, ClientGameController> mvc) {
        this.mvc = mvc;
        drawPileCardView = new ViewCard(null, "Draw", Color.GRAY, true);
        drawPileCardView.setActionEvent(() -> mvc.getController().drawCards());
        //drawPilePanel.add(drawPileCardView.getCardPanel());

        discardPileCardView = new ViewCard(null, "?", Color.GRAY, false);
        //discardPilePanel.add(discardPileCardView.getCardPanel());

        clientCardViews = new ArrayList<>();

        selectColorPanel = new JPanel(new GridLayout(4, 1));
        selectColorPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.BLACK, 1),
            "Choose Wild Color",
            TitledBorder.LEFT,
            TitledBorder.TOP
        ));

        for (CardColor cardColor : CardColor.values()) {
            JButton colorButton = new JButton(cardColor.getDescription());
            colorButton.setBackground(Color.decode("#" + cardColor.getHexColorCode()));
            colorButton.setForeground(Color.WHITE);
            colorButton.addActionListener((e) -> onSelectColorExecute(cardColor));
            selectColorPanel.add(colorButton);
        }

        //showGameButtons();
    }

    /* Server Events */

    /**
     * Called by the server when a player picks up cards from the draw pile.
     *
     * @param playerName The name of the player who picked up cards.
     * @param cardAmount The amount of cards taken from the draw pile.
     * @param nextTurn   True, if it is now the next player's turn.
     * @throws IllegalArgumentException Cards were inappropriately provided.
     */
    public void onDrawCards(@NotNull String playerName, int cardAmount, boolean self, boolean nextTurn) {
        if (self) createClientCardViews();
        else updateCardsInHand();
        updatePlayArea();

        String message = String.format(
            "> %s has drawn %d card%s from the deck. ",
            playerName,
            cardAmount,
            cardAmount == 1 ? "" : "s"
        );

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
        //TODO: Remove the card by index rather than regenerate all cards.
        if (self) createClientCardViews();
        else updateCardsInHand();
        updatePlayArea();

        ICard card = getModel().getLastPlayedCard();
        String currentPlayerName = getModel().getCurrentPlayerName();
        String message = String.format(
            "&gt; <strong>%s</strong> plays a <span style=\"color: %s\">%s</span>. ",
            card.getHexColorCode(),
            playerName,
            card.getDescription()
        );

        switch (getModel().getGameState()) {
            case AWAITING_PLAY -> {
                if (card instanceof SkipCard)
                    message += String.format(
                        "<strong>%s</strong>'s turn has been <u>skipped</u>. ",
                        getModel().getPreviousPlayer().getName()
                    );
                else if (card instanceof ReverseCard)
                    message += "The direction of play has been <u>reversed</u>. ";
                message += String.format("It is now <strong>%s</strong>'s turn.", currentPlayerName);
            }
            case AWAITING_DRAW_FOUR_RESPONSE -> message += String.format(
                "<strong>%s</strong> must either challenge this or pick up 4 cards from the draw pile.",
                currentPlayerName
            );
            case AWAITING_DRAW_TWO_RESPONSE -> message += String.format(
                "<strong>%s</strong> must play another draw two card or pick up 2 cards from the draw pile.",
                currentPlayerName
            );
            default -> throw new UnsupportedOperationException(String.format(
                "Unexpected game state %s",
                getModel().getGameState()
            ));
        }

        mvc.logClientEvent(message);
        mvc.getViewInformation().refreshPlayerTable();
    }

    /**
     * Called when the server has started a game.
     */
    public void onGameStart() {
        createClientCardViews();
        updatePlayArea();

        ICard card = getModel().getLastPlayedCard();
        String playerName = getModel().getCurrentPlayerName();

        switch (getModel().getGameState()) {
            case AWAITING_PLAY -> {
                if (card instanceof SkipCard)
                    mvc.logClientEvent(String.format(
                        "&gt; The starting player's turn has been <u>skipped</u> and it is now %s's turn.",
                        playerName
                    ));
                else if (card instanceof ReverseCard)
                    mvc.logClientEvent("&gt; The initial direction of play has been <u>reversed</u>.");
            }
            case AWAITING_INITIAL_COLOR -> mvc.logClientEvent(String.format(
                "&gt; %s must pick the color for this wild card.",
                playerName
            ));
            case AWAITING_DRAW_TWO_RESPONSE -> mvc.logClientEvent(String.format(
                "&gt; %s must play another draw two card or pick up 2 cards from the draw pile.",
                playerName
            ));
            default -> throw new IllegalStateException(String.format(
                "Unexpected game state %s",
                getModel().getGameState()
            ));
        }
    }

    public void onJoinGame() {
        createClientCardViews();
        updatePlayArea();
    }

    /* UI Actions */

    /**
     * Called when the client player selects a card to play.
     *
     * @param cardIndex The index of the card that was selected.
     * @throws IllegalStateException Cord color has already been chosen.
     */
    private void onPlayCardExecute(int cardIndex) {
        ICard card = mvc.getController().getGameModel().getClientCards().get(cardIndex);

        if (card instanceof AbstractWildCard) {
            if (isSelectingColor) showGameButtons();
            else showSelectColor(cardIndex);
        } else mvc.getController().playCard(cardIndex);
    }

    /**
     * Called when the client player selects a color option for the chosen wild card.
     *
     * @param chosenColor The color that was chosen by the client.
     * @throws IllegalStateException Not expecting color selection.
     */
    private void onSelectColorExecute(@NotNull CardColor chosenColor) {
        if (!isSelectingColor) throw new IllegalStateException("Not expecting color selection");

        mvc.getController().playWildCard(focusedCardIndex, chosenColor);
        showGameButtons();
    }

    /* UI Manipulation */

    /**
     * Displays the game buttons panel inside the action panel. This is the default shown, but must also be re-displayed
     * after selecting a color for a wild card.
     */
    private void showGameButtons() {
        isSelectingColor = false;
        focusedCardIndex = -1;
        showActionPanel(gameButtonsPanel);
    }

    /**
     * Displays the color selection panel inside the action panel when attempting to play a wild card.
     *
     * @param cardIndex The index of the wild card to pick the color for.
     */
    private void showSelectColor(int cardIndex) {
        isSelectingColor = true;
        focusedCardIndex = cardIndex;
        showActionPanel(selectColorPanel);
    }

    /**
     * Displays the given panel inside the action panel.
     *
     * @param newPanel The panel to display.
     */
    private void showActionPanel(@NotNull JPanel newPanel) {
        actionPanel.removeAll();
        actionPanel.add(newPanel);
        actionPanel.revalidate();
        actionPanel.repaint();
    }

    private void updatePlayArea() {
        ICard card = getModel().getLastPlayedCard();
        discardPileCardView.updateDiscardPileCard(card.getSymbol(), card.getDisplayColor());

        drawPileCardView.updateDrawPileCard(mvc.getController().canDrawCards());

        callUnoButton.setEnabled(mvc.getController().canCallUno());
        challengeUnoButton.setEnabled(mvc.getController().canChallengeUno());
        challengeDrawFourButton.setEnabled(
            getModel().getGameState() == AbstractGameModel.GameState.AWAITING_DRAW_FOUR_RESPONSE
        );

        showGameButtons();
    }

    /**
     * Called when there is a need to create (or re-create) the card views in the client player's hand. This happens
     * when:
     * <ul>
     *     <li>Joining an existing game.</li>
     *     <li>Starting a new game.</li>
     *     <li>Picking up new cards.</li> TODO: We know the card amount, can we just create the new cards? This way is "lazy".
     * </ul>
     */
    private void createClientCardViews() {
        clientCardViews.clear();
        clientCardsPane.removeAll();

        if (!mvc.getController().getGameModel().isParticipating()) return;

        List<ICard> clientCards = mvc.getController().getGameModel().getClientCards();
        clientCards.stream().map(
            card -> new ViewCard(card.getDescription(), card.getSymbol(), card.getDisplayColor(), false)
        ).forEachOrdered(cardView -> {
            clientCardViews.add(cardView);
            clientCardsPane.add(cardView.getCardPanel());
        });

        updateCardsInHand();
    }

    private void updateCardsInHand() {
        boolean isCurrentPlayer = mvc.getController().isCurrentPlayer();

        int i = 0;
        for (ICard card : mvc.getController().getGameModel().getClientCards()) {
            boolean isPlayable = isCurrentPlayer && mvc.getController().getGameModel().isCardPlayable(card);
            ViewCard cardView = clientCardViews.get(i);
            cardView.updateCardInHand(isPlayable, isCurrentPlayer);

            final int cardIndex = i;
            cardView.setActionEvent(() -> onPlayCardExecute(cardIndex));
        }
    }

    /* Field Getters */

    @NotNull
    private ClientGameModel getModel() {
        return mvc.getController().getGameModel();
    }

    @NotNull
    @Override
    public JMVC<ViewGame, ClientGameController> getMVC() {
        return mvc;
    }

    @NotNull
    @Override
    public JPanel getPanel() {
        return rootPanel;
    }
}
