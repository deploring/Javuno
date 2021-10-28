package solar.rpg.javuno.models.packets.out;

import org.jetbrains.annotations.NotNull;
import solar.rpg.jserver.packet.JServerPacket;

import java.util.List;

public class JavunoPacketOutConnectionAccepted extends JServerPacket {

    @NotNull
    private final List<String> existingPlayerNames;
    @NotNull
    private final List<String> readyPlayerNames;

    public JavunoPacketOutConnectionAccepted(
            @NotNull List<String> existingPlayerNames,
            @NotNull List<String> readyPlayerNames) {
        this.existingPlayerNames = existingPlayerNames;
        this.readyPlayerNames = readyPlayerNames;
    }

    @NotNull
    public List<String> getExistingPlayerNames() {
        return existingPlayerNames;
    }

    @NotNull
    public List<String> getReadyPlayerNames() {
        return readyPlayerNames;
    }
}
