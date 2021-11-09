package solar.rpg.javuno.models.cards.standard;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.cards.AbstractWildCard;
import solar.rpg.javuno.models.cards.IDrawCard;

/**
 * Represents an UNO wild draw four card. When this card is played, the next player's turn is forfeited, and they must
 * pick up four cards from the pile. Additionally, it allows the player to select which color it will represent.
 * Wild draw four cards can be played on top of any other card.
 *
 * @author jskinner
 * @since 1.0.0
 */
public final class WildDrawFourCard extends AbstractWildCard implements IDrawCard {

    @Override
    public int getDrawAmount() {
        return 4;
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Draw Four";
    }

    @NotNull
    @Override
    public String getSymbol() {
        return "+4";
    }
}
