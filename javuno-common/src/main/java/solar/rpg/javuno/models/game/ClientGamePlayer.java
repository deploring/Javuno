package solar.rpg.javuno.models.game;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a restricted view of a client player's state. This only shows the amount of cards that a player
 * has, and not the actual cards themselves. These are sent out by the server and are expected to be maintained
 * by the client as the game takes place.
 *
 * @author jskinner
 * @since 1.0.0
 */
public final class ClientGamePlayer extends AbstractGamePlayer {

    /**
     * Number of cards this player is holding.
     */
    private int cardCount;

    /**
     * Constructs a new {@code ClientGamePlayer} instance.
     *
     * @param name      Name of the client player.
     * @param uno       True if the player has called uno.
     * @param cardCount Number of cards this player is holding.
     */
    public ClientGamePlayer(@NotNull String name, boolean uno, int cardCount) {
        super(name, uno);
        this.cardCount = cardCount;
    }

    public void incrementCardCount(int amount) {
        cardCount += amount;
    }
    public void decrementCardAmount() {
        cardCount--;
    }

    @Override
    public int getCardCount() {
        return cardCount;
    }
}
