package solar.rpg.javuno.client.controller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import solar.rpg.javuno.client.controller.ConnectionController.JavunoClientConnection;
import solar.rpg.javuno.client.models.ClientGameLobbyModel;
import solar.rpg.javuno.client.models.ClientGameModel;
import solar.rpg.javuno.client.mvc.JavunoClientMVC;
import solar.rpg.javuno.client.views.ViewGame;
import solar.rpg.javuno.models.cards.ICard;
import solar.rpg.javuno.models.game.ClientGamePlayer;
import solar.rpg.javuno.models.game.Direction;
import solar.rpg.javuno.models.packets.in.JavunoPacketInOutPlayerReadyChanged;
import solar.rpg.javuno.models.packets.out.JavunoPacketOutConnectionRejected.ConnectionRejectionReason;
import solar.rpg.javuno.mvc.IController;
import solar.rpg.javuno.mvc.IView;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Logger;

/**
 * Handles all state and manipulation related to being connected to a Javuno server. This includes both a lobby model
 * and a game model. Incoming and outgoing server packets are handled by this controller.
 *
 * @author jskinner
 * @since 1.0.0
 */
public class ClientGameController implements IController {

    /**
     * Logging object.
     */
    @NotNull
    private final Logger logger;
    /**
     * MVC object related to this controller.
     */
    @NotNull
    private final JavunoClientMVC<ViewGame, ClientGameController> mvc;
    /**
     * Stores information about players in the lobby.
     */
    @Nullable
    private ClientGameLobbyModel lobbyModel;
    /**
     * Stores running game data.
     */
    @Nullable
    private ClientGameModel gameModel;
    /**
     * Handles all incoming packets and calls the appropriate controller methods.
     */
    @NotNull
    private final JavunoClientPacketHandler packetHandler;

    /**
     * Constructs a new {@code ClientGameController} instance.
     *
     * @param logger Logging object.
     */
    public ClientGameController(@NotNull Logger logger) {
        this.logger = logger;
        mvc = new JavunoClientMVC<>();
        packetHandler = new JavunoClientPacketHandler(mvc, logger);
    }

    /* Outgoing View Events (called by views) */

    /**
     * Called when this client player marks themselves as ready.
     */
    public void markSelfReady() {
        getClientConnection().writePacket(new JavunoPacketInOutPlayerReadyChanged(true));
    }

    /**
     * Called when this client player marks themselves as not ready.
     */
    public void unmarkSelfReady() {
        getClientConnection().writePacket(new JavunoPacketInOutPlayerReadyChanged(false));
    }

    /* Server Events (called by incoming server packets) */

    /**
     * Called when the server has started the game.
     *
     * @param clientCards        The client player's starting cards (or null if they are spectating).
     * @param discardPile        The discard pile, including the first card drawn from the deck.
     * @param players            List of participating player objects (the order matters here).
     * @param currentPlayerIndex Index of the current player (who will play the first card).
     * @param currentDirection   The current direction of play.
     */
    public void onGameStart(
            @Nullable List<ICard> clientCards,
            @NotNull Stack<ICard> discardPile,
            @NotNull List<ClientGamePlayer> players,
            int currentPlayerIndex,
            @NotNull Direction currentDirection) {
        if (gameModel != null) throw new IllegalStateException("Game already exists");
        getGameLobbyModel().setInGame(true);
        setGameModel(clientCards, discardPile, players, currentPlayerIndex, currentDirection);
        IView.invoke(() -> {
            mvc.logClientEvent(String.format(
                    "> The game has started! There are %d players and %s will go first. The starting card is a %s.",
                    players.size(),
                    getGameModel().getCurrentPlayerName(),
                    getGameModel().getLastPlayedCard().getDescription()));
            mvc.getViewInformation().refreshPlayerTable();
            mvc.getView().onGameStart();
        }, logger);
    }

    /**
     * Called by the server when a player changes their "ready to play" state.
     *
     * @param playerName The name of the player who changed their state.
     * @param isReady    True, if the player is now marked as ready to play.
     */
    public void onPlayerReadyChanged(@NotNull String playerName, boolean isReady) {
        boolean couldStart = getGameLobbyModel().canStart();
        if (isReady) getGameLobbyModel().markPlayerReady(playerName);
        else getGameLobbyModel().unmarkPlayerReady(playerName);
        boolean canStart = getGameLobbyModel().canStart();

        IView.invoke(() -> mvc.getView().onPlayerReadyChanged(playerName, isReady, couldStart != canStart),
                     logger);
    }

    /**
     * Called by the server when a player connects to the lobby.
     *
     * @param playerName The name of the player who connected.
     */
    public void onPlayerConnected(@NotNull String playerName) {
        getGameLobbyModel().addPlayer(playerName);
        IView.invoke(() -> {
                         mvc.logClientEvent(String.format("> %s has connected.", playerName));
                         mvc.getViewInformation().refreshPlayerTable();
                     },
                     logger);
    }

    /**
     * Called by the server when a player disconnects from the lobby.,
     *
     * @param playerName The name of the player who disconnected.
     */
    public void onPlayerDisconnected(@NotNull String playerName) {
        getGameLobbyModel().removePlayer(playerName);
        IView.invoke(() -> {
                         mvc.logClientEvent(String.format("> %s has disconnected.", playerName));
                         mvc.getViewInformation().refreshPlayerTable();
                     },
                     logger);
    }

    /**
     * Called by the server when the client player joins a lobby (no game is running).
     *
     * @param playerName       Name of the client player.
     * @param lobbyPlayerNames The names of all players in the lobby (the order matters here).
     * @param readyPlayerNames The names of all players who are marked as ready.
     */
    public void onJoinLobby(
            @NotNull String playerName,
            @NotNull List<String> lobbyPlayerNames,
            @NotNull List<String> readyPlayerNames) {
        setGameLobbyModel(playerName, lobbyPlayerNames, readyPlayerNames);
        IView.invoke(() -> mvc.getAppController().getMVC().getView().onConnected(), logger);
    }

    /**
     * Called by the server when the client joins an existing game.
     *
     * @param playerName         Name of the client player.
     * @param lobbyPlayerNames   Names of all players currently in the lobby (the order matters here)>.
     * @param clientCards        The client player's current cards (or null if they are spectating).
     * @param discardPile        The discard pile, including all previously discarded cards.
     * @param players            List of participating player objects (the order matters here).
     * @param currentPlayerIndex Index of the current player (who will play the next card).
     * @param currentDirection   The current direction of play.
     */
    public void onJoinGame(
            @NotNull String playerName,
            @NotNull List<String> lobbyPlayerNames,
            @Nullable List<ICard> clientCards,
            @NotNull Stack<ICard> discardPile,
            @NotNull List<ClientGamePlayer> players,
            int currentPlayerIndex,
            @NotNull Direction currentDirection) {
        setGameLobbyModel(playerName, lobbyPlayerNames, new ArrayList<>());
        getGameLobbyModel().setInGame(true);
        setGameModel(clientCards, discardPile, players, currentPlayerIndex, currentDirection);
        IView.invoke(() -> {
                         mvc.getAppController().getMVC().getView().onConnected();
                         mvc.logClientEvent(String.format(
                                 "> It is currently %s's turn. The current card is a %s.",
                                 getGameModel().getCurrentPlayerName(),
                                 getGameModel().getLastPlayedCard().getDescription()));
                     },
                     logger);
    }

    /**
     * Called by the server when the client's connection attempt is rejected.
     *
     * @param reason Reason for the connection rejection.
     */
    public void onConnectionRejected(@NotNull ConnectionRejectionReason reason) {
        mvc.getAppController().getConnectionController().onConnectionRejected();
        IView.invoke(() -> {
                         String errorMsg = "";
                         switch (reason) {
                             case INCORRECT_PASSWORD -> errorMsg = "Incorrect server password.";
                             case USERNAME_ALREADY_TAKEN -> errorMsg = "That username is already taken.";
                         }

                         if (!errorMsg.isEmpty()) {
                             mvc.logClientEvent(String.format("> %s", errorMsg));
                             mvc.getView().showErrorDialog("Unable to connect to server", errorMsg);
                         }
                     },
                     logger);
    }

    /**
     * Called when disconnected from the server.
     */
    public void onDisconnected() {
        lobbyModel = null;
        gameModel = null;
    }

    /* Attribute Getters/Setters */

    /**
     * @return True, if it is currently the client player's turn.
     */
    public boolean isCurrentPlayer() {
        return getGameModel().isCurrentPlayer(getPlayerName());
    }

    /**
     * @return Game model.
     */
    @NotNull
    public ClientGameModel getGameModel() {
        if (gameModel == null) throw new IllegalStateException("Game model does not exist");
        return gameModel;
    }

    /**
     * Creates a new game model using the provided state data.
     *
     * @param clientCards        The client player's current cards (or null if they are spectating).
     * @param discardPile        The discard pile, including all previously discarded cards.
     * @param players            List of participating player objects (the order matters here).
     * @param currentPlayerIndex Index of the current player (who will play the next card).
     * @param currentDirection   The current direction of play.
     */
    public void setGameModel(
            @Nullable List<ICard> clientCards,
            @NotNull Stack<ICard> discardPile,
            @NotNull List<ClientGamePlayer> players,
            int currentPlayerIndex,
            @NotNull Direction currentDirection) {
        if (gameModel != null) throw new IllegalStateException("Game model already exists");
        gameModel = new ClientGameModel(clientCards,
                                        discardPile,
                                        players,
                                        currentPlayerIndex,
                                        currentDirection);
    }

    /**
     * @return Game lobby model.
     */
    @NotNull
    public ClientGameLobbyModel getGameLobbyModel() {
        if (lobbyModel == null) throw new IllegalStateException("Game lobby model does not exist");
        return lobbyModel;
    }

    /**
     * Creates a new lobby model using the provided state data.
     *
     * @param playerName       Name of the client player.
     * @param lobbyPlayerNames Names of all players currently in the lobby.
     * @param readyPlayerNames Name of all players who are marked as ready to play.
     */
    public void setGameLobbyModel(
            @NotNull String playerName,
            @NotNull List<String> lobbyPlayerNames,
            @NotNull List<String> readyPlayerNames) {
        if (lobbyModel != null) throw new IllegalStateException("Game lobby model already exists");
        mvc.getAppController().getConnectionController().onConnectionAccepted();
        lobbyModel = new ClientGameLobbyModel(playerName, lobbyPlayerNames, readyPlayerNames);
    }

    /**
     * @return The client player's name.
     */
    @NotNull
    public String getPlayerName() {
        return getGameLobbyModel().getPlayerName();
    }

    /**
     * @param playerName Name of the player to generate the description for.
     * @return A brief verbal description of the given player's state.
     */
    @NotNull
    public String getPlayerStatus(@NotNull String playerName) {
        if (getGameLobbyModel().isInGame()) {
            if (getGameModel().doesPlayerExist(playerName)) {
                int cardAmount = getGameModel().getCardAmount(getGameModel().getPlayerIndex(playerName));
                return String.format("In Game (%d card%s)", cardAmount, cardAmount == 1 ? "" : "s");
            } else return "Spectating";
        } else return getGameLobbyModel().isPlayerReady(playerName) ? "Ready" : "Waiting";
    }

    /**
     * @return Incoming server packet handler instance.
     */
    @NotNull
    public JavunoClientPacketHandler getPacketHandler() {
        return packetHandler;
    }

    /* MVC */

    /**
     * @return An instance of the client connection (for writing packets).
     */
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
