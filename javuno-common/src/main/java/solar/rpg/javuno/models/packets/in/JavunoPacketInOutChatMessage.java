package solar.rpg.javuno.models.packets.in;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.packets.IJavunoDistributedPacket;
import solar.rpg.javuno.models.packets.IJavunoTimeLimitedPacket;
import solar.rpg.jserver.packet.JServerPacket;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * This packet is sent from a client to the server when they type in and send a message using the chat box. Once it is
 * received by the server, it is distributed out to all other clients, where it then shows up in their chat boxes.
 *
 * @author jskinner
 * @since 1.0.0
 */
public class JavunoPacketInOutChatMessage extends JServerPacket implements IJavunoDistributedPacket, IJavunoTimeLimitedPacket {

    /**
     * Chat message contents. This may be up to 300 characters long.
     */
    @NotNull
    private final String message;

    /**
     * Name of the entity who sent the chat message.
     */
    @NotNull
    private final String senderName;

    /**
     * Constructs a new {@code JavunoPacketInOutChatMessage}.
     *
     * @param message    Chat message contents.
     * @param senderName Name of the entity who sent the chat message.
     */
    public JavunoPacketInOutChatMessage(@NotNull String message, @NotNull String senderName) {
        if (message.isEmpty() || message.length() > 300)
            throw new IllegalArgumentException("Expected chat message in the range of 1-300 chars");
        this.message = message;
        this.senderName = senderName;
    }

    /**
     * @return Name of the entity who sent the chat message.
     */
    @NotNull
    public String getSenderName() {
        return senderName;
    }

    /**
     * @param formattingFunction Reference to a function that will format the message before it is shown to a client.
     *                           This is currently used to escape HTML characters.
     * @return The format of the message to display in the client event log.
     */
    @NotNull
    public String getMessageFormat(Function<String, String> formattingFunction) {
        return String.format("&lt;%s&gt;: %s", senderName, formattingFunction.apply(message));
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
