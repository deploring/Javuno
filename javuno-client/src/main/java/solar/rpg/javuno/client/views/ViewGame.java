package solar.rpg.javuno.client.views;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.client.controller.ClientGameController;
import solar.rpg.javuno.client.mvc.JavunoClientMVC;
import solar.rpg.javuno.models.cards.ColoredCard;
import solar.rpg.javuno.models.cards.ICard;
import solar.rpg.javuno.mvc.IView;
import solar.rpg.javuno.mvc.JMVC;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ViewGame implements IView {

    @NotNull
    private final JavunoClientMVC<ViewGame, ClientGameController> mvc;
    @NotNull
    private final JPanel rootPanel;
    private JPanel topRow;
    private JPanel middleRow;
    private JPanel bottomRow;
    private JPanel opponentHintsPanel;
    private JPanel lobbyButtonsPanel;
    private JButton readyButton;
    private JButton cancelButton;
    private List<JButton> clientCardButtons;
    private JPanel gameActionsPanel;
    private JPanel chooseColorPanel;
    private JButton callUnoButton;
    private JButton challengeUnoButton;
    private JButton challengeDrawFourButton;

    public ViewGame(@NotNull JavunoClientMVC<ViewGame, ClientGameController> mvc) {
        this.mvc = mvc;

        rootPanel = new JPanel(new GridLayout(3, 1));
        generateUI();
        showLobby();
    }

    /* Lobby Server Events */

    public void onGameStart() {
        refreshCards();
        refreshPlayArea();
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

    @NotNull
    @Override
    public JMVC<ViewGame, ClientGameController> getMVC() {
        return mvc;
    }

    /* UI Manipulation */

    private void refreshPlayArea() {
        middleRow.removeAll();

        JPanel deckPanel = new JPanel();
        deckPanel.setLayout(new BoxLayout(deckPanel, BoxLayout.X_AXIS));
        JButton deckButton = createCardButton("Draw", Color.LIGHT_GRAY, true);
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

        middleRow.add(gameActionsPanel);
        middleRow.revalidate();
        middleRow.repaint();
    }

    private void refreshCards() {
        clientCardButtons.clear();
        bottomRow.removeAll();

        JPanel cardsPanel = new JPanel();
        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.X_AXIS));

        if (mvc.getController().getGameModel().isParticipating()) {
            cardsPanel.add(Box.createHorizontalGlue());
            for (ICard card : mvc.getController().getGameModel().getClientCards()) {
                JButton cardButton = createCardButton(card.getSymbol(), card.getDisplayColor(), false);
                clientCardButtons.add(cardButton);
                cardsPanel.add(cardButton);
                cardsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
            }
            cardsPanel.add(Box.createHorizontalGlue());
        }
        JScrollPane cardsPane = new JScrollPane(cardsPanel);
        cardsPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        updateGameButtons();

        bottomRow.add(cardsPane, BorderLayout.CENTER);
        bottomRow.revalidate();
        bottomRow.repaint();
    }

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

    private void updateGameButtons() {
        int i = 0;
        for (ICard card : mvc.getController().getGameModel().getClientCards()) {
            boolean isPlayable = mvc.getController().isCurrentPlayer() &&
                                 mvc.getController().getGameModel().isCardPlayable(card);
            JButton cardButton = clientCardButtons.get(i);
            cardButton.setEnabled(isPlayable);
            cardButton.getComponent(1).setForeground(isPlayable ? Color.WHITE : Color.GRAY);
            cardButton.setToolTipText(isPlayable
                                      ? "Click to play this card."
                                      : "This card cannot be played currently.");
            i++;
        }

        //TODO: Enable these when needed
        callUnoButton.setEnabled(false);
        challengeUnoButton.setEnabled(false);
        challengeDrawFourButton.setEnabled(false);
    }

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

    private void setReadyButtons(boolean ready) {
        readyButton.setEnabled(!ready);
        cancelButton.setEnabled(ready);
    }

    private void onMarkSelfReadyExecute() {
        if (!readyButton.isEnabled() || cancelButton.isEnabled())
            throw new IllegalStateException("Buttons are not enabled correctly");
        mvc.getController().markSelfReady();
    }

    private void onUnmarkSelfReadyExecute() {
        if (readyButton.isEnabled() || !cancelButton.isEnabled())
            throw new IllegalStateException("Buttons are not enabled correctly");
        mvc.getController().unmarkSelfReady();
    }

    private void generateUI() {
        topRow = new JPanel(new GridLayout(1, 3));
        topRow.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                "Opponents",
                TitledBorder.LEFT,
                TitledBorder.TOP));

        middleRow = new JPanel(new GridLayout(1, 3));
        middleRow.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                "Play Area",
                TitledBorder.LEFT,
                TitledBorder.TOP));

        bottomRow = new JPanel();
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

        opponentHintsPanel = new JPanel();
        opponentHintsPanel.setLayout(new BoxLayout(opponentHintsPanel, BoxLayout.Y_AXIS));
        JLabel opponentHintsHeadingLabel = new JLabel("<html><h2>Opponents</h2></html>");
        JLabel opponentHintsLabel = new JLabel(
                "<html><p align='justify'><em>" +
                "Information about your opponents, such as number of cards, will appear here.</em>" +
                "</p></em></html>");
        opponentHintsPanel.add(opponentHintsHeadingLabel);
        opponentHintsPanel.add(opponentHintsLabel);

        lobbyButtonsPanel = new JPanel();
        lobbyButtonsPanel.setLayout(new BoxLayout(lobbyButtonsPanel, BoxLayout.Y_AXIS));
        JPanel lobbyButtonsHintsPanel = new JPanel(new BorderLayout());
        JLabel lobbyButtonsHintsLabel = new JLabel(
                "<html><p align='justify'><em>" +
                "The game starts once the first 4 players in the lobby are marked as ready. If there " +
                "are more than 4 players in the lobby, those players will spectate the game." +
                "</p></em></html>");
        lobbyButtonsHintsPanel.add(lobbyButtonsHintsLabel, BorderLayout.NORTH);
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        readyButton = new JButton("Ready");
        readyButton.addActionListener((e) -> onMarkSelfReadyExecute());
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener((e) -> onUnmarkSelfReadyExecute());
        buttonsPanel.add(readyButton);
        buttonsPanel.add(cancelButton);
        lobbyButtonsPanel.add(lobbyButtonsHintsPanel);
        lobbyButtonsPanel.add(buttonsPanel);

        /* Game Components */

        clientCardButtons = new ArrayList<>();

        gameActionsPanel = new JPanel();
        gameActionsPanel.setLayout(new BoxLayout(gameActionsPanel, BoxLayout.Y_AXIS));
        callUnoButton = new JButton("Call UNO");
        callUnoButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        challengeUnoButton = new JButton("Challenge UNO");
        challengeUnoButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        challengeDrawFourButton = new JButton("Challenge Draw Four");
        challengeDrawFourButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        gameActionsPanel.add(Box.createVerticalGlue());
        gameActionsPanel.add(callUnoButton);
        gameActionsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        gameActionsPanel.add(challengeUnoButton);
        gameActionsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        gameActionsPanel.add(challengeDrawFourButton);
        gameActionsPanel.add(Box.createVerticalGlue());

        chooseColorPanel = new JPanel(new GridLayout(4, 1));
        chooseColorPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                "Choose Wild Color",
                TitledBorder.LEFT,
                TitledBorder.TOP));
        JButton redButton = new JButton("Red");
        redButton.setBackground(ColoredCard.CardColor.RED.getColor());
        redButton.setForeground(Color.WHITE);
        JButton greenButton = new JButton("Green");
        greenButton.setBackground(ColoredCard.CardColor.GREEN.getColor());
        greenButton.setForeground(Color.WHITE);
        JButton blueButton = new JButton("Blue");
        blueButton.setBackground(ColoredCard.CardColor.BLUE.getColor());
        blueButton.setForeground(Color.WHITE);
        JButton yellowButton = new JButton("Yellow");
        yellowButton.setBackground(ColoredCard.CardColor.YELLOW.getColor());
        yellowButton.setForeground(Color.WHITE);
        chooseColorPanel.add(redButton);
        chooseColorPanel.add(greenButton);
        chooseColorPanel.add(blueButton);
        chooseColorPanel.add(yellowButton);
    }
}
