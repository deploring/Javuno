package solar.rpg.javuno.models.cards;

import org.jetbrains.annotations.NotNull;

import java.awt.*;

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

    @Override
    @NotNull
    public String getDescription() {
        return cardColor.getDescription();
    }

    @Override
    @NotNull
    public String getHexColorCode() {
        return cardColor.getHexColorCode();
    }

    /**
     * Denotes all the different colors that a {@code ColoredCard} can have.
     */
    public enum CardColor {
        RED("Red", "D72600"),
        GREEN("Green", "379711"),
        BLUE("Blue", "0956BF"),
        YELLOW("Yellow", "ECD407");

        @NotNull
        public final String description;

        /**
         * Card color represented as a hexadecimal color code.
         */
        @NotNull
        private final String hexColor;

        CardColor(@NotNull String description, @NotNull String hexColor) {
            this.description = description;
            this.hexColor = hexColor;
        }

        /**
         * @return The given card color as a hexadecimal color code.
         */
        @NotNull
        public String getHexColorCode() {
            return hexColor;
        }

        @NotNull
        public String getDescription() {
            return description;
        }
    }
}
