package solar.rpg.javuno.models.cards;

/**
 * Represents an UNO card that when played, forfeits the next player's turn, and they must draw a specified amount of
 * cards from the draw pile.
 *
 * @author jskinner
 * @since 1.0.0
 */
public interface IDrawCard extends ICard {

    /**
     * @return The specified amount of cards that the next player must draw when this card is played.
     */
    int getDrawAmount();

    /**
     * @return True, if the penalty has been applied for this card.
     */
    boolean isApplied();

    /**
     * This method should be called once the penalty for this card has been applied.
     *
     * @throws IllegalStateException Cannot be applied twice.
     */
    void apply();
}
