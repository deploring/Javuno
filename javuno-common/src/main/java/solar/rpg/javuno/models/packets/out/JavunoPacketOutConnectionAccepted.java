package solar.rpg.javuno.models.packets.out;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import solar.rpg.jserver.packet.JServerPacket;

import java.util.List;

/**
 * This packet is sent out by the server once it has accepted a connection request from a client.
 *
 * @author jskinner
 * @since 1.0.0
 */
public class JavunoPacketOutConnectionAccepted extends JServerPacket {

    /**
     * The name of this player.
     */
    @NotNull
    private final String playerName;
    /**
     * The names of all players in the lobby (order matters here).
     */
    @NotNull
    private final List<String> lobbyPlayerNames;
    /**
     * The names of all players in the lobby that are marked as ready.
     * {@code null} if a game has already started.
     */
    @Nullable
    private final List<String> readyPlayerNames;
    /**
     * The state of the current game.
     * {@code} null if a game is not running.
     */
    @Nullable
    private final JavunoPacketOutGameState gameState;

    /**
     * Constructs a new {@code JavunoPacketOutConnectionAcceptedLobby} instance.
     *
     * @param playerName       The name of this player.
     * @param lobbyPlayerNames The names of all players in the lobby (order matters here).
     * @param readyPlayerNames The names of all players in the lobby that are marked as ready (if game is not running).
     * @param gameState        The state of the current game (if game is running).
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
     * @return The name of this player.
     */
    @NotNull
    public String getPlayerName() {
        return playerName;
    }

    /**
     * @return The names of all players in the lobby (order matters here).
     */
    @NotNull
    public List<String> getLobbyPlayerNames() {
        return lobbyPlayerNames;
    }

    /**
     * @return True, if a game is currently running on the server.
     */
    public boolean isInGame() {
        return gameState != null;
    }

    /**
     * @return The names of all players in the lobby that are marked as ready.
     */
    @NotNull
    public List<String> getReadyPlayerNames() {
        if (readyPlayerNames == null) throw new IllegalStateException("Game is already running");
        return readyPlayerNames;
    }

    /**
     * @return The state of the current game.
     */
    @NotNull
    public JavunoPacketOutGameState getGameState() {
        if (gameState == null) throw new IllegalStateException("Game is not running");
        return gameState;
    }
}
