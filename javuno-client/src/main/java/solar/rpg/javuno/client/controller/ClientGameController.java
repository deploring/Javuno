package solar.rpg.javuno.client.controller;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.client.mvc.JavunoClientMVC;
import solar.rpg.javuno.client.views.ViewGame;
import solar.rpg.javuno.models.packets.JavunoPacketInOutChatMessage;
import solar.rpg.javuno.models.packets.JavunoPacketOutConnectionAccepted;
import solar.rpg.javuno.models.packets.JavunoPacketOutConnectionRejected;
import solar.rpg.javuno.mvc.IController;
import solar.rpg.jserver.packet.JServerPacket;

import javax.swing.*;
import java.util.logging.Logger;

public class ClientGameController implements IController {

    @NotNull
    private final Logger logger;
    @NotNull
    private final JavunoClientMVC<ViewGame, ClientGameController> mvc;
    @NotNull
    private String playerName;

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
    }

    private void handleConnectionAccepted(@NotNull JavunoPacketOutConnectionAccepted acceptedPacket) {
        mvc.getAppController().getConnectionController().onConnectionAccepted();
        SwingUtilities.invokeLater(() -> {
            getMVC().logClientEvent("Connection successful!");
            getMVC().setChatEnabled(true);
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

    @NotNull
    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(@NotNull String playerName) {
        this.playerName = playerName;
    }

    @Override
    @NotNull
    public JavunoClientMVC<ViewGame, ClientGameController> getMVC() {
        return mvc;
    }
}
