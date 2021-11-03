package solar.rpg.javuno.client.controller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import solar.rpg.javuno.client.controller.ConnectionController.JavunoClientConnection;
import solar.rpg.javuno.client.models.ClientGameLobbyModel;
import solar.rpg.javuno.client.mvc.JavunoClientMVC;
import solar.rpg.javuno.client.views.ViewGame;
import solar.rpg.javuno.models.packets.in.JavunoPacketInOutPlayerReadyChanged;
import solar.rpg.javuno.mvc.IController;

import java.util.List;
import java.util.logging.Logger;

public class ClientGameController implements IController {

    @NotNull
    private final Logger logger;
    @NotNull
    private final JavunoClientMVC<ViewGame, ClientGameController> mvc;
    @Nullable
    private String playerName;
    @Nullable
    private ClientGameLobbyModel lobbyModel;
    //@Nullable
    //private ClientGameModel gameModel;
    @NotNull
    private final JavunoClientPacketValidatorHandler packetHandler;

    public ClientGameController(@NotNull Logger logger) {
        this.logger = logger;
        mvc = new JavunoClientMVC<>();
        packetHandler = new JavunoClientPacketValidatorHandler(mvc, logger);
    }

    /* Outgoing View Events (called by views) */

    public void markSelfReady() {
        getClientConnection().writePacket(new JavunoPacketInOutPlayerReadyChanged(true));
    }

    public void unmarkSelfReady() {
        getClientConnection().writePacket(new JavunoPacketInOutPlayerReadyChanged(false));
    }

    /* Field Getters/Setters */

    public void setGameLobbyModel(@NotNull List<String> existingPlayerNames, @NotNull List<String> readyPlayerNames) {
        if (lobbyModel != null) throw new IllegalStateException("Game lobby model already exists");
        lobbyModel = new ClientGameLobbyModel(existingPlayerNames, readyPlayerNames);
    }

    @NotNull
    public ClientGameLobbyModel getGameLobbyModel() {
        if (lobbyModel == null) throw new IllegalStateException("Game lobby model does not exist");
        return lobbyModel;
    }

    @NotNull
    public String getPlayerName() {
        if (playerName == null) throw new IllegalStateException("Player name not set");
        return playerName;
    }

    public void setPlayerName(@Nullable String playerName) {
        this.playerName = playerName;
    }

    @NotNull
    public String getPlayerStatus(@NotNull String playerName) {
        return getGameLobbyModel().isPlayerReady(playerName) ? "Ready" : "Waiting";
    }

    @NotNull
    public JavunoClientPacketValidatorHandler getPacketHandler() {
        return packetHandler;
    }

    /* MVC */

    @NotNull
    private JavunoClientConnection getClientConnection() {
        return mvc.getAppController().getConnectionController().getClientConnection();
    }

    @Override
    @NotNull
    public JavunoClientMVC<ViewGame, ClientGameController> getMVC() {
        return mvc;
    }
}
