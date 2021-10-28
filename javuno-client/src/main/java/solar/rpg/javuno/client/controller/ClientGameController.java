package solar.rpg.javuno.client.controller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import solar.rpg.javuno.client.controller.ConnectionController.JavunoClientConnection;
import solar.rpg.javuno.client.models.ClientGameLobbyModel;
import solar.rpg.javuno.client.mvc.JavunoClientMVC;
import solar.rpg.javuno.client.views.ViewGame;
import solar.rpg.javuno.models.packets.in.JavunoPacketInOutChatMessage;
import solar.rpg.javuno.models.packets.in.JavunoPacketInOutPlayerReadyChanged;
import solar.rpg.javuno.models.packets.out.JavunoPacketOutConnectionAccepted;
import solar.rpg.javuno.models.packets.out.JavunoPacketOutConnectionRejected;
import solar.rpg.javuno.models.packets.out.JavunoPacketOutPlayerConnect;
import solar.rpg.javuno.models.packets.out.JavunoPacketOutPlayerDisconnect;
import solar.rpg.javuno.mvc.IController;
import solar.rpg.jserver.packet.JServerPacket;

import javax.swing.*;
import java.util.logging.Level;
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

    public ClientGameController(@NotNull Logger logger) {
        this.logger = logger;
        mvc = new JavunoClientMVC<>();
    }

    /* Outgoing View Events (called by views) */

    public void markSelfReady() {
        getLobbyModel().markPlayerReady(getPlayerName());
        getClientConnection().writePacket(new JavunoPacketInOutPlayerReadyChanged(getPlayerName(), true));
    }

    public void unmarkSelfReady() {
        getLobbyModel().unmarkPlayerReady(getPlayerName());
        getClientConnection().writePacket(new JavunoPacketInOutPlayerReadyChanged(getPlayerName(), false));
    }

    /* Incoming Packet Handling */

    public void handleGamePacket(@NotNull JServerPacket packet) {
        logger.log(Level.FINER, String.format("Handling %s packet from server", packet.getClass().getSimpleName()));

        if (packet instanceof JavunoPacketInOutPlayerReadyChanged readyChangedPacket)
            handlePlayerReadyChanged(readyChangedPacket);
        else if (packet instanceof JavunoPacketInOutChatMessage chatPacket)
            mvc.logClientEvent(chatPacket.getMessageFormat());
        else if (packet instanceof JavunoPacketOutConnectionAccepted acceptedPacket)
            handleConnectionAccepted(acceptedPacket);
        else if (packet instanceof JavunoPacketOutConnectionRejected rejectedPacket)
            handleConnectionRejected(rejectedPacket);
        else if (packet instanceof JavunoPacketOutPlayerConnect connectPacket)
            handlePlayerConnected(connectPacket);
        else if (packet instanceof JavunoPacketOutPlayerDisconnect disconnectPacket)
            handlePlayerDisconnected(disconnectPacket);
    }

    private void handlePlayerReadyChanged(@NotNull JavunoPacketInOutPlayerReadyChanged readyChangedPacket) {
        String playerName = readyChangedPacket.getPlayerName();
        if (getLobbyModel().isPlayerReady(playerName) == readyChangedPacket.isReady())
            throw new IllegalArgumentException(String.format("Player %s is already in the specified ready state",
                                                             playerName));

        if (readyChangedPacket.isReady())
            getLobbyModel().markPlayerReady(playerName);
        else
            getLobbyModel().unmarkPlayerReady(playerName);

        SwingUtilities.invokeLater(() -> {
            if (readyChangedPacket.isReady())
                mvc.logClientEvent(String.format("> %s has marked themselves as ready to play.", playerName));
            else
                mvc.logClientEvent(String.format("> %s is no longer marked as ready to play.", playerName));
            mvc.getViewInformation().refreshPlayerTable();
        });
    }

    private void handlePlayerConnected(@NotNull JavunoPacketOutPlayerConnect connectPacket) {
        getLobbyModel().addPlayer(connectPacket.getPlayerName());
        SwingUtilities.invokeLater(() -> {
            mvc.logClientEvent(String.format("> %s has connected.", connectPacket.getPlayerName()));
            mvc.getViewInformation().refreshPlayerTable();
        });
    }

    private void handlePlayerDisconnected(@NotNull JavunoPacketOutPlayerDisconnect disconnectPacket) {
        getLobbyModel().removePlayer(disconnectPacket.getPlayerName());
        SwingUtilities.invokeLater(() -> {
            mvc.logClientEvent(String.format("> %s has disconnected.", disconnectPacket.getPlayerName()));
            mvc.getViewInformation().refreshPlayerTable();
        });
    }

    private void handleConnectionAccepted(@NotNull JavunoPacketOutConnectionAccepted acceptedPacket) {
        mvc.getAppController().getConnectionController().onConnectionAccepted();
        lobbyModel = new ClientGameLobbyModel(acceptedPacket.getExistingPlayerNames(),
                                              acceptedPacket.getReadyPlayerNames());
        SwingUtilities.invokeLater(() -> mvc.getAppController().getMVC().getView().onConnected());
    }

    private void handleConnectionRejected(@NotNull JavunoPacketOutConnectionRejected rejectedPacket) {
        mvc.getAppController().getConnectionController().onConnectionRejected();
        SwingUtilities.invokeLater(() -> {
            String errorMsg = "";
            switch (rejectedPacket.getRejectionReason()) {
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
    public ClientGameLobbyModel getLobbyModel() {
        assert lobbyModel != null : "Lobby model has not been created";
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
        return getLobbyModel().isPlayerReady(playerName) ? "Ready" : "Waiting";
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
