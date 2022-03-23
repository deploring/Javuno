package solar.rpg.javuno.client.controller;

import org.apache.commons.text.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.client.mvc.JavunoClientMVC;
import solar.rpg.javuno.models.packets.JavunoBadPacketException;
import solar.rpg.javuno.models.packets.in.JavunoPacketInOutChatMessage;
import solar.rpg.javuno.models.packets.in.JavunoPacketInOutPlayerReadyChanged;
import solar.rpg.javuno.models.packets.out.*;
import solar.rpg.javuno.mvc.IView;
import solar.rpg.jserver.packet.JServerPacket;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@code JavunoClientPacketHandler} is a delegate class of {@link ClientGameController} and has access to its {@code
 * JMVC} object to call the appropriate view & controller methods. Using this it is able to validate that any received
 * {@code out} packet contains valid data, from a valid source. Packets coming from the server are generally trusted by
 * the client to be correct, where this is not the case for outgoing packets to the server, which can be malicious. If
 * an error is found, {@link JavunoBadPacketException} is thrown and this generally results in a disconnect due to a
 * mismatch in state.
 *
 * @author jskinner
 * @since 1.0.0
 */
@SuppressWarnings("ClassCanBeRecord") // This class is not a record.
public final class JavunoClientPacketHandler {

    @NotNull
    private final Logger logger;
    @NotNull
    private final JavunoClientMVC<ViewGameOld, ClientGameController> mvc;

    /**
     * Constructs a new {@code JavunoClientPacketValidatorHandler} instance.
     *
     * @param mvc MVC object that belongs to the {@code ClientGameController}.
     */
    public JavunoClientPacketHandler(
        @NotNull JavunoClientMVC<ViewGameOld, ClientGameController> mvc,
        @NotNull Logger logger) {
        this.mvc = mvc;
        this.logger = logger;
    }

    /**
     * Public facing method so any inbound packet can be handled by the client appropriately. This method delegates the
     * validation and handling of each type of packet to various functions.
     *
     * @param packet The inbound packet (from the server) to handle.
     * @throws JavunoBadPacketException There was a validation error or a problem handling the packet.
     */
    public void handlePacket(@NotNull JServerPacket packet) throws JavunoBadPacketException {
        logger.log(Level.FINER, String.format("Handling %s packet from server", packet.getClass().getSimpleName()));

        if (packet instanceof JavunoPacketOutDrawCards drawCardsPacket)
            handleDrawCardsPacket(drawCardsPacket);
        if (packet instanceof JavunoPacketOutPlayCard playCardPacket)
            handlePlayCardPacket(playCardPacket);
        else if (packet instanceof JavunoPacketOutGameStart gameStartPacket)
            handleGameStartPacket(gameStartPacket);
        else if (packet instanceof JavunoPacketInOutPlayerReadyChanged readyChangedPacket)
            handleReadyChangedPacket(readyChangedPacket);
        else if (packet instanceof JavunoPacketInOutChatMessage chatPacket)
            IView.invoke(() -> mvc.logClientEvent(chatPacket.getMessageFormat(StringEscapeUtils::escapeHtml4)), logger);
        else if (packet instanceof JavunoPacketOutServerMessage serverMessagePacket)
            IView.invoke(() -> mvc.logClientEvent(serverMessagePacket.getMessageFormat()), logger);
        else if (packet instanceof JavunoPacketOutConnectionAccepted acceptedPacket)
            handleConnectionAccepted(acceptedPacket);
        else if (packet instanceof JavunoPacketOutConnectionRejected rejectedPacket)
            handleConnectionRejected(rejectedPacket);
        else if (packet instanceof JavunoPacketOutPlayerConnect connectPacket)
            handlePlayerConnect(connectPacket);
        else if (packet instanceof JavunoPacketOutPlayerDisconnect disconnectPacket)
            handlePlayerDisconnect(disconnectPacket);
    }

    /**
     * Handles an incoming {@link JavunoPacketOutDrawCards} from the server. If a problem is encountered applying the
     * data, the client disconnects from the server due to the bad state.
     *
     * @param drawCardsPacket
     */
    private void handleDrawCardsPacket(@NotNull JavunoPacketOutDrawCards drawCardsPacket) {
        try {
            mvc.getController().onDrawCards(
                drawCardsPacket.getPlayerName(),
                drawCardsPacket.getCardAmount(),
                drawCardsPacket instanceof JavunoPacketOutReceiveCards receiveCardsPacket
                    ? receiveCardsPacket.getReceivedCards()
                    : null,
                drawCardsPacket.isNextTurn()
            );
        } catch (IllegalStateException | IllegalArgumentException e) {
            throw new JavunoBadPacketException(
                String.format("Unable to play card for client: %s", e.getMessage()),
                true
            );
        }
    }

    private void handlePlayCardPacket(@NotNull JavunoPacketOutPlayCard playCardPacket) {
        try {
            mvc.getController().onPlayCard(
                playCardPacket.getPlayerName(),
                playCardPacket.getCardToPlay(),
                playCardPacket.getCardIndex()
            );
        } catch (IllegalStateException e) {
            throw new JavunoBadPacketException(
                String.format("Unable to play card for client: %s", e.getMessage()),
                true
            );
        }
    }

    private void handleGameStartPacket(@NotNull JavunoPacketOutGameStart gameStartPacket) {
        try {
            mvc.getController().onGameStart(
                gameStartPacket.getClientCards(),
                gameStartPacket.getDiscardPile(),
                gameStartPacket.getPlayers(),
                gameStartPacket.getCurrentPlayerIndex(),
                gameStartPacket.getCurrentDirection()
            );
        } catch (IllegalStateException e) {
            throw new JavunoBadPacketException(
                String.format("Unable to start game for client: %s", e.getMessage()),
                true
            );
        }
    }

    private void handleReadyChangedPacket(@NotNull JavunoPacketInOutPlayerReadyChanged readyChangedPacket) {
        try {
            mvc.getController().onPlayerReadyChanged(readyChangedPacket.getPlayerName(), readyChangedPacket.isReady());
        } catch (IllegalStateException | IllegalArgumentException e) {
            throw new JavunoBadPacketException(String.format(
                "Unable to change ready status for %s: %s",
                readyChangedPacket.getPlayerName(),
                e.getMessage()
            ), true);
        }
    }

    private void handleConnectionAccepted(@NotNull JavunoPacketOutConnectionAccepted acceptedPacket) {
        try {
            if (acceptedPacket.isInGame()) {
                JavunoPacketOutGameState gameState = acceptedPacket.getGameState();
                mvc.getController().onJoinGame(
                    acceptedPacket.getPlayerName(),
                    acceptedPacket.getLobbyPlayerNames(),
                    gameState.getClientCards(),
                    gameState.getDiscardPile(),
                    gameState.getPlayers(),
                    gameState.getCurrentPlayerIndex(),
                    gameState.getCurrentDirection(),
                    gameState.getGameState(),
                    gameState.getUnoChallengeState()
                );
            } else mvc.getController().onJoinLobby(
                acceptedPacket.getPlayerName(),
                acceptedPacket.getLobbyPlayerNames(),
                acceptedPacket.getReadyPlayerNames()
            );
        } catch (IllegalStateException e) {
            throw new JavunoBadPacketException(
                String.format("Unable to setup initial state: %s", e.getMessage()),
                true
            );
        }
    }

    private void handleConnectionRejected(@NotNull JavunoPacketOutConnectionRejected rejectedPacket) {
        try {
            mvc.getController().onConnectionRejected(rejectedPacket.getRejectionReason());
        } catch (IllegalStateException e) {
            throw new JavunoBadPacketException(String.format(
                "Unable to handle rejected connection: %s",
                e.getMessage()
            ), true);
        }
    }

    private void handlePlayerConnect(@NotNull JavunoPacketOutPlayerConnect connectPacket) {
        try {
            mvc.getController().onPlayerConnected(connectPacket.getPlayerName());
        } catch (IllegalStateException | IllegalArgumentException e) {
            throw new JavunoBadPacketException(String.format(
                "Unable to handle connecting player %s: %s",
                connectPacket.getPlayerName(),
                e.getMessage()
            ), true);
        }
    }

    private void handlePlayerDisconnect(@NotNull JavunoPacketOutPlayerDisconnect disconnectPacket) {
        try {
            mvc.getController().onPlayerDisconnected(disconnectPacket.getPlayerName());
        } catch (IllegalStateException | IllegalArgumentException e) {
            throw new JavunoBadPacketException(String.format(
                "Unable to handle connecting player %s: %s",
                disconnectPacket.getPlayerName(),
                e.getMessage()
            ), true);
        }
    }
}
