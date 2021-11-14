package solar.rpg.javuno.models.packets.out;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.cards.ICard;
import solar.rpg.javuno.models.packets.AbstractJavunoPlayerPacket;

/**
 * This packet is sent out by the server when a player has played a valid card.
 * The card index is also sent back so the relevant client can remove the card from their hand.
 *
 * @author jskinner
 * @since 1.0.0
 */
public class JavunoPacketOutPlayCard extends AbstractJavunoPlayerPacket {

    /**
     * The card that was played.
     */
    @NotNull
    private final ICard cardToPlay;
    /**
     * The index of the card that was played.
     */
    private final int cardIndex;

    /**
     * Constructs a new {@code JavunoPacketOutPlayCard} instance.
     *
     * @param playerName The name of the player that played the card.
     * @param cardToPlay The card that was played.
     * @param cardIndex  The index of the card that was played.
     */
    public JavunoPacketOutPlayCard(@NotNull String playerName, @NotNull ICard cardToPlay, int cardIndex) {
        super(playerName);
        this.cardToPlay = cardToPlay;
        this.cardIndex = cardIndex;
    }

    /**
     * @return The card that was played.
     */
    @NotNull
    public ICard getCardToPlay() {
        return cardToPlay;
    }

    /**
     * @return The index of the card that was played.
     */
    public int getCardIndex() {
        return cardIndex;
    }
}
