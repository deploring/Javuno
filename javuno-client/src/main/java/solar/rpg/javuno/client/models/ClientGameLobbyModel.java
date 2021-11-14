package solar.rpg.javuno.client.models;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.game.AbstractGameLobbyModel;

import java.util.List;

/**
 * This model stores information about the game lobby that is required by a client.
 * It also stores the client's player name.
 *
 * @author jskinner
 * @since 1.0.0
 */
public class ClientGameLobbyModel extends AbstractGameLobbyModel {

    /**
     * The client player's name.
     */
    @NotNull
    private final String playerName;

    /**
     * Constructs a new {@code ClientGameLobbyModel} instance.
     *
     * @param playerName       The client player's name.
     * @param lobbyPlayerNames Names of all players currently in the lobby.
     * @param readyPlayerNames Name of all players who are marked as ready to play.
     */
    public ClientGameLobbyModel(
            @NotNull String playerName,
            @NotNull List<String> lobbyPlayerNames,
            @NotNull List<String> readyPlayerNames) {
        this.playerName = playerName;
        lobbyPlayerNames.forEach(this::addPlayer);
        readyPlayerNames.forEach(this::markPlayerReady);
    }

    /**
     * Adds a player to the lobby list.
     *
     * @param playerName Player name to add to the lobby list.
     * @throws IllegalArgumentException Player already exists.
     */
    public void addPlayer(@NotNull String playerName) {
        super.addPlayer(playerName);
    }

    /**
     * Removes a player from the lobby list.
     *
     * @param playerName Player to remove from the lobby list.
     * @throws IndexOutOfBoundsException Player does not exist.
     */
    public void removePlayer(@NotNull String playerName) {
        super.removePlayer(getPlayerLobbyIndex(playerName));
    }

    /**
     * @return The client player's name.
     */
    @NotNull
    public String getPlayerName() {
        return playerName;
    }
}
