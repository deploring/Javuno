package solar.rpg.javuno.model.packets;

/**
 * This packet must be sent by a client after establishing a socket connection to the server.
 * A password may be required to join the server successfully.
 *
 * @author jskinner
 * @since 1.0.0
 */
public class JavunoPacketInPlayerJoin {

    private final String playerName;
    private final String serverPassword;

    public JavunoPacketInPlayerJoin(String playerName, String serverPassword) {
        this.playerName = playerName;
        this.serverPassword = serverPassword;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getServerPassword() {
        return serverPassword;
    }
}
