package solar.rpg.javuno.models.packets.out;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.packets.common.AbstractJavunoPlayerPacket;

public class JavunoPacketOutPlayerDisconnect extends AbstractJavunoPlayerPacket {

    public JavunoPacketOutPlayerDisconnect(@NotNull String playerName) {
        super(playerName);
    }

}
