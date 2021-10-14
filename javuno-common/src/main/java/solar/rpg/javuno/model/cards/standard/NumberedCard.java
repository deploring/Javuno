package solar.rpg.javuno.model.cards.standard;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.model.cards.ColoredCard;

/**
 * Represents a numbered UNO card, which is also colored. They have no extra effects like action cards do.
 * These are the most common type of card in play. The following cards can be played on top of it:
 * <ul>
 *     <li>Card with the same color.</li>
 *     <li>Card with the same number.</li>
 *     <li>Wild cards.</li>
 * </ul>
 *
 * @author jskinner
 * @since 1.0.0
 */
public final class NumberedCard extends ColoredCard {

    /**
     * Number of this card (0-9).
     */
    private final int number;

    /**
     * Constructs a new {@code NumberedCard} instance.
     *
     * @param cardColor Color of this UNO card.
     * @param number    Number of this UNO card (0-9).
     */
    public NumberedCard(@NotNull CardColor cardColor, int number) {
        super(cardColor);

        assert number >= 0 && number <= 9 : String.format("Illegal card number %d", number);
        this.number = number;
    }

    /**
     * @return Number of this card (0-9).
     */
    public int getNumber() {
        return number;
    }

    @Override
    public boolean isForfeit() {
        return false;
    }
}
