package solar.rpg.javuno.models.packets.out;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.packets.common.AbstractJavunoPlayerPacket;

public class JavunoPacketOutPlayerConnect extends AbstractJavunoPlayerPacket {

    public JavunoPacketOutPlayerConnect(@NotNull String playerName) {
        super(playerName);
    }
}
