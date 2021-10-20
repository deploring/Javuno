package solar.rpg.javuno.models.packets;

import org.jetbrains.annotations.NotNull;
import solar.rpg.jserver.packet.JServerPacket;

public class JavunoPacketOutPlayerConnect extends JServerPacket {

    @NotNull
    private final String playerName;

    public JavunoPacketOutPlayerConnect(@NotNull String playerName) {
        this.playerName = playerName;
    }

    @NotNull
    public String getPlayerName() {
        return playerName;
    }
}
