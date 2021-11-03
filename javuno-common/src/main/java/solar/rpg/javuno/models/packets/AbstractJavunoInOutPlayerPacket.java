package solar.rpg.javuno.models.packets;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import solar.rpg.jserver.packet.JServerPacket;

/**
 * Represents a packet of data which is associated with the sender's player name (via origin address).
 * This data is set once the server processes the packet and sends it back out to other clients.
 * This distribution may or may not include the sender themselves.
 *
 * @author jskinner
 * @since 1.0.0
 */
public abstract class AbstractJavunoInOutPlayerPacket extends JServerPacket implements IJavunoDistributedPacket {

    /**
     * The name of the player. This is determined by the server after processing.
     */
    @Nullable
    private String playerName;

    /**
     * Constructs a new {@code AbstractJavunoInOutPlayerPacket} without setting the name of the player.
     * Use this if the packet is being sent from a client.
     */
    public AbstractJavunoInOutPlayerPacket() {
    }

    /**
     * Constructs a new {@code AbstractJavunoInOutPlayerPacket}.
     * Use this if the packet is being sent from the server.
     *
     * @param playerName The name of the player.
     */
    public AbstractJavunoInOutPlayerPacket(@NotNull String playerName) {
        this.playerName = playerName;
    }

    /**
     * @return The name of the player.
     * @throws IllegalStateException Player name has not been set.
     */
    @NotNull
    public String getPlayerName() {
        if (playerName == null) throw new IllegalStateException("Player name has not been set");
        return playerName;
    }

    /**
     * Sets the name of the player.
     *
     * @param playerName The name of the player.
     */
    public void setPlayerName(@Nullable String playerName) {
        if (this.playerName != null) throw new IllegalStateException("Player name already set");
        this.playerName = playerName;
    }
}