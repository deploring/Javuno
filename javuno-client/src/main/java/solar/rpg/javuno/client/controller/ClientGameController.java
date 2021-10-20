package solar.rpg.javuno.client.controller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import solar.rpg.javuno.client.models.ClientGameLobbyModel;
import solar.rpg.javuno.client.mvc.JavunoClientMVC;
import solar.rpg.javuno.client.views.MainFrame;
import solar.rpg.javuno.client.views.ViewGame;
import solar.rpg.javuno.models.packets.*;
import solar.rpg.javuno.mvc.IController;
import solar.rpg.jserver.packet.JServerPacket;

import javax.swing.*;
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

    public void handleGamePacket(JServerPacket packet) {
        if (packet instanceof JavunoPacketInOutChatMessage chatPacket)
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
        lobbyModel = new ClientGameLobbyModel(acceptedPacket.getExistingPlayerNames());
        SwingUtilities.invokeLater(() -> {
            mvc.getViewInformation().onConnected();
            mvc.getAppController().getMVC().getView().showView(MainFrame.ViewType.GAME_LOBBY);
        });
    }

    private void handleConnectionRejected(@NotNull JavunoPacketOutConnectionRejected rejectedPacket) {
        getMVC().getAppController().getConnectionController().onConnectionRejected();
        SwingUtilities.invokeLater(() -> {
            String errorMsg = "";
            switch (rejectedPacket.getRejectionReason()) {
                case INCORRECT_PASSWORD -> errorMsg = "Incorrect server password.";
                case USERNAME_ALREADY_TAKEN -> errorMsg = "That username is already taken.";
            }

            if (!errorMsg.isEmpty()) {
                mvc.logClientEvent(String.format(">> %s", errorMsg));
                mvc.getView().showErrorDialog("Unable to connect to server", errorMsg);
            }
        });
    }

    @Nullable
    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(@Nullable String playerName) {
        this.playerName = playerName;
    }

    @Override
    @NotNull
    public JavunoClientMVC<ViewGame, ClientGameController> getMVC() {
        return mvc;
    }

    @NotNull
    public ClientGameLobbyModel getLobbyModel() {
        assert lobbyModel != null : "Lobby model has not been created";
        return lobbyModel;
    }
}
