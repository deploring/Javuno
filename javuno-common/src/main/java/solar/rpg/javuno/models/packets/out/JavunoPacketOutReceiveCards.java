package solar.rpg.javuno.models.packets.out;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.cards.ICard;

import java.util.List;

/**
 * This packet is sent out by the server specifically to the player that successfully requested to draw their cards.
 * It is an extension of {@link JavunoPacketOutDrawCards}, except the actual cards are also sent to the relevant client.
 *
 * @author jskinner
 * @since 1.0.0
 */
public final class JavunoPacketOutReceiveCards extends JavunoPacketOutDrawCards {

    /**
     * The drawn cards that the player will receive.
     */
    @NotNull
    private final List<ICard> receivedCards;

    /**
     * Constructs a new {@code JavunoPacketOutReceiveCards} instance.
     *
     * @param playerName    The name of the player who picked up cards.
     * @param receivedCards The drawn cards that the player will receive.
     * @param nextTurn      True, if the player cannot play a card after drawing.
     */
    public JavunoPacketOutReceiveCards(
            @NotNull String playerName,
            @NotNull List<ICard> receivedCards,
            boolean nextTurn) {
        super(playerName, receivedCards.size(), nextTurn);
        this.receivedCards = receivedCards;
    }

    /**
     * @return The drawn cards that the player will receive.
     */
    @NotNull
    public List<ICard> getReceivedCards() {
        return receivedCards;
    }
}
