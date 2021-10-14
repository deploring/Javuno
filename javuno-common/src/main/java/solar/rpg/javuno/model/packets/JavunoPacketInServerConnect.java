package solar.rpg.javuno.model.packets;

import org.jetbrains.annotations.NotNull;
import solar.rpg.jserver.packet.JServerPacket;

import java.io.Serial;

/**
 * This packet must be sent by a client after establishing a socket connection to the server.
 * A password may be required to join the server successfully.
 *
 * @author jskinner
 * @since 1.0.0
 */
public class JavunoPacketInServerConnect extends JServerPacket {

    @NotNull
    private final String playerName;
    @NotNull
    private final String serverPassword;

    public JavunoPacketInServerConnect(@NotNull String playerName, @NotNull String serverPassword) {
        this.playerName = playerName;
        this.serverPassword = serverPassword;
    }

    @NotNull
    public String getPlayerName() {
        return playerName;
    }

    @NotNull
    public String getServerPassword() {
        return serverPassword;
    }
}
