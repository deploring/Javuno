package solar.rpg.javuno.models.packets.in;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.packets.IJavunoDistributedPacket;
import solar.rpg.javuno.models.packets.IJavunoTimeLimitedPacket;
import solar.rpg.jserver.packet.JServerPacket;

import java.util.concurrent.TimeUnit;

/**
 * This packet is sent out from a client when they type in and send something from the chat box.
 * Once it is received by the server, it is distributed out to all other clients, where it then
 * shows up in their chat boxes. This packet can only be sent every 3 seconds and the message is
 * limited to 300 characters.
 *
 * @author jskinner
 * @since 1.0.0
 */
public class JavunoPacketInOutChatMessage extends JServerPacket implements IJavunoDistributedPacket, IJavunoTimeLimitedPacket {

    /**
     * The chat message contents.
     */
    @NotNull
    private final String message;

    /**
     * The name of the sender.
     */
    @NotNull
    private final String senderName;

    /**
     * Constructs a new {@code JavunoPacketInOutChatMessage}.
     *
     * @param message    The chat message.
     * @param senderName The name of the sender.
     */
    public JavunoPacketInOutChatMessage(@NotNull String message, @NotNull String senderName) {
        if (message.isEmpty() || message.length() > 300)
            throw new IllegalArgumentException("Expected chat message in the range of 1-300 chars");
        this.message = message;
        this.senderName = senderName;
    }

    /**
     * @return The name of the message sender.
     */
    @NotNull
    public String getSenderName() {
        return senderName;
    }

    /**
     * @return The format of the message to display in the client event log.
     */
    @NotNull
    public String getMessageFormat() {
        return String.format("<%s>: %s", senderName, message);
    }

    @Override
    public long getLimitDuration() {
        return TimeUnit.SECONDS.toMillis(1);
    }

    @Override
    public boolean distributeToSender() {
        return true;
    }
}
