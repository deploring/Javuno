package solar.rpg.javuno.client.controller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import solar.rpg.javuno.client.controller.ConnectionController.JavunoClientConnection;
import solar.rpg.javuno.client.models.ClientGameLobbyModel;
import solar.rpg.javuno.client.models.ClientGameModel;
import solar.rpg.javuno.client.mvc.JavunoClientMVC;
import solar.rpg.javuno.client.views.ViewGame;
import solar.rpg.javuno.models.cards.ColoredCard.CardColor;
import solar.rpg.javuno.models.cards.ICard;
import solar.rpg.javuno.models.game.AbstractGameModel.GameState;
import solar.rpg.javuno.models.game.AbstractGameModel.UnoChallengeState;
import solar.rpg.javuno.models.game.ClientOpponent;
import solar.rpg.javuno.models.game.Direction;
import solar.rpg.javuno.models.packets.in.JavunoPacketInDrawCards;
import solar.rpg.javuno.models.packets.in.JavunoPacketInOutPlayerReadyChanged;
import solar.rpg.javuno.models.packets.in.JavunoPacketInPlayCard;
import solar.rpg.javuno.models.packets.in.JavunoPacketInPlayWildCard;
import solar.rpg.javuno.models.packets.out.JavunoPacketOutConnectionRejected.ConnectionRejectionReason;
import solar.rpg.javuno.mvc.IController;
import solar.rpg.javuno.mvc.IView;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Logger;

/**
 * Handles all state and manipulation related to participating in, or spectating an active UNO game running on a Javuno
 * server that this client is connected to. This includes both a lobby model and a game model. Outgoing events from the
 * game view and incoming game related events from the server are processed by this controller.
 *
 * @author jskinner
 * @since 1.0.0
 */
public class ClientGameController implements IController {

    @NotNull
    private final Logger logger;
    @NotNull
    private final JavunoClientMVC<ViewGame, ClientGameController> mvc;
    @Nullable
    private ClientGameLobbyModel lobbyModel;
    @Nullable
    private ClientGameModel gameModel;
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

    /* Outgoing Events (called by view) */

    /**
     * Called when this client player clicks on the draw pile.
     */
    public void drawCards() {
        getClientConnection().writePacket(new JavunoPacketInDrawCards());
    }

    /**
     * Called when this client player selects a wild to play and then selects a color.
     *
     * @param cardIndex   The index of the card in the client player's hand to play.
     * @param chosenColor The desired color chosen by the player.
     */
    public void playWildCard(int cardIndex, CardColor chosenColor) {
        getClientConnection().writePacket(new JavunoPacketInPlayWildCard(cardIndex, chosenColor));
    }

    /**
     * Called when this client player clicks on a card to play.
     *
     * @param cardIndex The index of the card in the client player's hand to play.
     * @throws IndexOutOfBoundsException Card index is out of bounds.
     */
    public void playCard(int cardIndex) {
        getClientConnection().writePacket(new JavunoPacketInPlayCard(cardIndex));
    }

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

    /* Incoming Events (called by incoming server packets) */

    /**
     * Called when a player picks up cards from the draw pile.
     *
     * @param playerName    The name of the player who picked up cards.
     * @param cardAmount    The amount of cards taken from the draw pile.
     * @param cardsReceived The actual cards received. This
     * @param nextTurn      True, if the player cannot play a card after drawing.
     * @throws IllegalArgumentException Cards were inappropriately provided.
     */
    public void onDrawCards(
        @NotNull String playerName,
        int cardAmount,
        @Nullable List<ICard> cardsReceived,
        boolean nextTurn) {
        boolean self = cardsReceived != null;

        if (isCurrentPlayer() && !self)
            throw new IllegalArgumentException("This client is the current player, but cards were not received");
        else if (!isCurrentPlayer() && self)
            throw new IllegalArgumentException("Cards were received, but this client is not the current player");

        if (self) getGameModel().addCards(cardsReceived);
        getGameModel().getPlayer(getGameModel().getPlayerIndex(playerName)).incrementCardCount(cardAmount);
        getGameModel().onDrawCards(nextTurn);

        IView.invoke(() -> mvc.getView().onDrawCards(playerName, cardAmount, self, nextTurn), logger);
    }

    /**
     * Called when a player has played a card.
     *
     * @param playerName The name of the player who played the card.
     * @param cardToPlay The card that was played.
     * @param cardIndex  The index of the card that was played from the player's hand (for removal).
     * @throws IllegalStateException    Player was not allowed to play the card, or game is not running.
     * @throws IllegalArgumentException Card was not playable.
     */
    public void onPlayCard(@NotNull String playerName, @NotNull ICard cardToPlay, int cardIndex) {
        if (!getGameModel().getCurrentPlayerName().equals(playerName))
            throw new IllegalStateException(String.format("%s is not the current player", playerName));
        if (!getGameModel().getGameState().canPlay())
            throw new IllegalStateException(String.format("Not expecting this action from %s", playerName));

        getGameModel().playCard(cardToPlay);
        getGameModel().getPlayer(getGameModel().getPlayerIndex(playerName)).decrementCardAmount();

        boolean self = playerName.equals(getPlayerName());
        if (self) getGameModel().removeClientCard(cardIndex);

        IView.invoke(() -> mvc.getView().onPlayCard(playerName, self), logger);
    }

    /**
     * Called when the server has started the game.
     *
     * @param clientCards        The client player's starting cards (or null if they are spectating).
     * @param discardPile        The discard pile, including the first card drawn from the deck.
     * @param players            List of participating player objects (the order matters here).
     * @param currentPlayerIndex Index of the current player (who will play the first card).
     * @param currentDirection   The current direction of play.
     * @throws IllegalStateException Game already exists, or lobby model does not exist.
     */
    public void onGameStart(
        @Nullable List<ICard> clientCards,
        @NotNull Stack<ICard> discardPile,
        @NotNull List<ClientOpponent> players,
        int currentPlayerIndex,
        @NotNull Direction currentDirection) {
        getGameLobbyModel().setInGame(true);
        setGameModel(
            clientCards,
            discardPile,
            players,
            currentPlayerIndex,
            currentDirection,
            GameState.AWAITING_START,
            UnoChallengeState.NOT_APPLICABLE
        );
        String startingPlayerName = getGameModel().getCurrentPlayerName();
        getGameModel().start();
        IView.invoke(() -> {
            mvc.logClientEvent(String.format(
                "> The game has started! There are %d players and %s will go first. The starting card is a %s.",
                players.size(),
                startingPlayerName,
                getGameModel().getLastPlayedCard().getDescription()
            ));
            mvc.getViewInformation().refreshPlayerTable();
            mvc.getView().onGameStart();
        }, logger);
    }

    /**
     * Called by the server when a player changes their "ready to play" state.
     *
     * @param playerName The name of the player who changed their state.
     * @param isReady    True, if the player is now marked as ready to play.
     * @throws IllegalStateException    Game lobby model does not exist, game has already started, or player does not
     *                                  exist.
     * @throws IllegalArgumentException Player is already marked as ready/not ready.
     */
    public void onPlayerReadyChanged(@NotNull String playerName, boolean isReady) {
        if (getGameLobbyModel().isInGame()) throw new IllegalStateException("Game has already started");
        boolean couldStart = getGameLobbyModel().canStart();
        if (isReady) getGameLobbyModel().markPlayerReady(playerName);
        else getGameLobbyModel().unmarkPlayerReady(playerName);
        boolean canStart = getGameLobbyModel().canStart();

        IView.invoke(
            () -> mvc.getView().onPlayerReadyChanged(playerName, isReady, couldStart != canStart),
            logger
        );
    }

    /**
     * Called by the server when a player connects to the lobby.
     *
     * @param playerName The name of the player who connected.
     * @throws IllegalStateException    Game lobby model does not exist.
     * @throws IllegalArgumentException Player already exists.
     */
    public void onPlayerConnected(@NotNull String playerName) {
        getGameLobbyModel().addPlayer(playerName);
        IView.invoke(() -> {
            mvc.logClientEvent(String.format("> %s has connected.", playerName));
            mvc.getViewInformation().refreshPlayerTable();
        }, logger);
    }

    /**
     * Called by the server when a player disconnects from the lobby.,
     *
     * @param playerName The name of the player who disconnected.
     * @throws IllegalStateException     Game lobby model does not exist.
     * @throws IndexOutOfBoundsException Player does not exist.
     */
    public void onPlayerDisconnected(@NotNull String playerName) {
        getGameLobbyModel().removePlayer(playerName);
        IView.invoke(() -> {
            mvc.logClientEvent(String.format("> %s has disconnected.", playerName));
            mvc.getViewInformation().refreshPlayerTable();
        }, logger);
    }

    /**
     * Called by the server when the client player joins a lobby (no game is running).
     *
     * @param playerName       Name of the client player.
     * @param lobbyPlayerNames The names of all players in the lobby (the order matters here).
     * @param readyPlayerNames The names of all players who are marked as ready.
     * @throws IllegalStateException Game lobby model already exists.
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
     * @param gameState          The current game state.
     */
    public void onJoinGame(
        @NotNull String playerName,
        @NotNull List<String> lobbyPlayerNames,
        @Nullable List<ICard> clientCards,
        @NotNull Stack<ICard> discardPile,
        @NotNull List<ClientOpponent> players,
        int currentPlayerIndex,
        @NotNull Direction currentDirection,
        @NotNull GameState gameState,
        @NotNull UnoChallengeState unoChallengeState) {
        setGameLobbyModel(playerName, lobbyPlayerNames, new ArrayList<>());
        getGameLobbyModel().setInGame(true);
        setGameModel(
            clientCards,
            discardPile,
            players,
            currentPlayerIndex,
            currentDirection,
            gameState,
            unoChallengeState
        );
        IView.invoke(() -> {
            mvc.getAppController().getMVC().getView().onConnected();
            mvc.logClientEvent(String.format(
                "> It is currently %s's turn. The current card is a %s.",
                getGameModel().getCurrentPlayerName(),
                getGameModel().getLastPlayedCard().getDescription()
            ));
        }, logger);
    }

    /**
     * Called by the server when the client's connection attempt is rejected.
     *
     * @param reason Reason for the connection rejection.
     * @throws IllegalStateException There is no pending connection.
     */
    public void onConnectionRejected(@NotNull ConnectionRejectionReason reason) {
        mvc.getAppController().getConnectionController().onConnectionRejected();
        IView.invoke(() -> {
            String errorMsg = "";
            switch (reason) {
                case INCORRECT_PASSWORD -> errorMsg = "Incorrect server password.";
                case USERNAME_ALREADY_TAKEN -> errorMsg = "That username is already taken.";
                case INVALID_USERNAME -> errorMsg = "That username is not valid. Please only use alphanumeric characters.";
            }

            if (!errorMsg.isEmpty()) {
                mvc.logClientEvent(String.format("&gt; %s", errorMsg));
                mvc.getView().showErrorDialog("Unable to connect to server", errorMsg);
            }
        }, logger);
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
     * @return True, if this client is the current player and is able to pick up from the draw pile.
     */
    public boolean canDrawCards() {
        return isCurrentPlayer() &&
            (getGameModel().hasCardMultiplier() || !getGameModel().canPlayAnyCard(getGameModel().getClientCards()));
    }

    /**
     * @return True, if this client can call uno before or after playing their second last card.
     */
    public boolean canCallUno() {
        ClientOpponent player = getGameModel().getPlayer(getGameModel().getPlayerIndex(getPlayerName()));
        ClientOpponent previousPlayer = getGameModel().getPreviousPlayer();

        if (isCurrentPlayer())
            return !player.isUno() && player.getCardCount() <= 2;
        else
            return previousPlayer.getName().equals(getPlayerName()) &&
                previousPlayer.getCardCount() == 1 &&
                !previousPlayer.isUno();
    }

    /**
     * @return True, if this client can challenge the previous player's lack of an uno call.
     */
    public boolean canChallengeUno() {
        return !isCurrentPlayer() && getGameModel().canChallengeUno();
    }

    /**
     * @return True, if it is currently the client player's turn.
     * @throws IllegalArgumentException The client player is not participating.
     */
    public boolean isCurrentPlayer() {
        return getGameModel().isCurrentPlayer(getPlayerName());
    }

    /**
     * @return Game model.
     * @throws IllegalStateException Game model does not exist.
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
     * @param gameState          The current game state.
     * @param unoChallengeState  The current uno challenge state.
     * @throws IllegalStateException Game model already exists.
     */
    public void setGameModel(
        @Nullable List<ICard> clientCards,
        @NotNull Stack<ICard> discardPile,
        @NotNull List<ClientOpponent> players,
        int currentPlayerIndex,
        @NotNull Direction currentDirection,
        @NotNull GameState gameState,
        @NotNull UnoChallengeState unoChallengeState) {
        if (gameModel != null) throw new IllegalStateException("Game model already exists");
        gameModel = new ClientGameModel(
            clientCards,
            discardPile,
            players,
            currentPlayerIndex,
            currentDirection,
            gameState,
            unoChallengeState
        );
    }

    /**
     * @return Game lobby model.
     * @throws IllegalStateException Game lobby model does not exist.
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
     * @throws IllegalStateException Game model already exists.
     */
    public void setGameLobbyModel(
        @NotNull String playerName,
        @NotNull List<String> lobbyPlayerNames,
        @NotNull List<String> readyPlayerNames) {
        if (lobbyModel != null) throw new IllegalStateException("Game lobby model already exists");
        lobbyModel = new ClientGameLobbyModel(playerName, lobbyPlayerNames, readyPlayerNames);
        IView.invoke(() -> mvc.getAppController().getConnectionController().onConnectionAccepted(), logger);
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
