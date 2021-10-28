package solar.rpg.javuno.models.packets.in;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.packets.common.IJavunoDistributePacket;
import solar.rpg.javuno.models.packets.common.IJavunoTimeLimitedPacket;
import solar.rpg.jserver.packet.JServerPacket;

import java.util.concurrent.TimeUnit;

public class JavunoPacketInOutChatMessage
        extends JServerPacket
        implements IJavunoDistributePacket, IJavunoTimeLimitedPacket {

    @NotNull
    private final String message;

    @NotNull
    private final String senderName;

    public JavunoPacketInOutChatMessage(@NotNull String message, @NotNull String senderName) {
        if (message.isEmpty() || message.length() > 300)
            throw new IllegalArgumentException("Expected chat message in the range of 1-300 chars");
        this.message = message;
        this.senderName = senderName;
    }

    @NotNull
    public String getSenderName() {
        return senderName;
    }

    @NotNull
    public String getMessageFormat() {
        return String.format("<%s> %s", senderName, message);
    }

    @Override
    public long getLimitDuration() {
        return TimeUnit.SECONDS.toMillis(10);
    }
}
