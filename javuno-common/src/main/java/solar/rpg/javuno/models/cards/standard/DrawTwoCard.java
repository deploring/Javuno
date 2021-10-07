package solar.rpg.javuno.models.cards.standard;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.cards.ColoredCard;
import solar.rpg.javuno.models.cards.IDrawCard;

/**
 * Represents an UNO draw two card. When this card is played, the next player's turn is forfeited, and they must pick up
 * two cards from the draw pile. The following cards can be played on top of it:
 * <ul>
 *     <li>Card with the same color.</li>
 *     <li>Another draw two card.</li>
 *     <li>Wild cards.</li>
 * </ul>
 *
 * @author jskinner
 * @since 1.0.0
 */
public final class DrawTwoCard extends ColoredCard implements IDrawCard {

    /**
     * Constructs a new {@code DrawTwoCard} instance.
     *
     * @param cardColor Color of this UNO draw two card.
     */
    public DrawTwoCard(@NotNull CardColor cardColor) {
        super(cardColor);
    }

    @Override
    public boolean isWild() {
        return false;
    }

    @Override
    public int getDrawAmount() {
        return 2;
    }
}
