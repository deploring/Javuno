package solar.rpg.javuno.server.models;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.packets.IJavunoTimeLimitedPacket;
import solar.rpg.javuno.models.packets.JavunoBadPacketException;

/**
 * Signals that a specific instance of a {@link IJavunoTimeLimitedPacket} has been sent to the server by a client more
 * than once in its specified timeout time.
 *
 * @author jskinner
 * @since 1.0.0
 */
public class JavunoPacketTimeoutException extends JavunoBadPacketException {

    /**
     * True, if the client should be notified of the timeout violation.
     */
    private final boolean notify;

    /**
     * Constructs a new {@code JavunoPacketTimeoutException} instance.
     *
     * @param message Further details about the violation.
     * @param notify  True, if the client should be notified of the timeout violation.
     */
    public JavunoPacketTimeoutException(@NotNull String message, boolean notify) {
        super(message, false);
        this.notify = notify;
    }

    /**
     * @return True, if the client should be notified of the timeout violation.
     */
    public boolean shouldNotify() {
        return notify;
    }
}
