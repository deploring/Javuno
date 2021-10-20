package solar.rpg.javuno.models.game;

import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class AbstractGameLobbyModel {

    @NotNull
    private final ArrayList<String> playerNames;

    public AbstractGameLobbyModel() {
        playerNames = new ArrayList<>();
    }

    public boolean doesPlayerExist(@NotNull String playerName) {
        return playerNames.contains(playerName);
    }

    public int getPlayerIndex(@NotNull String playerName) {
        return playerNames.indexOf(playerName);
    }

    protected void addPlayer(@NotNull String playerName) {
        if (doesPlayerExist(playerName))
            throw new IllegalArgumentException(String.format("Player %s already exists", playerName));
        playerNames.add(playerName);
    }

    protected void removePlayer(int playerIndex) {
        playerNames.remove(playerIndex);
    }
}
