package solar.rpg.javuno.models.packets.out;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import solar.rpg.javuno.models.cards.ICard;
import solar.rpg.javuno.models.game.Direction;

import java.util.List;
import java.util.Stack;

/**
 * This packet is sent out by the server when a new game has been started. It contains the initial game state as well
 * the names of all participating players.
 *
 * @author jskinner
 * @since 1.0.0
 */
public class JavunoPacketOutGameStart extends JavunoPacketOutGameState {

    /**
     * Constructs a new {@code JavunoPacketOutGameStart} instance.
     *
     * @param clientCards        The cards associated with the player client that this packet is being sent to.
     * @param discardPile        The current discard pile state.
     * @param playerCardCounts   Current card counts of all participating players.
     * @param gamePlayerNames    Names of all participating players (order matters here).
     * @param currentPlayerIndex The index of the player who will be playing the next card.
     * @param currentDirection   The current direction of game play.
     */
    public JavunoPacketOutGameStart(
            @Nullable List<ICard> clientCards,
            @NotNull Stack<ICard> discardPile,
            @NotNull List<Integer> playerCardCounts,
            @NotNull List<String> gamePlayerNames,
            int currentPlayerIndex,
            @NotNull Direction currentDirection) {
        super(clientCards, discardPile, playerCardCounts, gamePlayerNames, currentPlayerIndex, currentDirection);
    }
}
