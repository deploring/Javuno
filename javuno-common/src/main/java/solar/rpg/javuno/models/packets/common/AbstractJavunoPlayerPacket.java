package solar.rpg.javuno.models.packets.common;

import org.jetbrains.annotations.NotNull;
import solar.rpg.jserver.packet.JServerPacket;

/**
 * Represents a packet of data which is associated a player client.
 * In Javuno, these packets are validated on the server-side to ensure that
 * they came from the correct origin address before processing them.
 *
 * @author jskinner
 * @since 1.0.0
 */
public abstract class AbstractJavunoPlayerPacket extends JServerPacket {

    @NotNull
    private final String playerName;

    public AbstractJavunoPlayerPacket(@NotNull String playerName) {
        this.playerName = playerName;
    }

    @NotNull
    public String getPlayerName() {
        return playerName;
    }
}