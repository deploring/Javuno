package solar.rpg.javuno.models.packets.out;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.packets.AbstractJavunoPlayerPacket;
import solar.rpg.javuno.models.packets.IJavunoTimeLimitedPacket;

import java.util.concurrent.TimeUnit;

/**
 * This packet is sent out by the server when a player requests to draw their cards.
 *
 * @author jskinner
 * @since 1.0.0
 */
public class JavunoPacketOutDrawCards extends AbstractJavunoPlayerPacket implements IJavunoTimeLimitedPacket {

    /**
     * Number of cards drawn by the player.
     */
    private final int cardAmount;
    /**
     * True, if the player cannot play a card after drawing.
     */
    private final boolean nextTurn;

    /**
     * Constructs a new {@code JavunoPacketOutDrawCards} instance.
     * @param playerName The name of the player who picked up cards.
     * @param cardAmount Number of cards drawn by the player.
     * @param nextTurn True, if the player cannot play a card after drawing.
     */
    public JavunoPacketOutDrawCards(@NotNull String playerName, int cardAmount, boolean nextTurn) {
        super(playerName);
        this.cardAmount = cardAmount;
        this.nextTurn = nextTurn;
    }

    /**
     * @return Number of cards drawn by the player.
     */
    public int getCardAmount() {
        return cardAmount;
    }

    /**
     * @return True, if the player cannot play a card after drawing.
     */
    public boolean isNextTurn() {
        return nextTurn;
    }

    @Override
    public long getLimitDuration() {
        return TimeUnit.SECONDS.toMillis(1);
    }
}
