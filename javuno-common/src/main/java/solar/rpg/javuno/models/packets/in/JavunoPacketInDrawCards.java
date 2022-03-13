package solar.rpg.javuno.models.packets.in;

import solar.rpg.javuno.models.packets.IJavunoTimeLimitedPacket;
import solar.rpg.jserver.packet.JServerPacket;

import java.util.concurrent.TimeUnit;

/**
 * This packet is sent from a client to the server when they request to pick up cards from the deck.
 *
 * @author jskinner
 * @since 1.0.0
 */
public class JavunoPacketInDrawCards extends JServerPacket implements IJavunoTimeLimitedPacket {

    @Override
    public long getLimitDuration() {
        return TimeUnit.SECONDS.toMillis(1);
    }
}
