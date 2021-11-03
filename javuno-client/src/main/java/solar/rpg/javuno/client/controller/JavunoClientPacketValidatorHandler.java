package solar.rpg.javuno.client.controller;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.client.controller.ConnectionController.JavunoClientConnection;
import solar.rpg.javuno.client.models.ClientGameLobbyModel;
import solar.rpg.javuno.client.mvc.JavunoClientMVC;
import solar.rpg.javuno.client.views.ViewGame;
import solar.rpg.javuno.models.packets.JavunoBadPacketException;
import solar.rpg.javuno.models.packets.in.JavunoPacketInOutChatMessage;
import solar.rpg.javuno.models.packets.in.JavunoPacketInOutPlayerReadyChanged;
import solar.rpg.javuno.models.packets.out.*;
import solar.rpg.jserver.packet.JServerPacket;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@code JavunoClientPacketValidatorHandler} is a delegate class of {@link ClientGameController} and has access to
 * its {@code JMVC} object to call the appropriate view & controller methods.
 * Using this it is able to validate that any received {@code out} packet contains valid data, from a valid source.
 * Packets coming from the server are generally trusted by the client to be correct, where this is not the case for
 * outgoing packets to the server, which can be malicious. If an error is found, {@link JavunoBadPacketException}
 * is thrown and this generally results in a disconnect due to a mismatch in state.
 *
 * @author jskinner
 * @since 1.0.0
 */
public final class JavunoClientPacketValidatorHandler {

    @NotNull
    private final Logger logger;
    @NotNull
    private final JavunoClientMVC<ViewGame, ClientGameController> mvc;

    /**
     * Constructs a new {@code JavunoClientPacketValidatorHandler} instance.
     *
     * @param mvc MVC object that belongs to the {@code ClientGameController}.
     */
    public JavunoClientPacketValidatorHandler(
            @NotNull JavunoClientMVC<ViewGame, ClientGameController> mvc,
            @NotNull Logger logger) {
        this.mvc = mvc;
        this.logger = logger;
    }

    /**
     * Public facing method so any inbound packet can be handled by the client appropriately.
     * This method delegates the validation and handling of each type of packet to various functions.
     *
     * @param packet The inbound packet (from the server) to handle.
     * @throws JavunoBadPacketException There was a validation error or a problem handling the packet.
     */
    public void handlePacket(@NotNull JServerPacket packet) throws JavunoBadPacketException {
        logger.log(Level.FINER, String.format("Handling %s packet from server", packet.getClass().getSimpleName()));

        if (packet instanceof JavunoPacketInOutPlayerReadyChanged readyChangedPacket)
            handlePlayerReadyChanged(readyChangedPacket);
        else if (packet instanceof JavunoPacketInOutChatMessage chatPacket)
            mvc.logClientEvent(chatPacket.getMessageFormat());
        else if (packet instanceof JavunoPacketOutServerMessage serverMessagePacket)
            mvc.logClientEvent(serverMessagePacket.getMessageFormat());
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
        try {
            boolean couldStart = getModel().canStart();
            if (readyChangedPacket.isReady()) getModel().markPlayerReady(playerName);
            else getModel().unmarkPlayerReady(playerName);
            boolean canStart = getModel().canStart();

            SwingUtilities.invokeLater(() -> mvc.getView().onPlayerReadyChanged(playerName,
                                                                                readyChangedPacket.isReady(),
                                                                                couldStart != canStart));
        } catch (IllegalStateException | IllegalArgumentException e) {
            throw new JavunoBadPacketException(
                    String.format("Unable to change player ready status: %s", e.getMessage()),
                    true);
        }
    }

    private void handlePlayerConnected(@NotNull JavunoPacketOutPlayerConnect connectPacket) {
        getModel().addPlayer(connectPacket.getPlayerName());
        SwingUtilities.invokeLater(() -> {
            mvc.logClientEvent(String.format("> %s has connected.", connectPacket.getPlayerName()));
            mvc.getViewInformation().refreshPlayerTable();
        });
    }

    private void handlePlayerDisconnected(@NotNull JavunoPacketOutPlayerDisconnect disconnectPacket) {
        getModel().removePlayer(disconnectPacket.getPlayerName());
        SwingUtilities.invokeLater(() -> {
            mvc.logClientEvent(String.format("> %s has disconnected.", disconnectPacket.getPlayerName()));
            mvc.getViewInformation().refreshPlayerTable();
        });
    }

    private void handleConnectionAccepted(@NotNull JavunoPacketOutConnectionAccepted acceptedPacket) {
        mvc.getAppController().getConnectionController().onConnectionAccepted();
        mvc.getController().setGameLobbyModel(acceptedPacket.getExistingPlayerNames(),
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

    /* MVC */

    @NotNull
    private ClientGameLobbyModel getModel() {
        return mvc.getController().getGameLobbyModel();
    }

    @NotNull
    private JavunoClientConnection getClientConnection() {
        return mvc.getAppController().getConnectionController().getClientConnection();
    }
}
