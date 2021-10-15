package solar.rpg.javuno.models.packets;

import org.jetbrains.annotations.NotNull;
import solar.rpg.jserver.packet.JServerPacket;

/**
 * This packet must be sent by a client after establishing a socket connection to the server.
 * A password may be required to join the server successfully.
 *
 * @author jskinner
 * @since 1.0.0
 */
public class JavunoPacketOutConnectionRejected extends JServerPacket {

    @NotNull
    private final ConnectionRejectionReason rejectionReason;

    public JavunoPacketOutConnectionRejected(@NotNull ConnectionRejectionReason rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    @NotNull
    public ConnectionRejectionReason getRejectionReason() {
        return rejectionReason;
    }

    public enum ConnectionRejectionReason {
        INCORRECT_PASSWORD,
        USERNAME_ALREADY_TAKEN
    }
}
