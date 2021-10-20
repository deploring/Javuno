package solar.rpg.javuno.server.models;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.game.AbstractGameLobbyModel;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ServerGameLobbyModel extends AbstractGameLobbyModel {

    @NotNull
    private final ArrayList<InetSocketAddress> playerOriginAddresses;

    public ServerGameLobbyModel() {
        super();
        playerOriginAddresses = new ArrayList<>();
    }

    public void addPlayer(@NotNull String playerName, @NotNull InetSocketAddress originAddress) {
        super.addPlayer(playerName);
        assert !playerOriginAddresses.contains(originAddress) :
                String.format("Origin address %s already registered", originAddress);
        playerOriginAddresses.add(originAddress);
        assert playerOriginAddresses.indexOf(originAddress) == getPlayerIndex(playerName) : "Player index mismatch";
    }

    public void removePlayer(@NotNull String playerName) {
        int playerIndex = getPlayerIndex(playerName);
        if (playerIndex == -1)
            throw new IllegalArgumentException(String.format("Player %s does not exist", playerName));

        playerOriginAddresses.remove(playerIndex);
        super.removePlayer(playerIndex);
    }

    public List<InetSocketAddress> getAddresses() {
        return Collections.unmodifiableList(playerOriginAddresses);
    }

    public List<InetSocketAddress> getAddressesExcept(InetSocketAddress addressToExclude) {
        return playerOriginAddresses.stream().filter(
                (address) -> !address.equals(addressToExclude)).collect(Collectors.toList());
    }
}
