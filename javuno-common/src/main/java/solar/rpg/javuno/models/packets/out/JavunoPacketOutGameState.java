package solar.rpg.javuno.models.packets.out;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import solar.rpg.javuno.models.cards.ICard;
import solar.rpg.javuno.models.game.Direction;
import solar.rpg.jserver.packet.JServerPacket;

import java.util.List;
import java.util.Stack;

/**
 * This packet contains all UNO game state information for a currently running game.
 *
 * @author jskinner
 * @since 1.0.0
 */
public class JavunoPacketOutGameState extends JServerPacket {

    /**
     * The cards associated with the player client that this packet is being sent to.
     * This is null if the player is spectating a game instead of participating.
     */
    @Nullable
    private final List<ICard> clientCards;
    /**
     * The current discard pile state.
     */
    @NotNull
    private final Stack<ICard> discardPile;
    /**
     * Current card counts of all participating players.
     */
    @NotNull
    private final List<Integer> playerCardCounts;
    /**
     * Names of all participating players (order matters here).
     */
    @NotNull
    private final List<String> gamePlayerNames;
    /**
     * The index of the player who will be playing the next card.
     */
    private final int currentPlayerIndex;
    /**
     * The current direction of game play.
     */
    @NotNull
    private final Direction currentDirection;

    /**
     * Constructs a new {@code AbstractJavunoPacketOutGameState} instance.
     *
     * @param clientCards        The cards associated with the player client that this packet is being sent to.
     * @param discardPile        The current discard pile state.
     * @param playerCardCounts   Current card counts of all participating players.
     * @param gamePlayerNames    Names of all participating players (order matters here).
     * @param currentPlayerIndex The index of the player who will be playing the next card.
     * @param currentDirection   The current direction of game play.
     */
    public JavunoPacketOutGameState(
            @Nullable List<ICard> clientCards,
            @NotNull Stack<ICard> discardPile,
            @NotNull List<Integer> playerCardCounts,
            @NotNull List<String> gamePlayerNames,
            int currentPlayerIndex,
            @NotNull Direction currentDirection) {
        this.clientCards = clientCards;
        this.discardPile = discardPile;
        this.playerCardCounts = playerCardCounts;
        this.gamePlayerNames = gamePlayerNames;
        this.currentPlayerIndex = currentPlayerIndex;
        this.currentDirection = currentDirection;
    }

    /**
     * @return The cards associated with the player client that this packet is being sent to.
     */
    @Nullable
    public List<ICard> getClientCards() {
        return clientCards;
    }

    /**
     * @return The current discard pile state.
     */
    @NotNull
    public Stack<ICard> getDiscardPile() {
        return discardPile;
    }

    /**
     * @return Current card counts of all participating players.
     */
    @NotNull
    public List<Integer> getPlayerCardCounts() {
        return playerCardCounts;
    }

    /**
     * @return Names of all participating players (order matters here).
     */
    @NotNull
    public List<String> getGamePlayerNames() {
        return gamePlayerNames;
    }

    /**
     * @return The index of the player who will be playing the next card.
     */
    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    /**
     * @return The current direction of game play.
     */
    @NotNull
    public Direction getCurrentDirection() {
        return currentDirection;
    }
}
