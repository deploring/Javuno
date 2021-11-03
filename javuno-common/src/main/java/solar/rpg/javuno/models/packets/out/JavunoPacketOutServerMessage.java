package solar.rpg.javuno.models.packets.out;

import org.jetbrains.annotations.NotNull;
import solar.rpg.jserver.packet.JServerPacket;

/**
 * This packet is sent out by the server when a miscellaneous system message needs to be sent to all clients.
 * These are different to chat messages, and are more related to system and game events that don't necessarily
 * need their own packet since it is only an informational message.
 *
 * @author jskinner
 * @since 1.0.0
 */
public final class JavunoPacketOutServerMessage extends JServerPacket {

    /**
     * The server message contents.
     */
    @NotNull
    private final String message;

    /**
     * Constructs a new {@code} instance.
     *
     * @param message The server message contents.
     */
    public JavunoPacketOutServerMessage(@NotNull String message) {
        this.message = message;
    }

    /**
     * @return The format of the message to display in the client event log.
     */
    @NotNull
    public String getMessageFormat() {
        return String.format("> %s", message);
    }
}
