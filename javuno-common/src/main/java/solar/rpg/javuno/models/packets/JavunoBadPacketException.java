package solar.rpg.javuno.models.packets;

import org.jetbrains.annotations.NotNull;

/**
 * This exception is thrown by the server or a client when it encounters a problem processing an incoming
 * packet. This is because the packet was sent with invalid or unexpected data. This can be because of bugs
 * causing a deviation in state logic and triggering a validation error. It can also get raised from attempts
 * to send packets with malicious data to intentionally "break the game". Most of the time it is simply lag,
 * meaning players sent packets that were no longer valid before the server could make their state change.
 *
 * @author jskinner
 * @since 1.0.0
 */
public class JavunoBadPacketException extends RuntimeException {

    /**
     * True, if raising this exception should cause a disconnect.
     */
    private final boolean fatal;

    /**
     * Constructs a new {@code JavunoBadPacketException} instance.
     *
     * @param message A message stating why the exception was thrown.
     * @param fatal   True, if raising this exception should cause a disconnect.
     */
    public JavunoBadPacketException(@NotNull String message, boolean fatal) {
        super(message);
        this.fatal = fatal;
    }

    /**
     * @return True, if raising this exception should cause a disconnect.
     */
    public boolean isFatal() {
        return fatal;
    }
}
