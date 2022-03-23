package solar.rpg.javuno.models.cards;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import solar.rpg.javuno.models.cards.ColoredCard.CardColor;

import java.awt.*;

/**
 * Represents an UNO wild card which when played, allows the player to select which color it will represent. Wild cards
 * can be played on top of any other card.
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
    @Nullable
    public CardColor getChosenCardColor() {
        return chosenCardColor;
    }

    /**
     * Once played, the player must pick the color that this wild card represents.
     *
     * @param chosenCardColor The card color that this wild card will represent.
     */
    public void setChosenCardColor(@Nullable CardColor chosenCardColor) {
        if (this.chosenCardColor != null) throw new IllegalStateException("Wild card color has already been chosen");
        this.chosenCardColor = chosenCardColor;
    }

    @Override
    @NotNull
    public String getHexColorCode() {
        return chosenCardColor == null ? "#222222" : chosenCardColor.getHexColorCode();
    }
}
