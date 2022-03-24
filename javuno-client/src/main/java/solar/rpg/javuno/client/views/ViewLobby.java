package solar.rpg.javuno.client.views;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.client.controller.ClientGameController;
import solar.rpg.javuno.client.mvc.JavunoClientMVC;
import solar.rpg.javuno.mvc.IView;
import solar.rpg.javuno.mvc.JMVC;

import javax.swing.*;

public class ViewLobby implements IView {

    @NotNull
    private final JavunoClientMVC<ViewLobby, ClientGameController> mvc;

    private JPanel rootPanel;
    private JButton readyButton;
    private JButton cancelButton;

    /**
     * Constructs a new {@code ViewLobby} instance.
     *
     * @param mvc MVC relationship between this view and the {@link ClientGameController}.
     */
    public ViewLobby(@NotNull JavunoClientMVC<ViewLobby, ClientGameController> mvc) {
        this.mvc = mvc;

        readyButton.addActionListener((e) -> onMarkSelfReadyExecute());
        cancelButton.addActionListener((e) -> onMarkSelfNotReadyExecute());
    }

    /* Server Events */

    //TODO: Global message configuration? Probably use a JData XML traverser
    private static final String[] PLAYER_READY_MESSAGES = new String[]{
        "> You have marked yourself as ready to play.",
        "> You are no longer marked as ready to play.",
        "> %s has marked themselves as ready to play.",
        "> %s is no longer marked as ready to play."};

    /**
     * This method is called by the server when a player in the lobby changes their ready state.
     *
     * @param playerName Name of the player who changed their ready state.
     * @param isReady    True, if the player has marked themselves as ready.
     * @param notify     True, if the user should be notified that a game is starting/no longer starting.
     */
    public void onPlayerReadyChanged(@NotNull String playerName, boolean isReady, boolean notify) {
        boolean isSelf = playerName.equals(mvc.getController().getPlayerName());
        //TODO: This is questionable, position dependency.
        int isReadyOffset = isReady ? 0 : 1;

        if (isSelf) {
            mvc.logClientEvent(PLAYER_READY_MESSAGES[isReadyOffset]);
            setReadyButtons(isReady);
        } else mvc.logClientEvent(String.format(PLAYER_READY_MESSAGES[2 + isReadyOffset], playerName));

        if (notify) {
            if (isReady) mvc.logClientEvent(
                "&gt; As there are now at least 2 players marked as ready, the game will start in 10 seconds. Mark " +
                    "yourself as ready if you wish to play. There is a maximum of four players per game.");
            else mvc.logClientEvent("&gt; The game will no longer start.");
        }

        mvc.getViewInformation().refreshPlayerTable();
    }

    public void onShowLobby() {
        setReadyButtons(false);
    }

    /* UI Manipulation */

    /**
     * Sets the state of the "Ready" and "Cancel" buttons after an update to the client's ready state.
     *
     * @param ready True, if the client is marked as ready.
     */
    private void setReadyButtons(boolean ready) {
        readyButton.setEnabled(!ready);
        cancelButton.setEnabled(ready);
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
    private void onMarkSelfNotReadyExecute() {
        if (readyButton.isEnabled() || !cancelButton.isEnabled())
            throw new IllegalStateException("Buttons are not enabled correctly");
        mvc.getController().unmarkSelfReady();
    }

    /* Field Getters & Setters */

    @NotNull
    @Override
    public JPanel getPanel() {
        return rootPanel;
    }

    @NotNull
    @Override
    public JMVC<ViewLobby, ClientGameController> getMVC() {
        return mvc;
    }

}
