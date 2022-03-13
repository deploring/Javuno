package solar.rpg.javuno.models.packets.out;

import org.jetbrains.annotations.NotNull;
import solar.rpg.jserver.packet.JServerPacket;

/**
 * This packet must be sent out by the server once it has rejected a connection request from a client.
 * A rejection reason is also included to show to the user.
 *
 * @author jskinner
 * @since 1.0.0
 */
public class JavunoPacketOutConnectionRejected extends JServerPacket {

    /**
     * The connection rejection reason.
     */
    @NotNull
    private final ConnectionRejectionReason rejectionReason;

    /**
     * Constructs a new {@code JavunoPacketOutConnectionRejected} instance.
     * @param rejectionReason The connection rejection reason.
     */
    public JavunoPacketOutConnectionRejected(@NotNull ConnectionRejectionReason rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    /**
     * @return The connection rejection reason.
     */
    @NotNull
    public ConnectionRejectionReason getRejectionReason() {
        return rejectionReason;
    }

    /**
     * Denotes the various reasons that a connection request can be rejected for.
     */
    public enum ConnectionRejectionReason {
        /**
         * The server password was incorrect.
         */
        INCORRECT_PASSWORD,
        /**
         * There is already a user with the requested username connected to the server.
         */
        USERNAME_ALREADY_TAKEN
    }
}
