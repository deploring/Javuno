package solar.rpg.javuno.models.packets.in;

import org.jetbrains.annotations.NotNull;
import solar.rpg.jserver.packet.JServerPacket;

/**
 * This packet must be sent by a client after establishing a socket connection to the server.
 * A password may be required to join the server successfully.
 *
 * @author jskinner
 * @since 1.0.0
 */
public class JavunoPacketInServerConnect extends JServerPacket {

    @NotNull
    private final String wantedPlayerName;
    @NotNull
    private final String serverPassword;

    public JavunoPacketInServerConnect(@NotNull String wantedPlayerName, @NotNull String serverPassword) {
        this.wantedPlayerName = wantedPlayerName;
        this.serverPassword = serverPassword;
    }

    @NotNull
    public String getWantedPlayerName() {
        return wantedPlayerName;
    }

    @NotNull
    public String getServerPassword() {
        return serverPassword;
    }
}
