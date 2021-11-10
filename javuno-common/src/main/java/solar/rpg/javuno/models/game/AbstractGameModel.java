package solar.rpg.javuno.models.game;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.cards.AbstractWildCard;
import solar.rpg.javuno.models.cards.ColoredCard;
import solar.rpg.javuno.models.cards.ColoredCard.CardColor;
import solar.rpg.javuno.models.cards.ICard;
import solar.rpg.javuno.models.cards.standard.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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
     * Current draw two multiplier (increases once per consecutive draw two card played).
     */
    private int drawTwoMultiplier;

    /**
     * Constructs a new {@code AbstractGameModel} instance with an existing discard pile.
     *
     * @param discardPile The existing discard pile.
     * @param players     Participating player objects. <em>Note that the order matters here.</em>
     * @param direction   The current direction of game play.
     */
    public AbstractGameModel(
            @NotNull Stack<ICard> discardPile,
            @NotNull List<T> players,
            @NotNull Direction direction) {
        this.discardPile = discardPile;
        this.players = players;
        this.direction = direction;
        currentPlayerIndex = 0;
        currentGameState = GameState.UNKNOWN;
        drawTwoMultiplier = -1;
    }

    /**
     * @return The current direction of game play.
     */
    @NotNull
    public Direction getDirection() {
        return direction;
    }

    /* Player Methods */

    /**
     * @return Name of the participating player who is next to play a card.
     */
    public String getCurrentPlayerName() {
        return players.get(currentPlayerIndex).getName();
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

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

    public boolean isCurrentPlayer(@NotNull String playerName) {
        if (!doesPlayerExist(playerName))
            throw new IllegalArgumentException(String.format("%s is not participating", playerName));
        return getCurrentPlayerIndex() == getPlayerIndex(playerName);
    }

    /**
     * @param playerName Name of player.
     * @return The index of the player, otherwise -1 if they don't exist.
     */
    public int getPlayerIndex(@NotNull String playerName) {
        for (int i = 0; i < players.size(); i++) {
            AbstractGamePlayer player = players.get(i);
            if (player.getName().equals(playerName)) return i;
        }
        throw new IllegalArgumentException(String.format("Player %s does not exist", playerName));
    }

    /**
     * Proceeds to the next player's turn. This depends on the direction of play.
     */
    public int getNextPlayerIndex() {
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

    @NotNull
    public T getPlayer(int playerIndex) {
        return players.get(playerIndex);
    }

    @NotNull
    public List<T> getPlayers() {
        return new ArrayList<>(players);
    }

    //-------------------- Card Methods --------------------//

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
     * Places a new card on top of the discard pile. This must be a valid card to play.
     *
     * @param cardToPlay The card to play.
     */
    public void playCard(@NotNull ICard cardToPlay) {
        if (!isCardPlayable(cardToPlay)) throw new IllegalArgumentException("Card is not playable");
        discardPile.push(cardToPlay);

        if (cardToPlay instanceof ReverseCard)
            direction = direction == Direction.FORWARD ? Direction.BACKWARD : Direction.FORWARD;
        else if (cardToPlay instanceof SkipCard)
            setCurrentPlayerIndex(getNextPlayerIndex());
        else if (cardToPlay instanceof DrawTwoCard) {
            if (drawTwoMultiplier == -1) drawTwoMultiplier = 1;
            else drawTwoMultiplier++;
            currentGameState = GameState.AWAITING_DRAW_TWO_RESPONSE;
        } else if (cardToPlay instanceof WildDrawFourCard)
            currentGameState = GameState.AWAITING_DRAW_FOUR_RESPONSE;

        setCurrentPlayerIndex(getNextPlayerIndex());
    }

    /**
     * @param cardToPlay The given card to check.
     * @return True, if the given card can be played on top of the discard pile.
     */
    public boolean isCardPlayable(@NotNull ICard cardToPlay) {
        ICard lastPlayed = getLastPlayedCard();

        // Only another draw two card can be played on top of a draw two card.
        //TODO: If consecutive draw twos are disabled, then this is false.
        if (lastPlayed instanceof DrawTwoCard) return cardToPlay instanceof DrawTwoCard;

        // Wild cards can be played on top of any other color.
        if (cardToPlay instanceof AbstractWildCard) return true;

        // Cards with matching colors can be played on top of one another.
        if (getCardColor(lastPlayed) == getCardColor(cardToPlay)) return true;

        // Cards with matching numbers can be played on top of one another.
        if (lastPlayed instanceof NumberedCard numbered1 && cardToPlay instanceof NumberedCard numbered2)
            return numbered1.getNumber() == numbered2.getNumber();

        return (lastPlayed instanceof SkipCard && cardToPlay instanceof SkipCard) ||
               (lastPlayed instanceof ReverseCard && cardToPlay instanceof ReverseCard);
    }

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

    public enum GameState {
        /**
         * Waiting for the current player to perform an action.
         */
        AWAITING_PLAY,
        /**
         * If the starting card is a Wild card, the starting player gets to choose the color.
         */
        AWAITING_INITIAL_COLOR,
        /**
         * After a draw four has been played, the next player must decide whether they will challenge the
         * draw four (if enabled) or pick up from the deck.
         */
        AWAITING_DRAW_FOUR_RESPONSE,
        /**
         * The next player must decide whether to play another draw two card (if enabled) or pick up from the deck.
         */
        AWAITING_DRAW_TWO_RESPONSE,
        /**
         * Initial state. This must be set properly before using the model.
         */
        UNKNOWN
    }
}
