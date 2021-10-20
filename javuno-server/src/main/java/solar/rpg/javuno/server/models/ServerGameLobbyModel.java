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
        assert playerOriginAddresses.indexOf(originAddress) ==
               lobbyPlayerNames.indexOf(playerName) : "Player index mismatch";
    }

    public void removePlayer(@NotNull InetSocketAddress originAddress) {
        int playerIndex = playerOriginAddresses.indexOf(originAddress);
        assert playerIndex != -1 : String.format("Could not find player associated with %s", originAddress);

        playerOriginAddresses.remove(playerIndex);
        super.removePlayer(playerIndex);
    }

    public String getPlayerName(@NotNull InetSocketAddress originAddress) {
        int playerIndex = playerOriginAddresses.indexOf(originAddress);
        assert playerIndex != -1 : String.format("Could not find player associated with %s", originAddress);

        return lobbyPlayerNames.get(playerIndex);
    }
}
