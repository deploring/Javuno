package solar.rpg.javuno.server.models;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.packets.JavunoBadPacketException;

public class JavunoPacketTimeoutException extends JavunoBadPacketException {

    private final boolean notify;

    public JavunoPacketTimeoutException(@NotNull String message, boolean notify) {
        super(message, false);
        this.notify = notify;
    }
}
