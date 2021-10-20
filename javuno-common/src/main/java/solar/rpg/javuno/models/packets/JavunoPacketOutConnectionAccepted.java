package solar.rpg.javuno.models.packets;

import org.jetbrains.annotations.NotNull;
import solar.rpg.jserver.packet.JServerPacket;

import java.util.List;

public class JavunoPacketOutConnectionAccepted extends JServerPacket {

    @NotNull
    private final List<String> existingPlayerNames;

    public JavunoPacketOutConnectionAccepted(@NotNull List<String> existingPlayerNames) {
        this.existingPlayerNames = existingPlayerNames;
    }

    @NotNull
    public List<String> getExistingPlayerNames() {
        return existingPlayerNames;
    }
}
