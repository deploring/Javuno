package solar.rpg.javuno.model.cards;

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

    @Override
    default boolean isForfeit() {
        return true;
    }
}
