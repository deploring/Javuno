package solar.rpg.javuno.models.cards.standard;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.cards.ColoredCard;

/**
 * Represents an UNO skip card. When this card is played, the next player's turn is skipped with no other effects.
 * The following cards can be played on top of it:
 * <ul>
 *     <li>Card with the same color.</li>
 *     <li>Another skip card.</li>
 *     <li>Wild cards.</li>
 * </ul>
 *
 * @author jskinner
 * @since 1.0.0
 */
public final class SkipCard extends ColoredCard {

    /**
     * Constructs a new {@code SkipCard} instance.
     *
     * @param cardColor Color of this UNO skip card.
     */
    public SkipCard(@NotNull CardColor cardColor) {
        super(cardColor);
    }

    @Override
    public boolean isForfeit() {
        return true;
    }

    @Override
    public boolean isWild() {
        return false;
    }
}
