package solar.rpg.javuno.client.controller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import solar.rpg.javuno.client.controller.ConnectionController.JavunoClientConnection;
import solar.rpg.javuno.client.models.ClientGameLobbyModel;
import solar.rpg.javuno.client.models.ClientGameModel;
import solar.rpg.javuno.client.mvc.JavunoClientMVC;
import solar.rpg.javuno.client.views.ViewGame;
import solar.rpg.javuno.models.cards.ICard;
import solar.rpg.javuno.models.packets.in.JavunoPacketInOutPlayerReadyChanged;
import solar.rpg.javuno.models.packets.out.JavunoPacketOutConnectionRejected.ConnectionRejectionReason;
import solar.rpg.javuno.mvc.IController;

import javax.swing.*;
import java.util.List;
import java.util.logging.Logger;

public class ClientGameController implements IController {

    @NotNull
    private final Logger logger;
    @NotNull
    private final JavunoClientMVC<ViewGame, ClientGameController> mvc;
    @Nullable
    private String playerName;
    @Nullable
    private ClientGameLobbyModel lobbyModel;
    @Nullable
    private ClientGameModel gameModel;
    @NotNull
    private final JavunoClientPacketHandler packetHandler;

    public ClientGameController(@NotNull Logger logger) {
        this.logger = logger;
        mvc = new JavunoClientMVC<>();
        packetHandler = new JavunoClientPacketHandler(mvc, logger);
    }

    /* Outgoing View Events (called by views) */

    public void markSelfReady() {
        getClientConnection().writePacket(new JavunoPacketInOutPlayerReadyChanged(true));
    }

    public void unmarkSelfReady() {
        getClientConnection().writePacket(new JavunoPacketInOutPlayerReadyChanged(false));
    }

    /* Server Events */

    public void onGameStart(
            @NotNull List<ICard> startingCards,
            @NotNull List<Integer> playerCardCounts,
            @NotNull List<String> gamePlayerNames,
            int startingIndex) {
        if (gameModel != null) throw new IllegalStateException("Game already exists");
        gameModel = new ClientGameModel(startingCards, playerCardCounts, gamePlayerNames, startingIndex);
    }

    public void onPlayerReadyChanged(@NotNull String playerName, boolean isReady) {
        boolean couldStart = getGameLobbyModel().canStart();
        if (isReady) getGameLobbyModel().markPlayerReady(playerName);
        else getGameLobbyModel().unmarkPlayerReady(playerName);
        boolean canStart = getGameLobbyModel().canStart();

        SwingUtilities.invokeLater(() -> mvc.getView().onPlayerReadyChanged(playerName,
                                                                            isReady,
                                                                            couldStart != canStart));
    }

    public void onPlayerConnected(@NotNull String playerName) {
        getGameLobbyModel().addPlayer(playerName);
        SwingUtilities.invokeLater(() -> {
            mvc.logClientEvent(String.format("> %s has connected.", playerName));
            mvc.getViewInformation().refreshPlayerTable();
        });
    }

    public void onPlayerDisconnected(@NotNull String playerName) {
        getGameLobbyModel().removePlayer(playerName);
        SwingUtilities.invokeLater(() -> {
            mvc.logClientEvent(String.format("> %s has disconnected.", playerName));
            mvc.getViewInformation().refreshPlayerTable();
        });
    }

    public void onJoinLobby(@NotNull List<String> existingPlayerNames, @NotNull List<String> readyPlayerNames) {
        if (lobbyModel != null) throw new IllegalStateException("Game lobby model already exists");
        mvc.getAppController().getConnectionController().onConnectionAccepted();
        lobbyModel = new ClientGameLobbyModel(existingPlayerNames, readyPlayerNames);
        SwingUtilities.invokeLater(() -> mvc.getAppController().getMVC().getView().onConnected());
    }

    public void onConnectionRejected(@NotNull ConnectionRejectionReason reason) {
        mvc.getAppController().getConnectionController().onConnectionRejected();
        SwingUtilities.invokeLater(() -> {
            String errorMsg = "";
            switch (reason) {
                case INCORRECT_PASSWORD -> errorMsg = "Incorrect server password.";
                case USERNAME_ALREADY_TAKEN -> errorMsg = "That username is already taken.";
            }

            if (!errorMsg.isEmpty()) {
                mvc.logClientEvent(String.format("> %s", errorMsg));
                mvc.getView().showErrorDialog("Unable to connect to server", errorMsg);
            }
        });
    }

    /* Field Getters/Setters */

    @NotNull
    public ClientGameModel getGameModel() {
        if (gameModel == null) throw new IllegalStateException("Game model does not exist");
        return gameModel;
    }

    @NotNull
    public ClientGameLobbyModel getGameLobbyModel() {
        if (lobbyModel == null) throw new IllegalStateException("Game lobby model does not exist");
        return lobbyModel;
    }

    @NotNull
    public String getPlayerName() {
        if (playerName == null) throw new IllegalStateException("Player name not set");
        return playerName;
    }

    public void setPlayerName(@Nullable String playerName) {
        this.playerName = playerName;
    }

    @NotNull
    public String getPlayerStatus(@NotNull String playerName) {
        return getGameLobbyModel().isPlayerReady(playerName) ? "Ready" : "Waiting";
    }

    @NotNull
    public JavunoClientPacketHandler getPacketHandler() {
        return packetHandler;
    }

    /* MVC */

    @NotNull
    private JavunoClientConnection getClientConnection() {
        return mvc.getAppController().getConnectionController().getClientConnection();
    }

    @Override
    @NotNull
    public JavunoClientMVC<ViewGame, ClientGameController> getMVC() {
        return mvc;
    }
}
