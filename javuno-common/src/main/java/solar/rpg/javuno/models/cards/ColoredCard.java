package solar.rpg.javuno.models.cards;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a playable UNO card that is colored.
 *
 * @author jskinner
 * @since 1.0.0
 */
public abstract class ColoredCard implements ICard {

    /**
     * Color of this UNO card.
     */
    @NotNull
    private final CardColor cardColor;

    /**
     * Constructs a new {@code ColoredCard} instance.
     *
     * @param cardColor Color of this UNO card.
     */
    protected ColoredCard(@NotNull CardColor cardColor) {
        this.cardColor = cardColor;
    }

    /**
     * @return Color of this UNO card.
     */
    @NotNull
    public CardColor getCardColor() {
        return cardColor;
    }

    /**
     * Denotes all the different colors that a {@code ColoredCard} can have.
     */
    public enum CardColor {
        RED("D72600"),
        GREEN("379711"),
        BLUE("0956BF"),
        YELLOW("ECD407");

        /**
         * Card color represented as a hexadecimal color code.
         */
        @NotNull
        private final String hexColor;

        CardColor(@NotNull String hexColor) {
            this.hexColor = hexColor;
        }

        /**
         * @return The given card color as a hexadecimal color code.
         */
        @NotNull
        public String getHexColor() {
            return hexColor;
        }
    }
}
