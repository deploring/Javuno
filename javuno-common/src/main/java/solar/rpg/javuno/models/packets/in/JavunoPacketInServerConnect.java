package solar.rpg.javuno.models.packets.in;

import org.jetbrains.annotations.NotNull;
import solar.rpg.jserver.packet.JServerPacket;

/**
 * This packet is sent from a client to the server after establishing a socket connection to the server. A password may
 * be required to join the server successfully.
 *
 * @author jskinner
 * @since 1.0.0
 */
public class JavunoPacketInServerConnect extends JServerPacket {

    /**
     * The requested player name. It may or may not be taken.
     */
    @NotNull
    private final String wantedPlayerName;
    /**
     * The password to the server, empty if one is not set.
     */
    @NotNull
    private final String serverPassword;

    /**
     * Constructs a new {@code JavunoPacketInServerConnect} instance.
     *
     * @param wantedPlayerName The requested player name, which may already be taken.
     * @param serverPassword   The password to the server, empty if one is not set.
     */
    public JavunoPacketInServerConnect(@NotNull String wantedPlayerName, @NotNull String serverPassword) {
        this.wantedPlayerName = wantedPlayerName;
        this.serverPassword = serverPassword;
    }

    /**
     * @return The requested player name.
     */
    @NotNull
    public String getWantedPlayerName() {
        return wantedPlayerName;
    }

    /**
     * @return The server password.
     */
    @NotNull
    public String getServerPassword() {
        return serverPassword;
    }
}
