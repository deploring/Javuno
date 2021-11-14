package solar.rpg.javuno.models.packets.in;

import solar.rpg.javuno.models.packets.IJavunoTimeLimitedPacket;
import solar.rpg.jserver.packet.JServerPacket;

import java.util.concurrent.TimeUnit;

/**
 * This packet is sent out by a client when they request to draw their outstanding cards.
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
