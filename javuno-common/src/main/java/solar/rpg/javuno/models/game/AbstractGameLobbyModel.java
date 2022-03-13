package solar.rpg.javuno.models.game;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This model stores the JAVUNO game lobby state that is common to both the server and client side.
 *
 * @author jskinner
 * @since 1.0.0
 */
public abstract class AbstractGameLobbyModel {

    /**
     * Names of all players in the lobby. The order is important.
     */
    @NotNull
    private final ArrayList<String> lobbyPlayerNames;
    /**
     * Names of all players who are marked as ready.
     */
    @NotNull
    private final ArrayList<String> readyPlayerNames;
    /**
     * True, if a game is currently running.
     */
    private boolean inGame;

    /**
     * Constructs a new {@code AbstractGameLobbyModel} instance.
     */
    public AbstractGameLobbyModel() {
        lobbyPlayerNames = new ArrayList<>();
        readyPlayerNames = new ArrayList<>();
        inGame = false;
    }

    /* Lobby Player Methods */

    /**
     * @param playerName Name of the player to retrieve the lobby index of.
     * @return The index of the player in the lobby list, or -1 if they cannot be found.
     */
    public int getPlayerLobbyIndex(@NotNull String playerName) {
        return lobbyPlayerNames.indexOf(playerName);
    }

    /**
     * @param playerIndex The player's lobby index.
     * @return Name of the player associated with the given lobby index.
     */
    public String getPlayerName(int playerIndex) {
        return lobbyPlayerNames.get(playerIndex);
    }

    /**
     * @param playerName The player name to check.
     * @return True, if the given player name exists in the lobby list.
     */
    public boolean doesPlayerExist(@NotNull String playerName) {
        return lobbyPlayerNames.contains(playerName);
    }

    /**
     * Adds a player to the lobby list upon connecting to the server.
     *
     * @param playerName Player name to add to the lobby list.
     * @throws IllegalArgumentException Player already exists.
     */
    protected void addPlayer(@NotNull String playerName) {
        if (doesPlayerExist(playerName))
            throw new IllegalArgumentException(String.format("Player %s already exists", playerName));
        lobbyPlayerNames.add(playerName);
    }

    /**
     * Removes a player from the lobby list upon disconnecting from the server.
     *
     * @param playerIndex Lobby index of the player to remove.
     */
    protected void removePlayer(int playerIndex) {
        String playerName = lobbyPlayerNames.get(playerIndex);
        lobbyPlayerNames.remove(playerIndex);
        readyPlayerNames.remove(playerName);
    }

    /**
     * @return Unmodifiable view of the names of all players in the lobby.
     */
    @NotNull
    public List<String> getLobbyPlayerNames() {
        return Collections.unmodifiableList(lobbyPlayerNames);
    }

    /* Ready Player Methods */

    /**
     * @return True, if there are at least 2 players in the lobby marked as ready.
     */
    public boolean canStart() {
        return readyPlayerNames.size() > 1;
    }

    /**
     * @param playerName Name of the player to check.
     * @return True, if the given player is marked as ready.
     * @throws JavunoStateException Player does not exist.
     */
    public boolean isPlayerReady(@NotNull String playerName) {
        if (!doesPlayerExist(playerName))
            throw new JavunoStateException(String.format("Player %s does not exist", playerName));
        return readyPlayerNames.contains(playerName);
    }

    /**
     * Marks a player as ready to play in the lobby.
     *
     * @param playerName Name of the player to mark as ready.
     * @throws JavunoStateException Player does not exist.
     * @throws JavunoStateException Player is already marked as ready.
     */
    public void markPlayerReady(@NotNull String playerName) {
        if (!doesPlayerExist(playerName))
            throw new JavunoStateException(String.format("Player %s does not exist", playerName));
        if (isPlayerReady(playerName))
            throw new JavunoStateException(String.format("Player %s is already marked as ready", playerName));
        readyPlayerNames.add(playerName);
    }

    /**
     * Marks a ready player as not ready to play in the lobby.
     *
     * @param playerName Name of the player who is marked as ready.
     * @throws JavunoStateException Player does not exist.
     * @throws JavunoStateException Player is not marked as ready.
     */
    public void unmarkPlayerReady(@NotNull String playerName) {
        if (!isPlayerReady(playerName))
            throw new JavunoStateException(String.format("Player %s is not marked as ready", playerName));
        readyPlayerNames.remove(playerName);
    }

    /**
     * @return Copy of ready player names list.
     */
    @NotNull
    public List<String> getReadyPlayerNames() {
        return new ArrayList<>(readyPlayerNames);
    }

    /**
     * @return True, if a game is currently running.
     */
    public boolean isInGame() {
        return inGame;
    }

    /**
     * Sets whether a game is currently running.
     *
     * @param inGame True, if a game is currently running.
     */
    public void setInGame(boolean inGame) {
        this.inGame = inGame;
    }
}
