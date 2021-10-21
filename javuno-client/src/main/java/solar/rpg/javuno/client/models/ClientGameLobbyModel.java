package solar.rpg.javuno.client.models;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.game.AbstractGameLobbyModel;

import java.util.List;

public class ClientGameLobbyModel extends AbstractGameLobbyModel {

    public ClientGameLobbyModel(@NotNull List<String> existingPlayerNames) {
        existingPlayerNames.forEach(this::addPlayer);
    }

    public void addPlayer(@NotNull String playerName) {
        super.addPlayer(playerName);
    }

    public void removePlayer(@NotNull String playerName) {
        int playerIndex = getPlayerLobbyIndex(playerName);
        assert playerIndex != -1 : String.format("Could not find player %s", playerName);
        super.removePlayer(playerIndex);
    }
}
