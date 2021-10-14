package solar.rpg.javuno.model.cards;

import org.jetbrains.annotations.Nullable;
import solar.rpg.javuno.model.cards.ColoredCard.CardColor;

/**
 * Represents an UNO wild card which when played, allows the player to select which color it will represent.
 * Wild cards can be played on top of any other card.
 *
 * @author jskinner
 * @since 1.0.0
 */
public abstract class AbstractWildCard implements ICard {

    @Nullable
    private CardColor chosenCardColor;

    /**
     * @return The chosen card color for this wild card.
     */
    public CardColor getChosenCardColor() {
        assert chosenCardColor != null : "Expected wild card color to be chosen";
        return chosenCardColor;
    }

    /**
     * Once played, the player must pick the color that this wild card represents.
     *
     * @param chosenCardColor The card color that this wild card will represent.
     */
    public void setChosenCardColor(CardColor chosenCardColor) {
        assert this.chosenCardColor == null : "Expected wild card color to not be chosen";
        this.chosenCardColor = chosenCardColor;
    }
}
