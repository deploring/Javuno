package solar.rpg.javuno.models.packets.out;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.packets.AbstractJavunoInOutPlayerPacket;
import solar.rpg.javuno.models.packets.IJavunoDistributedPacket;

/**
 * This packet is sent out by the server once it has accepted a connection request from a client.
 * It is sent to all clients (except the sender) to notify them that a new player has joined the lobby.
 *
 * @author jskinner
 * @since 1.0.0
 */
public class JavunoPacketOutPlayerConnect extends AbstractJavunoInOutPlayerPacket implements IJavunoDistributedPacket {

    /**
     * Constructs a new {@code JavunoPacketOutPlayerConnect} instance.
     *
     * @param playerName The name of the player that connected to the server.
     */
    public JavunoPacketOutPlayerConnect(@NotNull String playerName) {
        super(playerName);
    }

    @Override
    public boolean distributeToSender() {
        return false;
    }
}
