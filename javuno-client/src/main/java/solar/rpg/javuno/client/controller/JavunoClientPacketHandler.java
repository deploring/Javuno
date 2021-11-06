package solar.rpg.javuno.client.controller;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.client.mvc.JavunoClientMVC;
import solar.rpg.javuno.client.views.ViewGame;
import solar.rpg.javuno.models.packets.JavunoBadPacketException;
import solar.rpg.javuno.models.packets.in.JavunoPacketInOutChatMessage;
import solar.rpg.javuno.models.packets.in.JavunoPacketInOutPlayerReadyChanged;
import solar.rpg.javuno.models.packets.out.*;
import solar.rpg.jserver.packet.JServerPacket;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@code JavunoClientPacketHandler} is a delegate class of {@link ClientGameController} and has access to its
 * {@code JMVC} object to call the appropriate view & controller methods. Using this it is able to validate that any
 * received {@code out} packet contains valid data, from a valid source. Packets coming from the server are generally
 * trusted by the client to be correct, where this is not the case for outgoing packets to the server, which can be
 * malicious. If an error is found, {@link JavunoBadPacketException} is thrown and this generally results in a
 * disconnect due to a mismatch in state.
 *
 * @author jskinner
 * @since 1.0.0
 */
@SuppressWarnings("ClassCanBeRecord") // This class is not treated as a record here.
public final class JavunoClientPacketHandler {

    @NotNull
    private final Logger logger;
    @NotNull
    private final JavunoClientMVC<ViewGame, ClientGameController> mvc;

    /**
     * Constructs a new {@code JavunoClientPacketValidatorHandler} instance.
     *
     * @param mvc MVC object that belongs to the {@code ClientGameController}.
     */
    public JavunoClientPacketHandler(
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

        if (packet instanceof JavunoPacketOutGameStart gameStartPacket)
            mvc.getController().onGameStart(gameStartPacket.getStartingCards(),
                                            gameStartPacket.getPlayerCardCounts(),
                                            gameStartPacket.getGamePlayerNames(),
                                            gameStartPacket.getStartingIndex());
        else if (packet instanceof JavunoPacketInOutPlayerReadyChanged readyChangedPacket)
            mvc.getController().onPlayerReadyChanged(readyChangedPacket.getPlayerName(), readyChangedPacket.isReady());
        else if (packet instanceof JavunoPacketInOutChatMessage chatPacket)
            mvc.logClientEvent(chatPacket.getMessageFormat());
        else if (packet instanceof JavunoPacketOutServerMessage serverMessagePacket)
            mvc.logClientEvent(serverMessagePacket.getMessageFormat());
        else if (packet instanceof JavunoPacketOutConnectionAccepted acceptedPacket)
            mvc.getController().onJoinLobby(acceptedPacket.getExistingPlayerNames(),
                                            acceptedPacket.getReadyPlayerNames());
        else if (packet instanceof JavunoPacketOutConnectionRejected rejectedPacket)
            mvc.getController().onConnectionRejected(rejectedPacket.getRejectionReason());
        else if (packet instanceof JavunoPacketOutPlayerConnect connectPacket)
            mvc.getController().onPlayerConnected(connectPacket.getPlayerName());
        else if (packet instanceof JavunoPacketOutPlayerDisconnect disconnectPacket)
            mvc.getController().onPlayerDisconnected(disconnectPacket.getPlayerName());
    }
}
