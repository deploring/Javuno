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
import java.util.NoSuchElementException;
import java.util.Stack;

/**
 * {@code AbstractGameModel} represents the UNO game state which includes a draw pile and discard pile.
 * It allows players to place cards on the discard pile, or draw new cards from the draw pile.
 * The public implementation of the game model for use by clients does not have knowledge of the underlying state apart
 * from what is normally visible, <em>i.e. participating player names, number of cards, discard pile...</em>
 *
 * @author jskinner
 * @since 1.0.0
 */
public abstract class AbstractGameModel<T extends AbstractGamePlayer> implements Serializable {

    /**
     * List of all participating player objects. The index order matters here.
     */
    @NotNull
    protected final List<T> players;

    /**
     * A {@code Stack} of all discarded {@link ICard} UNO cards.
     */
    @NotNull
    protected final Stack<ICard> discardPile;
    /**
     * Current direction of game play.
     */
    @NotNull
    private Direction direction;
    /**
     * Index of the player who is playing the next card.
     */
    private int currentPlayerIndex;
    /**
     * Current state of the game.
     */
    @NotNull
    private GameState currentGameState;
    /**
     * Current Uno challenge state.
     */
    @NotNull
    private UnoChallengeState unoChallengeState;

    /**
     * Constructs a new {@code AbstractGameModel} instance with an existing discard pile.
     *
     * @param discardPile       The existing discard pile.
     * @param players           Participating player objects. <em>Note that the order matters here.</em>
     * @param direction         The current direction of game play.
     * @param gameState         The current game state.
     * @param unoChallengeState The current uno challenge state.
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
        this.unoChallengeState = unoChallengeState;
        currentPlayerIndex = 0;
        currentGameState = gameState;
    }

    /**
     * @return The current direction of game play.
     */
    @NotNull
    public Direction getDirection() {
        return direction;
    }

    /**
     * @return Name of the current player.
     */
    public String getCurrentPlayerName() {
        return players.get(currentPlayerIndex).getName();
    }

    /**
     * @return Index of the current player (who will make the next action).
     */
    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    /**
     * Sets the index of the current player (who will make the next action).
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
     * @param playerName The given player to check.
     * @return True, if the current turn belongs to the given player.
     * @throws IllegalArgumentException Given player is not participating.
     */
    public boolean isCurrentPlayer(@NotNull String playerName) throws IllegalArgumentException {
        if (!doesPlayerExist(playerName))
            throw new IllegalArgumentException(String.format("%s is not participating", playerName));
        return getCurrentPlayerIndex() == getPlayerIndex(playerName);
    }

    /**
     * @param playerName Name of the player.
     * @return The index of the player with the given name.
     * @throws NoSuchElementException Given player does not exist.
     * @see #doesPlayerExist(String)
     */
    public int getPlayerIndex(@NotNull String playerName) throws NoSuchElementException {
        for (int i = 0; i < players.size(); i++) {
            AbstractGamePlayer player = players.get(i);
            if (player.getName().equals(playerName)) return i;
        }
        throw new NoSuchElementException(String.format("Player %s does not exist", playerName));
    }

    public void nextPlayer() {
        setCurrentPlayerIndex(getNextPlayerIndex(direction));
    }

    /**
     * Proceeds to the next player's turn. This depends on the direction of play.
     *
     * @param direction The given direction of play.
     */
    public int getNextPlayerIndex(Direction direction) {
        switch (direction) {
            case FORWARD -> {
                int result = currentPlayerIndex + 1;
                if (result >= players.size()) result = 0;
                return result;
            }
            case BACKWARD -> {
                int result = currentPlayerIndex - 1;
                if (result < 0) result = players.size() - 1;
                return result;
            }
            default -> throw new IllegalStateException("Unexpected value: " + direction);
        }
    }

    /**
     * @param playerIndex The player index.
     * @return The player object at the given index.
     * @throws IndexOutOfBoundsException Index is out of bounds.
     */
    @NotNull
    public T getPlayer(int playerIndex) {
        return players.get(playerIndex);
    }

    /**
     * @return The player object who played the last card.
     */
    public T getPreviousPlayer() {
        return players.get(getNextPlayerIndex(direction.getReverse()));
    }

    /**
     * @return Copy of list containing the participating player objects.
     */
    @NotNull
    public List<T> getPlayers() {
        return new ArrayList<>(players);
    }

    /**
     * Called by the relevant controller when the first card has been dealt. This sets the initial state of the game.
     * Normal rules apply to the first dealt card, with the starting player then being able to play one of their own
     * cards.
     *
     * @throws IllegalStateException Game could not be started.
     */
    public void start() {
        if (currentGameState != GameState.UNKNOWN) throw new IllegalStateException("Game has already started");
        if (discardPile.size() != 1) throw new IllegalStateException("Discard pile does not have 1 card");

        currentGameState = GameState.AWAITING_PLAY;

        ICard card = getLastPlayedCard();
        if (card instanceof AbstractWildCard)
            currentGameState = GameState.AWAITING_INITIAL_COLOR;
        else if (card instanceof SkipCard)
            setCurrentPlayerIndex(getNextPlayerIndex(this.direction));
        else if (card instanceof DrawTwoCard)
            currentGameState = GameState.AWAITING_DRAW_TWO_RESPONSE;
        else if (card instanceof ReverseCard) {
            direction = direction.getReverse();
        }
    }

    /**
     * Sets appropriate game state after cards have been picked up from the draw pile.
     *
     * @param nextTurn True, if it is now the next player's turn.
     */
    public void onDrawCards(boolean nextTurn) {
        if (getLastPlayedCard() instanceof IDrawCard drawCard && !drawCard.isApplied()) drawCard.apply();
        currentGameState = AbstractGameModel.GameState.AWAITING_PLAY;
        if (nextTurn) nextPlayer();
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

    /**
     * @return The amount of cards that the current player is holding.
     */
    public int getCurrentPlayerCardAmount() {
        return getPlayers().get(currentPlayerIndex).getCardCount();
    }

    /**
     * @param cardsToCheck The cards to check.
     * @return True, if any of the provided cards can be played on top of the discard pile.
     */
    public boolean canPlayAnyCard(List<ICard> cardsToCheck) {
        return (int) cardsToCheck.stream().filter(this::isCardPlayable).count() > 0;
    }

    /**
     * @return True, if the player can opt to pick up cards as the result of a draw two or wild draw four card.
     */
    public boolean hasCardMultiplier() {
        return (currentGameState == GameState.AWAITING_DRAW_TWO_RESPONSE && getDrawTwoMultiplier() > 0) ||
                (currentGameState == GameState.AWAITING_DRAW_FOUR_RESPONSE &&
                        (!discardPile.empty() &&
                                getLastPlayedCard() instanceof WildDrawFourCard drawFourCard &&
                                !drawFourCard.isApplied()));
    }

    /**
     * @return True, if the player who played the last card can be challenged for not making an uno call.
     */
    public boolean canChallengeUno() {
        AbstractGamePlayer previousPlayer = getPreviousPlayer();
        return previousPlayer.getCardCount() == 1 && !previousPlayer.isUno();
    }

    /**
     * Places a new card on top of the discard pile. This must be a valid card to play.
     *
     * @param cardToPlay The card to play.
     * @throws IllegalArgumentException Wild card color has not been set.
     * @throws IllegalArgumentException Card is not playable.
     */
    public void playCard(@NotNull ICard cardToPlay) {
        if (cardToPlay instanceof AbstractWildCard wildCard && wildCard.getChosenCardColor() == null)
            throw new IllegalArgumentException("Wild card color has not been set");
        if (!isCardPlayable(cardToPlay)) throw new IllegalArgumentException("Card is not playable");
        discardPile.push(cardToPlay);

        currentGameState = GameState.AWAITING_PLAY;

        if (cardToPlay instanceof ReverseCard) {
            direction = direction.getReverse();
            if (players.size() == 2) return;
        } else if (cardToPlay instanceof SkipCard)
            nextPlayer();
        else if (cardToPlay instanceof DrawTwoCard) {
            currentGameState = GameState.AWAITING_DRAW_TWO_RESPONSE;
        } else if (cardToPlay instanceof WildDrawFourCard)
            currentGameState = GameState.AWAITING_DRAW_FOUR_RESPONSE;

        nextPlayer();
    }

    /**
     * @param cardToPlay The given card to check.
     * @return True, if the given card can be played on top of the discard pile.
     */
    public boolean isCardPlayable(@NotNull ICard cardToPlay) {
        ICard lastPlayed = getLastPlayedCard();

        // Only another draw two card can be played on top of a draw two card.
        //TODO: If consecutive draw twos are disabled, then this is false.
        if (lastPlayed instanceof DrawTwoCard && cardToPlay instanceof DrawTwoCard) return true;

        if (currentGameState != GameState.AWAITING_PLAY) return false;

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
     * @return The color of the card (if colored), otherwise the chosen color (if wild).
     * @throws IllegalArgumentException Card does not have a color.
     */
    private CardColor getCardColor(@NotNull ICard card) {
        CardColor result;
        if (card instanceof ColoredCard coloredCard) result = coloredCard.getCardColor();
        else if (card instanceof AbstractWildCard wildCard) result = wildCard.getChosenCardColor();
        else throw new IllegalArgumentException("Card does not have a color");
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

    @NotNull
    public GameState getCurrentGameState() {
        return currentGameState;
    }

    public int getDrawTwoMultiplier() {
        int result = 0;
        for (int i = discardPile.size() - 1; i > 0; i--) {
            if (discardPile.get(i) instanceof DrawTwoCard drawTwoCard && !drawTwoCard.isApplied()) result++;
            else break;
        }
        return result;
    }

    public enum GameState {
        /**
         * Waiting for the current player to perform an action.
         */
        AWAITING_PLAY(true, true),
        /**
         * If the starting card is a Wild card, the starting player gets to choose the color.
         */
        AWAITING_INITIAL_COLOR(false, false),
        /**
         * After a draw four has been played, the next player must decide whether they will challenge the
         * draw four (if enabled) or pick up from the deck.
         */
        AWAITING_DRAW_FOUR_RESPONSE(true, false),
        /**
         * The next player must decide whether to play another draw two card (if enabled) or pick up from the deck.
         */
        AWAITING_DRAW_TWO_RESPONSE(true, true),
        /**
         * Initial state. This must be set properly before using the model.
         */
        UNKNOWN(false, false);

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
