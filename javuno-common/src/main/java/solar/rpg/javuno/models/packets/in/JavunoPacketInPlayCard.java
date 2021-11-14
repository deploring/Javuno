package solar.rpg.javuno.models.packets.in;

import solar.rpg.javuno.models.packets.IJavunoTimeLimitedPacket;
import solar.rpg.jserver.packet.JServerPacket;

import java.util.concurrent.TimeUnit;

/**
 * This packet is sent out by a client when they play one of the cards in their hand.
 *
 * @author jskinner
 * @since 1.0.0
 */
public class JavunoPacketInPlayCard extends JServerPacket implements IJavunoTimeLimitedPacket {

    /**
     * The index of the card to play.
     */
    private final int cardIndex;

    /**
     * Constructs a new {@code JavunoPacketInOutPlayCard} instance.
     *
     * @param cardIndex The index of the card to play.
     */
    public JavunoPacketInPlayCard(int cardIndex) {
        this.cardIndex = cardIndex;
    }

    /**
     * @return The index of the card to play.
     */
    public int getCardIndex() {
        return cardIndex;
    }

    @Override
    public long getLimitDuration() {
        return TimeUnit.SECONDS.toMillis(1);
    }
}
