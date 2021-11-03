package solar.rpg.javuno.server.models;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import solar.rpg.javuno.models.game.AbstractGameLobbyModel;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * This model extends the {@link AbstractGameLobbyModel} class, and stores origin address information about players in
 * the lobby. This is done so the controller can send packets to any given player client as needed.
 *
 * @author jskinner
 * @since 1.0.0
 */
public class ServerGameLobbyModel extends AbstractGameLobbyModel {

    /**
     * List of the origin addresses of all players in the lobby. The order matches the lobby list.
     */
    @NotNull
    private final ArrayList<InetSocketAddress> playerOriginAddresses;

    /**
     * Constructs a new, empty {@code ServerGameLobbyModel}.
     */
    public ServerGameLobbyModel() {
        super();
        playerOriginAddresses = new ArrayList<>();
    }

    /**
     * @param originAddress The origin address to check.
     * @return True, if the given origin address is associated with a player in the lobby list.
     */
    public boolean doesPlayerExist(@NotNull InetSocketAddress originAddress) {
        return playerOriginAddresses.contains(originAddress);
    }

    /**
     * Adds a new player name to the lobby list and associates it with an origin address.
     *
     * @param playerName    The new player name.
     * @param originAddress The player's associated origin address.
     * @throws IllegalArgumentException Origin address already exists.
     * @throws IllegalStateException    Index of origin address does not match index of player.
     */
    public void addPlayer(@NotNull String playerName, @NotNull InetSocketAddress originAddress) {
        if (playerOriginAddresses.contains(originAddress))
            throw new IllegalArgumentException(String.format("Origin address %s already registered", originAddress));
        super.addPlayer(playerName);
        playerOriginAddresses.add(originAddress);
        if (playerOriginAddresses.indexOf(originAddress) != getPlayerLobbyIndex(playerName))
            throw new IllegalStateException("Player index mismatch");
    }

    /**
     * Removes a player from the lobby list associated with an origin address.
     *
     * @param originAddress The origin address of the player to remove.
     * @throws IllegalArgumentException Origin address does not exist.
     */
    public void removePlayer(@NotNull InetSocketAddress originAddress) {
        int playerIndex = getPlayerLobbyIndex(originAddress);
        playerOriginAddresses.remove(playerIndex);
        super.removePlayer(playerIndex);
    }

    /**
     * @param playerName The player name to retrieve the origin address from.
     * @return The origin address of the given player in the lobby list.
     * @throws IllegalArgumentException Player name does not exist.
     */
    @NotNull
    public InetSocketAddress getOriginAddress(@NotNull String playerName) {
        return playerOriginAddresses.get(getPlayerLobbyIndex(playerName));
    }

    /**
     * @param originAddress The origin address to retrieve the player name from.
     * @return The name of the player associated with the given origin address.
     * @throws IllegalArgumentException Origin address does not exist.
     */
    @NotNull
    public String getPlayerName(@NotNull InetSocketAddress originAddress) {
        return getPlayerName(getPlayerLobbyIndex(originAddress));
    }

    /**
     * @param originAddress The origin address to retrieve the player name from.
     * @param theDefault    Value to return if the given origin address does not exist.
     * @return The name of the player associated with the given origin address, otherwise the default value.
     */
    @NotNull
    public String getPlayerNameWithDefault(@NotNull InetSocketAddress originAddress, @NotNull String theDefault) {
        if (doesPlayerExist(originAddress)) return getPlayerName(originAddress);
        else return theDefault;
    }

    /**
     * @param originAddress The origin address to retrieve the lobby index of the player from.
     * @return The lobby index of the player.
     * @throws IllegalArgumentException Origin address does not exist.
     */
    public int getPlayerLobbyIndex(@NotNull InetSocketAddress originAddress) {
        int result = playerOriginAddresses.indexOf(originAddress);
        if (result == -1)
            throw new IllegalArgumentException(String.format("Could not find player associated with %s",
                                                             originAddress));
        return result;
    }
}
