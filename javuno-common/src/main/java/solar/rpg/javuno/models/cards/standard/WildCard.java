package solar.rpg.javuno.models.cards.standard;

import solar.rpg.javuno.models.cards.AbstractWildCard;

/**
 * Represents an UNO wild card which when played, allows the player to select which color it will represent.
 * Wild cards can be played on top of any other card.
 *
 * @author jskinner
 * @since 1.0.0
 */
public final class WildCard extends AbstractWildCard {

    @Override
    public boolean isForfeit() {
        return false;
    }
}
