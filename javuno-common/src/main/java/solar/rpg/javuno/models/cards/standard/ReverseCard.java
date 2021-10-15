package solar.rpg.javuno.models.cards.standard;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.cards.ColoredCard;

/**
 * Represents an UNO reverse card. When this card is played, the direction of play is reversed.
 * The following cards can be played on top of it:
 * <ul>
 *     <li>Card with the same color.</li>
 *     <li>Another reverse card.</li>
 *     <li>Wild cards.</li>
 * </ul>
 *
 * @author jskinner
 * @since 1.0.0
 */
public final class ReverseCard extends ColoredCard {

    /**
     * Constructs a new {@code ReverseCard} instance.
     *
     * @param cardColor Color of this UNO reverse card.
     */
    public ReverseCard(@NotNull CardColor cardColor) {
        super(cardColor);
    }

    @Override
    public boolean isForfeit() {
        return false;
    }
}
