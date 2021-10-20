package solar.rpg.javuno.models.packets;

import org.jetbrains.annotations.NotNull;
import solar.rpg.jserver.packet.JServerPacket;

public class JavunoPacketOutPlayerDisconnect extends JServerPacket {

    @NotNull
    private final String playerName;

    public JavunoPacketOutPlayerDisconnect(@NotNull String playerName) {
        this.playerName = playerName;
    }

    @NotNull
    public String getPlayerName() {
        return playerName;
    }
}
