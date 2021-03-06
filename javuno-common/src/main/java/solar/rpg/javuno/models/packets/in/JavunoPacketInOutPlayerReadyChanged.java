package solar.rpg.javuno.models.packets.in;

import solar.rpg.javuno.models.packets.AbstractJavunoPlayerPacket;
import solar.rpg.javuno.models.packets.IJavunoDistributedPacket;
import solar.rpg.javuno.models.packets.IJavunoTimeLimitedPacket;

import java.util.concurrent.TimeUnit;

/**
 * This packet is sent from a client to the server when they mark themselves as ready/not ready in the lobby. Once it is
 * processed by the server, it is distributed back out to all clients if valid.
 *
 * @author jskinner
 * @since 1.0.0
 */
public class JavunoPacketInOutPlayerReadyChanged
    extends AbstractJavunoPlayerPacket implements IJavunoTimeLimitedPacket, IJavunoDistributedPacket {

    /**
     * True, if the player has marked themselves as ready.
     */
    private final boolean isReady;

    /**
     * Constructs a new {@code AbstractJavunoPacketPlayerReadyChanged} instance.
     *
     * @param isReady True, if the player has marked themselves as ready.
     */
    public JavunoPacketInOutPlayerReadyChanged(boolean isReady) {
        this.isReady = isReady;
    }

    /**
     * @return True, if the player has marked themselves as ready.
     */
    public boolean isReady() {
        return isReady;
    }

    @Override
    public long getLimitDuration() {
        return TimeUnit.SECONDS.toMillis(3);
    }

    @Override
    public boolean distributeToSender() {
        return true;
    }
}
