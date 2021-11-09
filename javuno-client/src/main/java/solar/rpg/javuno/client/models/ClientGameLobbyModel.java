package solar.rpg.javuno.client.models;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.game.AbstractGameLobbyModel;

import java.util.List;

public class ClientGameLobbyModel extends AbstractGameLobbyModel {

    @NotNull
    private final String playerName;

    public ClientGameLobbyModel(
            @NotNull String playerName,
            @NotNull List<String> lobbyPlayerNames,
            @NotNull List<String> readyPlayerNames) {
        this.playerName = playerName;
        lobbyPlayerNames.forEach(this::addPlayer);
        readyPlayerNames.forEach(this::markPlayerReady);
    }

    public void addPlayer(@NotNull String playerName) {
        super.addPlayer(playerName);
    }

    public void removePlayer(@NotNull String playerName) {
        int playerIndex = getPlayerLobbyIndex(playerName);
        assert playerIndex != -1 : String.format("Could not find player %s", playerName);
        super.removePlayer(playerIndex);
    }

    @NotNull
    public String getPlayerName() {
        return playerName;
    }
}
