package solar.rpg.javuno.models.packets;

import org.jetbrains.annotations.NotNull;

/**
 * This packet must be sent by a client after establishing a socket connection to the server.
 * A password may be required to join the server successfully.
 *
 * @author jskinner
 * @since 1.0.0
 */
public class JavunoPacketInServerConnect extends JavunoPacketOutPlayerConnect {

    @NotNull
    private final String serverPassword;

    public JavunoPacketInServerConnect(@NotNull String playerName, @NotNull String serverPassword) {
        super(playerName);
        this.serverPassword = serverPassword;
    }

    @NotNull
    public String getServerPassword() {
        return serverPassword;
    }
}
