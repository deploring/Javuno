package solar.rpg.javuno.client.controller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import solar.rpg.javuno.client.controller.ConnectionController.JavunoClientConnection;
import solar.rpg.javuno.client.models.ClientGameLobbyModel;
import solar.rpg.javuno.client.models.ClientGameModel;
import solar.rpg.javuno.client.mvc.JavunoClientMVC;
import solar.rpg.javuno.client.views.ViewGame;
import solar.rpg.javuno.models.cards.ICard;
import solar.rpg.javuno.models.game.Direction;
import solar.rpg.javuno.models.packets.in.JavunoPacketInOutPlayerReadyChanged;
import solar.rpg.javuno.models.packets.out.JavunoPacketOutConnectionRejected.ConnectionRejectionReason;
import solar.rpg.javuno.mvc.IController;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
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
    @Nullable
    private ClientGameModel gameModel;
    @NotNull
    private final JavunoClientPacketHandler packetHandler;

    public ClientGameController(@NotNull Logger logger) {
        this.logger = logger;
        mvc = new JavunoClientMVC<>();
        packetHandler = new JavunoClientPacketHandler(mvc, logger);
    }

    /* Outgoing View Events (called by views) */

    public void markSelfReady() {
        getClientConnection().writePacket(new JavunoPacketInOutPlayerReadyChanged(true));
    }

    public void unmarkSelfReady() {
        getClientConnection().writePacket(new JavunoPacketInOutPlayerReadyChanged(false));
    }

    /* Server Events */

    public void onGameStart(
            @Nullable List<ICard> clientCards,
            @NotNull Stack<ICard> discardPile,
            @NotNull List<Integer> playerCardCounts,
            @NotNull List<String> gamePlayerNames,
            int currentPlayerIndex,
            @NotNull Direction currentDirection) {
        if (gameModel != null) throw new IllegalStateException("Game already exists");
        getGameLobbyModel().setInGame(true);
        setGameModel(clientCards, discardPile, playerCardCounts, gamePlayerNames, currentPlayerIndex, currentDirection);
        SwingUtilities.invokeLater(() -> {
            mvc.logClientEvent(String.format(
                    "> The game has started! There are %d players and %s will go first. The starting card is a %s.",
                    gamePlayerNames.size(),
                    getGameModel().getCurrentPlayerName(),
                    getGameModel().getLastPlayedCard().getDescription()));
            mvc.getViewInformation().refreshPlayerTable();
            mvc.getView().onGameStart();
        });
    }

    public void onPlayerReadyChanged(@NotNull String playerName, boolean isReady) {
        boolean couldStart = getGameLobbyModel().canStart();
        if (isReady) getGameLobbyModel().markPlayerReady(playerName);
        else getGameLobbyModel().unmarkPlayerReady(playerName);
        boolean canStart = getGameLobbyModel().canStart();

        SwingUtilities.invokeLater(() -> mvc.getView().onPlayerReadyChanged(playerName,
                                                                            isReady,
                                                                            couldStart != canStart));
    }

    public void onPlayerConnected(@NotNull String playerName) {
        getGameLobbyModel().addPlayer(playerName);
        SwingUtilities.invokeLater(() -> {
            mvc.logClientEvent(String.format("> %s has connected.", playerName));
            mvc.getViewInformation().refreshPlayerTable();
        });
    }

    public void onPlayerDisconnected(@NotNull String playerName) {
        getGameLobbyModel().removePlayer(playerName);
        SwingUtilities.invokeLater(() -> {
            mvc.logClientEvent(String.format("> %s has disconnected.", playerName));
            mvc.getViewInformation().refreshPlayerTable();
        });
    }

    public void onJoinLobby(
            @NotNull String playerName,
            @NotNull List<String> lobbyPlayerNames,
            @NotNull List<String> readyPlayerNames) {
        setGameLobbyModel(playerName, lobbyPlayerNames, readyPlayerNames);
        SwingUtilities.invokeLater(() -> mvc.getAppController().getMVC().getView().onConnected());
    }

    public void onJoinGame(
            @NotNull String playerName,
            @NotNull List<String> lobbyPlayerNames,
            @Nullable List<ICard> clientCards,
            @NotNull Stack<ICard> discardPile,
            @NotNull List<Integer> playerCardCounts,
            @NotNull List<String> gamePlayerNames,
            int currentPlayerIndex,
            @NotNull Direction currentDirection) {
        setGameLobbyModel(playerName, lobbyPlayerNames, new ArrayList<>());
        getGameLobbyModel().setInGame(true);
        setGameModel(clientCards, discardPile, playerCardCounts, gamePlayerNames, currentPlayerIndex, currentDirection);
        SwingUtilities.invokeLater(() -> {
            mvc.getAppController().getMVC().getView().onConnected();
            mvc.logClientEvent(String.format(
                    "> It is currently %s's turn. The current card is a %s.",
                    getGameModel().getCurrentPlayerName(),
                    getGameModel().getLastPlayedCard().getDescription()));
        });
    }

    public void onConnectionRejected(@NotNull ConnectionRejectionReason reason) {
        mvc.getAppController().getConnectionController().onConnectionRejected();
        SwingUtilities.invokeLater(() -> {
            String errorMsg = "";
            switch (reason) {
                case INCORRECT_PASSWORD -> errorMsg = "Incorrect server password.";
                case USERNAME_ALREADY_TAKEN -> errorMsg = "That username is already taken.";
            }

            if (!errorMsg.isEmpty()) {
                mvc.logClientEvent(String.format("> %s", errorMsg));
                mvc.getView().showErrorDialog("Unable to connect to server", errorMsg);
            }
        });
    }

    public void onDisconnected() {
        lobbyModel = null;
        gameModel = null;
    }

    /* Attribute Getters/Setters */

    public boolean isCurrentPlayer() {
        return getGameModel().isCurrentPlayer(getPlayerName());
    }

    @NotNull
    public ClientGameModel getGameModel() {
        if (gameModel == null) throw new IllegalStateException("Game model does not exist");
        return gameModel;
    }

    public void setGameModel(
            @Nullable List<ICard> clientCards,
            @NotNull Stack<ICard> discardPile,
            @NotNull List<Integer> playerCardCounts,
            @NotNull List<String> gamePlayerNames,
            int currentPlayerIndex,
            @NotNull Direction currentDirection) {
        if (gameModel != null) throw new IllegalStateException("Game model already exists");
        gameModel = new ClientGameModel(clientCards,
                                        discardPile,
                                        playerCardCounts,
                                        gamePlayerNames,
                                        currentPlayerIndex,
                                        currentDirection);
    }

    @NotNull
    public ClientGameLobbyModel getGameLobbyModel() {
        if (lobbyModel == null) throw new IllegalStateException("Game lobby model does not exist");
        return lobbyModel;
    }

    public void setGameLobbyModel(
            @NotNull String playerName,
            @NotNull List<String> lobbyPlayerNames,
            @NotNull List<String> readyPlayerNames) {
        if (lobbyModel != null) throw new IllegalStateException("Game lobby model already exists");
        mvc.getAppController().getConnectionController().onConnectionAccepted();
        lobbyModel = new ClientGameLobbyModel(playerName, lobbyPlayerNames, readyPlayerNames);
    }

    @NotNull
    public String getPlayerName() {
        return getGameLobbyModel().getPlayerName();
    }

    @NotNull
    public String getPlayerStatus(@NotNull String playerName) {
        if (getGameLobbyModel().isInGame()) {
            if (getGameModel().doesPlayerExist(playerName)) {
                int cardAmount = getGameModel().getCardAmount(getGameModel().getPlayerIndex(playerName));
                return String.format("In Game (%d card%s)", cardAmount, cardAmount == 1 ? "" : "s");
            } else return "Spectating";
        } else return getGameLobbyModel().isPlayerReady(playerName) ? "Ready" : "Waiting";
    }

    @NotNull
    public JavunoClientPacketHandler getPacketHandler() {
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
