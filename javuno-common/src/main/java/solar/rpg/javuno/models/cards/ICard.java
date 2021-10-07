package solar.rpg.javuno.models.cards;

/**
 * Represents a playable UNO card of any type.
 *
 * @author jskinner
 * @since 1.0.0
 */
public interface ICard {

    /**
     * @return True, if playing this card causes the next player's turn to forfeited.
     */
    boolean isForfeit();
}
