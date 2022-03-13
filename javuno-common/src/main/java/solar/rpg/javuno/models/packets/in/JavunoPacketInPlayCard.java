package solar.rpg.javuno.models.packets.in;

import solar.rpg.javuno.models.packets.IJavunoTimeLimitedPacket;
import solar.rpg.jserver.packet.JServerPacket;

import java.util.concurrent.TimeUnit;

/**
 * This packet is sent from a client to the server when they play one of the cards in their hand.
 *
 * @author jskinner
 * @since 1.0.0
 */
public class JavunoPacketInPlayCard extends JServerPacket implements IJavunoTimeLimitedPacket {

    /**
     * Index of the card to play in the player's hand. The actual details of the card to play is not sent for security
     * and validation purposes.
     */
    private final int cardIndex;

    /**
     * Constructs a new {@code JavunoPacketInOutPlayCard} instance.
     *
     * @param cardIndex Index of the card to play in the player's hand.
     */
    public JavunoPacketInPlayCard(int cardIndex) {
        this.cardIndex = cardIndex;
    }

    /**
     * @return Index of the card to play in the player's hand.
     */
    public int getCardIndex() {
        return cardIndex;
    }

    @Override
    public long getLimitDuration() {
        return TimeUnit.SECONDS.toMillis(1);
    }
}
