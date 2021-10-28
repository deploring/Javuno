package solar.rpg.javuno.models.packets.out;

import org.jetbrains.annotations.NotNull;
import solar.rpg.jserver.packet.JServerPacket;

public final class JavunoPacketOutServerMessage extends JServerPacket {

    @NotNull
    private final String message;

    public JavunoPacketOutServerMessage(@NotNull String message) {
        this.message = message;
    }

    @NotNull
    public String getMessage() {
        return message;
    }
}
