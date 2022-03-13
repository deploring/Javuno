package solar.rpg.javuno.models.packets.out;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import solar.rpg.javuno.models.game.JavunoStateException;
import solar.rpg.jserver.packet.JServerPacket;

import java.util.List;

/**
 * This packet is sent out from the server once it has accepted a connection request from a client.
 *
 * @author jskinner
 * @since 1.0.0
 */
public class JavunoPacketOutConnectionAccepted extends JServerPacket {

    /**
     * Confirmed name of the player.
     */
    @NotNull
    private final String playerName;
    /**
     * Names of all players in the lobby. The order is important.
     */
    @NotNull
    private final List<String> lobbyPlayerNames;
    /**
     * Names of all players who are marked as ready. {@code null} if a game has already started.
     */
    @Nullable
    private final List<String> readyPlayerNames;
    /**
     * State of the currently running UNO game. {@code} null if a game is not running.
     */
    @Nullable
    private final JavunoPacketOutGameState gameState;

    /**
     * Constructs a new {@code JavunoPacketOutConnectionAcceptedLobby} instance.
     *
     * @param playerName       Confirmed name of the player.
     * @param lobbyPlayerNames Names of all players in the lobby. The order is important.
     * @param readyPlayerNames Names of all players who are marked as ready (if game is not running).
     * @param gameState        State of the currently running UNO game (if game is running).
     */
    public JavunoPacketOutConnectionAccepted(
        @NotNull String playerName, @NotNull List<String> lobbyPlayerNames,
        @Nullable List<String> readyPlayerNames,
        @Nullable JavunoPacketOutGameState gameState) {
        this.playerName = playerName;
        if ((readyPlayerNames == null) == (gameState == null))
            throw new IllegalArgumentException("Either ready player names list or game state must be provided");
        this.lobbyPlayerNames = lobbyPlayerNames;
        this.readyPlayerNames = readyPlayerNames;
        this.gameState = gameState;
    }

    /**
     * @return Confirmed name of the player.
     */
    @NotNull
    public String getPlayerName() {
        return playerName;
    }

    /**
     * @return Names of all players in the lobby. The order is important.
     */
    @NotNull
    public List<String> getLobbyPlayerNames() {
        return lobbyPlayerNames;
    }

    /**
     * @return True, if a game is currently running.
     */
    public boolean isInGame() {
        return gameState != null;
    }

    /**
     * @return Names of all players who are marked as ready.
     * @throws JavunoStateException Game is already running.
     */
    @NotNull
    public List<String> getReadyPlayerNames() {
        if (readyPlayerNames == null) throw new JavunoStateException("Game is already running");
        return readyPlayerNames;
    }

    /**
     * @return The state of the current game.
     * @throws JavunoStateException Game is not running.
     */
    @NotNull
    public JavunoPacketOutGameState getGameState() {
        if (gameState == null) throw new JavunoStateException("Game is not running");
        return gameState;
    }
}
