package solar.rpg.javuno.models.packets.in;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.packets.common.AbstractJavunoPlayerPacket;
import solar.rpg.javuno.models.packets.common.IJavunoDistributePacket;

public class JavunoPacketInOutPlayerReadyChanged extends AbstractJavunoPlayerPacket implements IJavunoDistributePacket {

    private final boolean isReady;

    public JavunoPacketInOutPlayerReadyChanged(@NotNull String playerName, boolean isReady) {
        super(playerName);
        this.isReady = isReady;
    }

    public boolean isReady() {
        return isReady;
    }
}
