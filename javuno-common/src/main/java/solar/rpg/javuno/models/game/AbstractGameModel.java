package solar.rpg.javuno.models.game;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.cards.AbstractWildCard;
import solar.rpg.javuno.models.cards.ColoredCard;
import solar.rpg.javuno.models.cards.ColoredCard.CardColor;
import solar.rpg.javuno.models.cards.ICard;
import solar.rpg.javuno.models.cards.IDrawCard;
import solar.rpg.javuno.models.cards.standard.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * This model stores the state of an active UNO game that is common to both the server and client side.
 *
 * @param <T> The type of players handled by this game model, dependent on the server/client implementation.
 * @author jskinner
 * @since 1.0.0
 */
public abstract class AbstractGameModel<T extends AbstractGamePlayer> implements Serializable {

    /**
     * Participating players. <em>The index order is important.</em>
     */
    @NotNull
    protected final List<T> players;
    /**
     * UNO discard pile. The card on top of the stack is the last played card.
     */
    @NotNull
    protected final Stack<ICard> discardPile;
    /**
     * Current direction of game play. This can be changed with a reverse card.
     */
    @NotNull
    private Direction direction;
    /**
     * Index of the player who has the current turn.
     */
    private int currentPlayerIndex;
    /**
     * Current game state. This can change depending on what cards are played and what actions players take.
     */
    @NotNull
    private GameState gameState;
    /**
     * Current Uno challenge state.
     */
    @NotNull
    private UnoChallengeState unoChallengeState; //TODO: Should this be an attribute on the players?

    /**
     * Constructs a new {@code AbstractGameModel} instance. The concrete implementation must provide either the starting
     * or existing game state to this constructor.
     *
     * @param discardPile       UNO discard pile.
     * @param players           Participating players.
     * @param direction         Current direction of game play.
     * @param gameState         Current game state.
     * @param unoChallengeState The current uno challenge state. //TODO: fix
     */
    public AbstractGameModel(
        @NotNull Stack<ICard> discardPile,
        @NotNull List<T> players,
        @NotNull Direction direction,
        @NotNull GameState gameState,
        @NotNull UnoChallengeState unoChallengeState) {
        this.discardPile = discardPile;
        this.players = players;
        this.direction = direction;
        this.gameState = gameState;
        this.unoChallengeState = unoChallengeState;
        currentPlayerIndex = 0; //TODO: Bug? Why is this set to zero? Do we pass through game models in packets??
    }

    /**
     * @return Current direction of game play.
     */
    @NotNull
    public Direction getDirection() {
        return direction;
    }

    /**
     * @return Name of the player who has the current turn.
     */
    public String getCurrentPlayerName() {
        return players.get(currentPlayerIndex).getName();
    }

    /**
     * @return Index of the current player who has the current turn.
     */
    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    /**
     * Sets the index of the player who has the current turn. This changes when a player completes their turn. Player
     * turns can be completely skipped when playing a skip card.
     *
     * @param currentPlayerIndex The index of the new current player.
     */
    public void setCurrentPlayerIndex(int currentPlayerIndex) {
        this.currentPlayerIndex = currentPlayerIndex;
    }

    /**
     * @param playerName The player name to check.
     * @return True, if the given player is participating in this game.
     */
    public boolean doesPlayerExist(@NotNull String playerName) {
        return players.stream().anyMatch(p -> p.getName().equals(playerName));
    }

    /**
     * @param playerName The player name to check.
     * @return True, if the current turn belongs to the given player.
     * @throws JavunoStateException Given player does not exist or is not participating.
     */
    public boolean isCurrentPlayer(@NotNull String playerName) {
        if (!doesPlayerExist(playerName))
            throw new JavunoStateException(String.format("%s is not participating", playerName));
        return currentPlayerIndex == getPlayerIndex(playerName);
    }

    /**
     * @param playerName Name of the player.
     * @return The index of the player with the given name.
     * @throws JavunoStateException Given player does not exist or is not participating.
     */
    public int getPlayerIndex(@NotNull String playerName) {
        for (int i = 0; i < players.size(); i++) {
            AbstractGamePlayer player = players.get(i);
            if (player.getName().equals(playerName)) return i;
        }
        throw new JavunoStateException(String.format("%s is not participating", playerName));
    }

    public void nextPlayer() {
        setCurrentPlayerIndex(getNextPlayerIndex(direction));
    }

    /**
     * @param direction The direction of play.
     * @return The index of the player who has the next turn, for the given direction of play.
     */
    public int getNextPlayerIndex(@NotNull Direction direction) {
        if (direction == Direction.FORWARD) {
            int result = currentPlayerIndex + 1;
            if (result >= players.size()) result = 0;
            return result;
        } else if (direction == Direction.BACKWARD) {
            int result = currentPlayerIndex - 1;
            if (result < 0) result = players.size() - 1;
            return result;
        }
        throw new UnsupportedOperationException("Unexpected value: " + direction);
    }

    /**
     * @param playerIndex The player index.
     * @return The player at the given index.
     */
    @NotNull
    public T getPlayer(int playerIndex) {
        return players.get(playerIndex);
    }

    /**
     * @return The player who had the previous turn.
     */
    @NotNull
    public T getPreviousPlayer() {
        return players.get(getNextPlayerIndex(direction.getReverse()));
    }

    /**
     * @return Copy of references to the participating players.
     */
    @NotNull
    public List<T> getPlayers() {
        return new ArrayList<>(players);
    }

    /**
     * @return Copy of the discard pile.
     */
    @NotNull
    public Stack<ICard> getDiscardPile() {
        Stack<ICard> result = new Stack<>();
        result.addAll(discardPile);
        return result;
    }

    @NotNull
    public GameState getGameState() {
        return gameState;
    }

    @NotNull
    public UnoChallengeState getUnoChallengeState() {
        return unoChallengeState;
    }

    /**
     * This method is called by the game controller when the player with the current turn draws cards from the draw
     * pile. This applies the penalty of any current draw card.
     *
     * @param nextTurn True, if the game should proceed to the next player's turn.
     */
    public void onDrawCards(boolean nextTurn) {
        if (getLastPlayedCard() instanceof IDrawCard drawCard && !drawCard.isApplied()) drawCard.apply();
        gameState = AbstractGameModel.GameState.AWAITING_PLAY;
        if (nextTurn) nextPlayer();
    }

    /**
     * @param cardsToCheck The cards to check.
     * @return True, if any of the provided cards can be played on top of the discard pile.
     */
    public boolean canPlayAnyCard(List<ICard> cardsToCheck) {
        return cardsToCheck.stream().anyMatch(this::isCardPlayable);
    }

    /**
     * A card multiplier denotes how many cards a player must pick up following the placement of a draw four card, or a
     * series of draw two cards.
     *
     * @return True, if there is currently an active card multiplier.
     * @see #getDrawTwoMultiplier()
     */
    public boolean hasCardMultiplier() {
        return (gameState == GameState.AWAITING_DRAW_TWO_RESPONSE && getDrawTwoMultiplier() > 0) ||
            (gameState == GameState.AWAITING_DRAW_FOUR_RESPONSE &&
                (!discardPile.empty() &&
                    getLastPlayedCard() instanceof WildDrawFourCard drawFourCard &&
                    !drawFourCard.isApplied()
                )
            );
    }

    /**
     * The draw two multiplier denotes how many cards a player must pick up following the placement of a series of draw
     * two cards. This is calculated by looking back through the discard pile and determining how many consecutive draw
     * two cards are yet to have their penalty applied.
     *
     * @return The draw two multiplier, starts at 0 and increases by 1 for each consecutive draw two card that is yet to
     * be applied.
     */
    public int getDrawTwoMultiplier() {
        int result = 0;
        for (int i = discardPile.size() - 1; i > 0; i--) {
            if (discardPile.get(i) instanceof DrawTwoCard drawTwoCard && !drawTwoCard.isApplied()) result++;
            else break;
        }
        return result;
    }

    /**
     * @return True, if the player who played the last card can be challenged for not making an UNO call.
     */
    public boolean canChallengeUno() {
        T previousPlayer = getPreviousPlayer();
        return previousPlayer.getCardCount() == 1 && !previousPlayer.isUno();
    }

    /**
     * Sets the initial state of the game after the first card has been played from the draw pile. Normal rules for the
     * starting card apply to the starting player, except if it is a wild card; the starting player may pick the initial
     * color. In the case of a draw four, the card penalty does not apply.
     *
     * TODO: confirm this?
     *
     * @throws JavunoStateException Game has already started.
     * @throws JavunoStateException Discard pile does not contain 1 card.
     */
    public void start() {
        if (gameState != GameState.AWAITING_START) throw new JavunoStateException("Game has already started");
        if (discardPile.size() != 1) throw new JavunoStateException("Discard pile does not have 1 card");

        gameState = GameState.AWAITING_PLAY;

        ICard card = getLastPlayedCard();
        if (card instanceof AbstractWildCard)
            gameState = GameState.AWAITING_INITIAL_COLOR;
        else if (card instanceof SkipCard)
            setCurrentPlayerIndex(getNextPlayerIndex(direction));
        else if (card instanceof DrawTwoCard)
            gameState = GameState.AWAITING_DRAW_TWO_RESPONSE;
        else if (card instanceof ReverseCard)
            direction = direction.getReverse();
    }

    /**
     * Places a new card on top of the discard pile. This must be a valid card to play.
     *
     * @param cardToPlay The card to play.
     * @throws JavunoStateException Card is a wild card and chosen color has not been set.
     * @throws JavunoStateException Card is not playable.
     */
    public void playCard(@NotNull ICard cardToPlay) {
        if (cardToPlay instanceof AbstractWildCard wildCard && wildCard.getChosenCardColor() == null)
            throw new JavunoStateException("Wild card color has not been set");
        if (!isCardPlayable(cardToPlay)) throw new JavunoStateException("Card is not playable");

        discardPile.push(cardToPlay);
        gameState = GameState.AWAITING_PLAY;

        if (cardToPlay instanceof ReverseCard) {
            direction = direction.getReverse();
            if (players.size() == 2) return; // If there are only two players, the other person's turn is skipped.
        } else if (cardToPlay instanceof SkipCard)
            nextPlayer();
        else if (cardToPlay instanceof DrawTwoCard)
            gameState = GameState.AWAITING_DRAW_TWO_RESPONSE;
        else if (cardToPlay instanceof WildDrawFourCard)
            gameState = GameState.AWAITING_DRAW_FOUR_RESPONSE;

        nextPlayer();
    }

    /**
     * @param cardToPlay The given card to check.
     * @return True, if the given card can be played on top of the discard pile.
     */
    public boolean isCardPlayable(@NotNull ICard cardToPlay) {
        ICard lastPlayed = getLastPlayedCard();

        // Only another draw two card can be played on top of a draw two card (same for all action cards).
        //TODO: If consecutive draw twos are disabled, then this is false.
        if (lastPlayed instanceof DrawTwoCard && cardToPlay instanceof DrawTwoCard) return true;

        if (gameState != GameState.AWAITING_PLAY) return false;

        // Wild cards can be played on top of any other color (except in response to a draw two).
        if (cardToPlay instanceof AbstractWildCard)
            return !(lastPlayed instanceof DrawTwoCard lastDrawTwo) || lastDrawTwo.isApplied();

        // Cards with matching colors can be played on top of one another.
        if (getCardColor(lastPlayed) == getCardColor(cardToPlay)) return true;

        // Cards with matching numbers can be played on top of one another.
        if (lastPlayed instanceof NumberedCard numbered1 && cardToPlay instanceof NumberedCard numbered2)
            return numbered1.getNumber() == numbered2.getNumber();

        return (lastPlayed instanceof SkipCard && cardToPlay instanceof SkipCard) ||
            (lastPlayed instanceof ReverseCard && cardToPlay instanceof ReverseCard);
    }

    /**
     * @param card The card to retrieve the color from.
     * @return The color of the card, otherwise the chosen color if wild.
     * @throws JavunoStateException Wild card does not have a chosen color.
     */
    private CardColor getCardColor(@NotNull ICard card) {
        CardColor result;
        if (card instanceof ColoredCard coloredCard) result = coloredCard.getCardColor();
        else if (card instanceof AbstractWildCard wildCard) result = wildCard.getChosenCardColor();
        else throw new JavunoStateException("Wild card does not have a chosen color");
        return result;
    }

    /**
     * @return The card on the top of the discard pile.
     * @throws IllegalStateException Discard pile is empty.
     */
    public ICard getLastPlayedCard() {
        if (discardPile.empty())
            throw new IllegalStateException("Expected at least one card on the discard pile");
        return discardPile.peek();
    }

    public enum GameState {
        /**
         * Waiting for the player with the current turn to play a new card.
         */
        AWAITING_PLAY(true, true),
        /**
         * If the starting card is a wild card, the starting player gets to choose the color.
         */
        AWAITING_INITIAL_COLOR(false, false),
        /**
         * After a draw four has been played, the next player must decide whether they will challenge the draw four (if
         * enabled) or pick up from the draw pile.
         */
        AWAITING_DRAW_FOUR_RESPONSE(true, false),
        /**
         * The next player must decide whether to play another draw two card (if enabled) or pick up from the draw
         * pile.
         */
        AWAITING_DRAW_TWO_RESPONSE(true, true),
        /**
         * Initial state. The game must be started before using the game model.
         */
        AWAITING_START(false, false);

        private final boolean canDraw;
        private final boolean canPlay;

        GameState(boolean canDraw, boolean canPlay) {
            this.canDraw = canDraw;
            this.canPlay = canPlay;
        }

        public boolean canDraw() {
            return canDraw;
        }

        public boolean canPlay() {
            return canPlay;
        }
    }

    public enum UnoChallengeState {
        /**
         * Uno cannot be challenged at the moment.
         */
        NOT_APPLICABLE,
        /**
         * Uno has been called by the relevant player.
         */
        CALLED,
        /**
         * Uno has not been called by the previous player, and can be challenged.
         */
        NOT_CALLED
    }
}
