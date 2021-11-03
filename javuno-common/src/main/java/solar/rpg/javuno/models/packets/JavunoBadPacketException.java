package solar.rpg.javuno.models.packets;

import org.jetbrains.annotations.NotNull;

/**
 * This exception is thrown by the server or a client  when it encounters a problem processing an incoming
 * packet. Most of the time, this is because the packet was sent with invalid or unexpected data. This can
 * be because of bugs causing a deviation in state logic and triggering a validation error. It can also get
 * raised from attempts to send packets with malicious data to intentionally "break the game".
 *
 * @author jskinner
 * @since 1.0.0
 */
public class JavunoBadPacketException extends RuntimeException {

    /**
     * True, if raising this exception should cause the connection to be disconnected.
     */
    private final boolean disconnect;

    /**
     * Constructs a new {@code JavunoBadPacketException} instance.
     *
     * @param message    A message stating why the exception was thrown.
     * @param disconnect True, if raising this exception should cause the connection to be disconnected.
     */
    public JavunoBadPacketException(@NotNull String message, boolean disconnect) {
        super(message);
        this.disconnect = disconnect;
    }

    /**
     * @return True, if raising this exception should cause the connection to be disconnected.
     */
    public boolean isDisconnect() {
        return disconnect;
    }
}
