package solar.rpg.javuno.server.models;

import org.jetbrains.annotations.NotNull;

public class JavunoBadPacketException extends Exception {

    private final boolean disconnect;
    public JavunoBadPacketException(@NotNull String message, boolean disconnect) {
        super(message);
        this.disconnect = disconnect;
    }

    public boolean isDisconnect() {
        return disconnect;
    }
}
