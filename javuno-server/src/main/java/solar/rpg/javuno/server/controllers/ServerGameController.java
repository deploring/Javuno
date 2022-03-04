package solar.rpg.javuno.server.controllers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import solar.rpg.javuno.models.cards.AbstractWildCard;
import solar.rpg.javuno.models.cards.ICard;
import solar.rpg.javuno.models.game.ClientGamePlayer;
import solar.rpg.javuno.models.packets.out.*;
import solar.rpg.javuno.mvc.IController;
import solar.rpg.javuno.mvc.JMVC;
import solar.rpg.javuno.server.models.ServerGameLobbyModel;
import solar.rpg.javuno.server.models.ServerGameModel;
import solar.rpg.javuno.server.models.ServerGamePlayer;
import solar.rpg.javuno.server.views.MainFrame;
import solar.rpg.jserver.connection.handlers.packet.JServerHost;
import solar.rpg.jserver.packet.JServerPacket;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static solar.rpg.javuno.models.cards.ColoredCard.CardColor;

public class ServerGameController implements IController {

    @NotNull
    private final Logger logger;
    @NotNull
    private final JMVC<MainFrame, ServerGameController> mvc;
    @NotNull
    private final ExecutorService executor;
    @NotNull
    private final ServerGameLobbyModel gameLobbyModel;
    @Nullable
    private ServerGameModel gameModel;
    @NotNull
    private final JavunoServerPacketValidatorHandler packetHandler;
    @Nullable
    private CompletableFuture<Void> currentGameStart;

    public ServerGameController(@NotNull ExecutorService executor, @NotNull Logger logger) {
        this.executor = executor;
        this.logger = logger;
        mvc = new JMVC<>();
        gameLobbyModel = new ServerGameLobbyModel();
        packetHandler = new JavunoServerPacketValidatorHandler(mvc, logger);
    }

    /* Game Starting Logic */

    public void tryGameStart() {
        if (gameLobbyModel.isInGame()) throw new IllegalStateException("Game has already started");
        if (currentGameStart != null) {
            if (!gameLobbyModel.canStart()) cancelGameStarting();
        } else if (getGameLobbyModel().canStart()) setGameStarting();
    }

    private void setGameStarting() {
        if (currentGameStart != null) throw new IllegalStateException("Game is already starting");
        currentGameStart = new CompletableFuture<>();

        final CompletableFuture<Void> gameStart = currentGameStart;
        executor.submit(() -> {
            try {
                //TODO Make this longer
                Thread.sleep(TimeUnit.SECONDS.toMillis(5));
                if (!gameStart.isCancelled()) onStartGame();
            } catch (InterruptedException ignored) {
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void cancelGameStarting() {
        if (currentGameStart == null) throw new IllegalStateException("Game is not starting");
        currentGameStart.cancel(true);
        currentGameStart = null;
    }

    private void onStartGame() {
        if (currentGameStart == null) throw new IllegalStateException("Game is not starting");
        currentGameStart.complete(null);
        currentGameStart = null;

        gameLobbyModel.setInGame(true);
        gameModel = new ServerGameModel(
                gameLobbyModel.getReadyPlayerNames().stream().map(ServerGamePlayer::new).collect(Collectors.toList()));

        for (String playerName : gameLobbyModel.getLobbyPlayerNames()) {
            InetSocketAddress originAddress = gameLobbyModel.getOriginAddress(playerName);

            List<ICard> playerCards = gameModel.doesPlayerExist(playerName)
                                      ? gameModel.getPlayer(gameModel.getPlayerIndex(playerName)).getCards()
                                      : null;

            getHostController().getServerHost().writePacket(
                    originAddress,
                    new JavunoPacketOutGameStart(playerCards,
                                                 gameModel.getDiscardPile(),
                                                 getClientGamePlayers(),
                                                 gameModel.getCurrentPlayerIndex(),
                                                 gameModel.getDirection()));
        }

        gameModel.start();
    }

    /* Lobby Events */

    public void onPlayerReadyChanged(@NotNull InetSocketAddress originAddress, boolean isReady) {
        String playerName = gameLobbyModel.getPlayerName(originAddress);

        if (isReady) gameLobbyModel.markPlayerReady(playerName);
        else gameLobbyModel.unmarkPlayerReady(playerName);
    }

    public void onPlayerDisconnect(@NotNull InetSocketAddress originAddress) {
        String oldPlayerName = gameLobbyModel.getPlayerName(originAddress);
        gameLobbyModel.removePlayer(originAddress);
        getPacketHandler().onPlayerDisconnect(originAddress);
        getHostController().getServerHost().writePacketAll(new JavunoPacketOutPlayerDisconnect(oldPlayerName));
    }

    private void onPlayerConnect(@NotNull String playerName, @NotNull InetSocketAddress originAddress) {
        gameLobbyModel.addPlayer(playerName, originAddress);
    }

    /* Client Events */

    public void onDrawCards(@NotNull InetSocketAddress originAddress) {
        String playerName = getGameLobbyModel().getPlayerName(originAddress);
        if (!getGameModel().isCurrentPlayer(playerName))
            throw new IllegalStateException(String.format("%s is not the current player", playerName));
        if (!getGameModel().getCurrentGameState().canDraw())
            throw new IllegalStateException(String.format("Not expecting this action from %s", playerName));

        List<ICard> cardsToDraw = mvc.getController().getDrawnCards(playerName);

        getGameModel().getPlayer(getGameModel().getPlayerIndex(playerName)).getCards().addAll(cardsToDraw);

        boolean nextTurn = cardsToDraw.size() != 1 || !getGameModel().isCardPlayable(cardsToDraw.get(0));
        getGameModel().onDrawCards(nextTurn);

        getHostController().getServerHost().writePacketAllExcept(new JavunoPacketOutDrawCards(playerName,
                                                                                              cardsToDraw.size(),
                                                                                              nextTurn), originAddress);
        getHostController().getServerHost().writePacket(originAddress,
                                                        new JavunoPacketOutReceiveCards(playerName,
                                                                                        cardsToDraw,
                                                                                        nextTurn));
    }

    /**
     * This method is called when a client attempts to play a card in their hand.
     *
     * @param originAddress The player's origin address.
     * @param cardIndex     The index of the card to play.
     * @param chosenColor   The chosen card color, if a draw four was played (otherwise null).
     * @throws IllegalStateException    The associated player is not the current player.
     * @throws IllegalStateException    Game state is not set to AWAITING_PLAY.
     * @throws IllegalArgumentException Chosen color has not been provided where selected card is a wild card.
     * @throws IllegalArgumentException Chosen color was provided where selected selected card is not a wild card.
     */
    public void onPlayCard(@NotNull InetSocketAddress originAddress, int cardIndex, @Nullable CardColor chosenColor) {
        String playerName = getGameLobbyModel().getPlayerName(originAddress);
        if (!getGameModel().isCurrentPlayer(playerName))
            throw new IllegalStateException(String.format("%s is not the current player", playerName));
        if (!getGameModel().getCurrentGameState().canPlay())
            throw new IllegalStateException(String.format("Not expecting this action from %s", playerName));
        ServerGamePlayer player = getGameModel().getPlayer(getGameModel().getPlayerIndex(playerName));
        ICard card = player.getCards().remove(cardIndex);

        if (card instanceof AbstractWildCard wildCard) {
            if (chosenColor == null) throw new IllegalArgumentException("Chosen color has not been provided");
            wildCard.setChosenCardColor(chosenColor);
        } else if (chosenColor != null) throw new IllegalArgumentException("Expected chosen color to be null");

        getGameModel().playCard(card);
        getHostController().getServerHost().writePacketAll(new JavunoPacketOutPlayCard(playerName, card, cardIndex));
    }

    /**
     * This method is called when a client has connected to the server and sent through their connect packet.
     * The server password is first validated (if one is set), followed by validating the availability of the
     * wanted player name. If successful, an accepted packet is sent. Otherwise, a rejected packet is sent with
     * the reason for the rejection.
     *
     * @param originAddress    The origin address of the connecting player.
     * @param wantedPlayerName The wanted player name, which may or may not be taken.
     * @param serverPassword   The provided server password.
     */
    public void onPlayerConnect(
            @NotNull InetSocketAddress originAddress,
            @NotNull String wantedPlayerName,
            @NotNull String serverPassword) {
        JServerHost serverHost = getHostController().getServerHost();
        boolean closeSocket = false;
        JServerPacket packetToWrite;
        if (!serverPassword.isEmpty() && !serverPassword.equals(getHostController().getServerPassword())) {
            packetToWrite = new JavunoPacketOutConnectionRejected(JavunoPacketOutConnectionRejected.ConnectionRejectionReason.INCORRECT_PASSWORD);
            closeSocket = true;
        } else if (getGameLobbyModel().doesPlayerExist(wantedPlayerName)) {
            packetToWrite = new JavunoPacketOutConnectionRejected(JavunoPacketOutConnectionRejected.ConnectionRejectionReason.USERNAME_ALREADY_TAKEN);
            closeSocket = true;
        } else {
            onPlayerConnect(wantedPlayerName, originAddress);
            packetToWrite = new JavunoPacketOutConnectionAccepted(wantedPlayerName,
                                                                  gameLobbyModel.getLobbyPlayerNames(),
                                                                  gameLobbyModel.isInGame()
                                                                  ? null
                                                                  : gameLobbyModel.getReadyPlayerNames(),
                                                                  gameLobbyModel.isInGame()
                                                                  ? getGameStatePacket(wantedPlayerName)
                                                                  : null);
            serverHost.writePacketAllExcept(new JavunoPacketOutPlayerConnect(wantedPlayerName), originAddress);
        }

        serverHost.writePacket(originAddress, packetToWrite);
        if (closeSocket) serverHost.closeSocket(originAddress);
    }

    /* Field Getters & Setters */

    @NotNull
    public List<ICard> getDrawnCards(@NotNull String playerName) {
        if (!getGameModel().isCurrentPlayer(playerName))
            throw new IllegalStateException(String.format("%s is not the current player", playerName));
        if (!getGameModel().getCurrentGameState().canDraw())
            throw new IllegalStateException(String.format("Not expecting this action from %s", playerName));
        return getGameModel().drawCards();
    }

    @NotNull
    public JavunoServerPacketValidatorHandler getPacketHandler() {
        return packetHandler;
    }

    @NotNull
    public ServerGameLobbyModel getGameLobbyModel() {
        return gameLobbyModel;
    }

    @NotNull
    public ServerGameModel getGameModel() {
        if (gameModel == null) throw new IllegalStateException("Game is not running");
        return gameModel;
    }

    @NotNull
    private List<ClientGamePlayer> getClientGamePlayers() {
        return getGameModel().getPlayers().stream().map(serverGamePlayer -> new ClientGamePlayer(
                serverGamePlayer.getName(),
                serverGamePlayer.isUno(),
                serverGamePlayer.getCardCount())).collect(Collectors.toList());
    }

    @Nullable
    private List<ICard> getPlayerCards(@NotNull String playerName) {
        return getGameModel().doesPlayerExist(playerName) ?
               getGameModel().getPlayer(getGameModel().getPlayerIndex(playerName)).getCards() :
               null;
    }

    @NotNull
    private JavunoPacketOutGameState getGameStatePacket(@NotNull String playerName) {
        return new JavunoPacketOutGameState(getPlayerCards(playerName),
                                            getGameModel().getDiscardPile(),
                                            getClientGamePlayers(),
                                            getGameModel().getCurrentPlayerIndex(),
                                            getGameModel().getDirection(),
                                            getGameModel().getCurrentGameState(),
                                            getGameModel().getUnoChallengeState());
    }

    /* MVC */

    @NotNull
    private HostController getHostController() {
        return mvc.getView().getMVC().getController().getHostController();
    }

    @Override
    public JMVC<MainFrame, ServerGameController> getMVC() {
        return mvc;
    }
}
