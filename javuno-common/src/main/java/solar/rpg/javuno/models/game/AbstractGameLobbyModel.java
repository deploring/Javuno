package solar.rpg.javuno.models.game;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractGameLobbyModel {

    @NotNull
    private final ArrayList<String> lobbyPlayerNames;

    public AbstractGameLobbyModel() {
        lobbyPlayerNames = new ArrayList<>();
    }

    public int getPlayerLobbyIndex(@NotNull String playerName) {
        return lobbyPlayerNames.indexOf(playerName);
    }

    public String getPlayerName(int playerIndex) {
        return lobbyPlayerNames.get(playerIndex);
    }

    public boolean doesPlayerExist(@NotNull String playerName) {
        return lobbyPlayerNames.contains(playerName);
    }

    protected void addPlayer(@NotNull String playerName) {
        if (doesPlayerExist(playerName))
            throw new IllegalArgumentException(String.format("Player %s already exists", playerName));
        lobbyPlayerNames.add(playerName);
    }

    protected void removePlayer(int playerIndex) {
        lobbyPlayerNames.remove(playerIndex);
    }

    @NotNull
    public List<String> getLobbyPlayerNames() {
        return Collections.unmodifiableList(lobbyPlayerNames);
    }
}
