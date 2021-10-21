package solar.rpg.javuno.server.models;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.game.AbstractGameLobbyModel;

import java.net.InetSocketAddress;
import java.util.ArrayList;

public class ServerGameLobbyModel extends AbstractGameLobbyModel {

    @NotNull
    private final ArrayList<InetSocketAddress> playerOriginAddresses;

    public ServerGameLobbyModel() {
        super();
        playerOriginAddresses = new ArrayList<>();
    }

    public boolean doesPlayerExist(@NotNull InetSocketAddress originAddress) {
        return playerOriginAddresses.contains(originAddress);
    }

    public void addPlayer(@NotNull String playerName, @NotNull InetSocketAddress originAddress) {
        if (playerOriginAddresses.contains(originAddress))
            throw new IllegalArgumentException(String.format("Origin address %s already registered", originAddress));
        super.addPlayer(playerName);
        playerOriginAddresses.add(originAddress);
        if (playerOriginAddresses.indexOf(originAddress) != getPlayerLobbyIndex(playerName))
            throw new IllegalStateException("Player index mismatch");
    }

    public void removePlayer(@NotNull InetSocketAddress originAddress) {
        int playerIndex = getPlayerLobbyIndex(originAddress);
        playerOriginAddresses.remove(playerIndex);
        super.removePlayer(playerIndex);
    }

    public String getPlayerName(@NotNull InetSocketAddress originAddress) {
        return getPlayerName(getPlayerLobbyIndex(originAddress));
    }

    public String getPlayerNameWithDefault(@NotNull InetSocketAddress originAddress, @NotNull String theDefault) {
        if (doesPlayerExist(originAddress)) return getPlayerName(originAddress);
        else return theDefault;
    }

    public int getPlayerLobbyIndex(@NotNull InetSocketAddress originAddress) {
        int result = playerOriginAddresses.indexOf(originAddress);
        if (result == -1)
            throw new IllegalArgumentException(String.format("Could not find player associated with %s",
                                                             originAddress));
        return result;
    }
}
