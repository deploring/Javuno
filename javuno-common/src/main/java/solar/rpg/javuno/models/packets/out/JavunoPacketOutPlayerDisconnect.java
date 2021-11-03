package solar.rpg.javuno.models.packets.out;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.packets.AbstractJavunoInOutPlayerPacket;
import solar.rpg.javuno.models.packets.IJavunoDistributedPacket;

/**
 * This packet is sent out by the server once a client has disconnected.
 * It is sent to all clients (except the disconnected) to notify them that an existing player has
 * disconnected and left the lobby.
 *
 * @author jskinner
 * @since 1.0.0
 */
public class JavunoPacketOutPlayerDisconnect
        extends AbstractJavunoInOutPlayerPacket implements IJavunoDistributedPacket {

    /**
     * Constructs a new {@code JavunoPacketOutPlayerDisconnect} instance.
     *
     * @param playerName The name of the player that disconnected.
     */
    public JavunoPacketOutPlayerDisconnect(@NotNull String playerName) {
        super(playerName);
    }

    @Override
    public boolean distributeToSender() {
        return false;
    }
}
