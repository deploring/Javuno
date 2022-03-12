package solar.rpg.javuno.models.game;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a player participating in a JAVUNO game from the third-person perspective of a client, or more casually,
 * an opponent. This model allows an opponent's card count to be displayed for strategy/challenge purposes for the
 * relevant client, but does not reveal the details of those cards to said client.
 *
 * @author jskinner
 * @since 1.0.0
 */
public final class ClientOpponent extends AbstractGamePlayer {

    /**
     * Number of cards that this opponent is holding.
     */
    private int cardCount;

    /**
     * Constructs a new {@code ClientOpponent} instance.
     *
     * @param name      Name of the opponent.
     * @param uno       True, if the opponent has called UNO.
     * @param cardCount Number of cards that this opponent is holding.
     */
    public ClientOpponent(@NotNull String name, boolean uno, int cardCount) {
        super(name, uno);
        this.cardCount = cardCount;
    }

    /**
     * Increments the amount of cards that this opponent is holding by the specified amount.
     *
     * @param amount The amount of cards that the opponent has picked up. This may be greater than 1.
     */
    public void incrementCardCount(int amount) {
        cardCount += amount;
    }

    /**
     * Decrements the amount of cards that this opponent is holding by 1. This is done when an opponent plays a card.
     */
    public void decrementCardAmount() {
        cardCount--;
    }

    @Override
    public int getCardCount() {
        return cardCount;
    }
}
