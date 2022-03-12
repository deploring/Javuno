package solar.rpg.javuno.models.game;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Represents a player who is participating in an active JAVUNO game. The details stored on this abstract model are
 * common to both the server and client side.
 *
 * @author jskinner
 * @since 1.0.0
 */
public abstract class AbstractGamePlayer implements Serializable {

    /**
     * The name of the player participating in the JAVUNO game.
     */
    @NotNull
    private final String name;
    /**
     * True, if this player has called UNO.
     */
    private boolean uno;

    /**
     * Constructs a new {@code AbstractGamePlayer} instance.
     *
     * @param name The name of the player.
     * @param uno  True, if this player has called UNO.
     */
    public AbstractGamePlayer(@NotNull String name, boolean uno) {
        this.name = name;
        this.uno = uno;
    }

    /**
     * @return The name of the player.
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * @return The number of UNO cards that this player is holding.
     */
    public abstract int getCardCount();

    /**
     * @return True, if this player has called UNO.
     */
    public boolean isUno() {
        return uno;
    }

    /**
     * Sets the UNO call state for this player. This should be set to {@code true} where the player has called UNO and
     * this is significant to the game state. It should be set back to {@code false} if an UNO challenge is once again
     * applicable.
     *
     * @param uno True, if this player has called UNO.
     */
    public void setUno(boolean uno) {
        this.uno = uno;
    }

    /**
     * This player is only equivalent to another object if they are the same object, or the player name matches.
     *
     * @param o The object to compare.
     * @return True, if this player is equivalent to another given object.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractGamePlayer that = (AbstractGamePlayer) o;
        return name.equals(that.name);
    }
}
