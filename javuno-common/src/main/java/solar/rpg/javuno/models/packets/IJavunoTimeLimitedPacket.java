package solar.rpg.javuno.models.packets;

/**
 * Marker interface denoting that this packet will only be processed by the server once received if there has
 * not been another instance of the same packet type received within a certain period of time.
 * If the packet is sent too quickly, a {@link JavunoBadPacketException} will be thrown and needs to be handled
 * by the server.
 *
 * @author jskinner
 * @since 1.0.0
 */
public interface IJavunoTimeLimitedPacket {

    /**
     * @return The amount of time (in milliseconds) before the packet can be processed again after being received.
     */
    long getLimitDuration();
}
