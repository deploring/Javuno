package solar.rpg.javuno.model.cards.standard;

import solar.rpg.javuno.model.cards.AbstractWildCard;
import solar.rpg.javuno.model.cards.IDrawCard;

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
}
