package solar.rpg.javuno.client.views;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.client.controller.ClientGameController;
import solar.rpg.javuno.client.mvc.JavunoClientMVC;
import solar.rpg.javuno.mvc.IView;
import solar.rpg.javuno.mvc.JMVC;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

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

    public ViewGame(@NotNull JavunoClientMVC<ViewGame, ClientGameController> mvc) {
        this.mvc = mvc;

        rootPanel = new JPanel(new GridLayout(3, 1));
        generateUI();
        showLobby();
    }

    /* Lobby Server Events */

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

    //TODO: Make private
    public void showLobby() {
        topRow.removeAll();
        topRow.add(opponentHintsPanel);
        topRow.add(new JPanel());
        topRow.add(new JPanel());

        middleRow.removeAll();
        middleRow.add(new JPanel());
        middleRow.add(lobbyButtonsPanel);
        setReadyButtons(false);
        middleRow.add(new JPanel());
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
        opponentHintsPanel = new JPanel();
        opponentHintsPanel.setLayout(new BoxLayout(opponentHintsPanel, BoxLayout.Y_AXIS));
        JLabel opponentHintsHeadingLabel = new JLabel("<html><h2>Opponents</h2></html>");
        JLabel opponentHintsLabel = new JLabel(
                "<html><p align='justify'><em>" +
                "Information about your opponents, such as number of cards, will appear here.</em>" +
                "</p></em></html>");
        opponentHintsPanel.add(opponentHintsHeadingLabel);
        opponentHintsPanel.add(opponentHintsLabel);

        middleRow = new JPanel(new GridLayout(1, 3));
        middleRow.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                "Play Area",
                TitledBorder.LEFT,
                TitledBorder.TOP));
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

        bottomRow = new JPanel();
        bottomRow.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                "Your Cards",
                TitledBorder.LEFT,
                TitledBorder.TOP));

        rootPanel.add(topRow);
        rootPanel.add(middleRow);
        rootPanel.add(bottomRow);
    }
}
