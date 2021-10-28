package solar.rpg.javuno.models.game;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractGameLobbyModel {

    @NotNull
    private final ArrayList<String> lobbyPlayerNames;
    @NotNull
    private final ArrayList<String> readyPlayerNames;

    public AbstractGameLobbyModel() {
        lobbyPlayerNames = new ArrayList<>();
        readyPlayerNames = new ArrayList<>();
    }

    /* Lobby Player Methods */

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
        String playerName = lobbyPlayerNames.get(playerIndex);
        lobbyPlayerNames.remove(playerIndex);
        readyPlayerNames.remove(playerName);
    }

    @NotNull
    public List<String> getLobbyPlayerNames() {
        return Collections.unmodifiableList(lobbyPlayerNames);
    }

    @NotNull
    public List<String> getReadyPlayerNames() {
        return Collections.unmodifiableList(readyPlayerNames);
    }

    /* Ready Player Methods */

    public boolean isPlayerReady(@NotNull String playerName) {
        return readyPlayerNames.contains(playerName);
    }

    public void markPlayerReady(@NotNull String playerName) {
        if (isPlayerReady(playerName))
            throw new IllegalArgumentException(String.format("Player %s is currently ready", playerName));
        readyPlayerNames.add(playerName);
    }

    public void unmarkPlayerReady(@NotNull String playerName) {
        if (!isPlayerReady(playerName))
            throw new IllegalArgumentException(String.format("Player %s is currently not ready", playerName));
        readyPlayerNames.remove(playerName);
    }
}
