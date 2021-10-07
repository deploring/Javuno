package solar.rpg.javuno.models;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.cards.AbstractWildCard;
import solar.rpg.javuno.models.cards.ColoredCard;
import solar.rpg.javuno.models.cards.ICard;
import solar.rpg.javuno.models.cards.standard.DrawTwoCard;
import solar.rpg.javuno.models.cards.standard.NumberedCard;
import solar.rpg.javuno.models.cards.standard.ReverseCard;
import solar.rpg.javuno.models.cards.standard.SkipCard;

import java.util.Stack;

/**
 * {@code Game} represents the UNO game state which includes a draw pile and discard pile.
 * It allows players to place cards on the discard pile, or draw new cards from the draw pile.
 *
 * @author jskinner
 * @since 1.0.0
 */
public abstract class AbstractGameModel {

    private final Stack<ICard> drawPile;
    private final Stack<ICard> discardPile;

    /**
     * Constructs a new {@code AbstractGameModel} instance.
     *
     */
    public AbstractGameModel() {
        drawPile = new UnoDeckFactory().getNewDrawPile(2);
        discardPile = new Stack<>();
    }

    /**
     * @return The card on the top of the discard pile.
     */
    public ICard getLastPlayedCard() {
        assert !discardPile.empty() : "Expected at least one card on the discard pile";
        return discardPile.peek();
    }

    /**
     * @return The card on top of the draw pile, which is also removed from the draw pile.
     */
    public ICard drawCard() {
        assert drawPile.size() > 0 : "Expected non-empty draw pile";
        return drawPile.pop();
    }

    /**
     * Places a new card on top of the discard pile. This must be a valid card to play.
     *
     * @param cardToPlay The card to play.
     */
    public void playCard(@NotNull ICard cardToPlay) {
        assert isCardPlayable(cardToPlay) : "Card is not playable";
        discardPile.push(cardToPlay);
    }

    /**
     * @param cardToPlay The given card to check.
     * @return True, if the given card can be played on top of the discard pile.
     */
    public boolean isCardPlayable(@NotNull ICard cardToPlay) {
        ICard lastPlayed = getLastPlayedCard();

        return (lastPlayed instanceof NumberedCard && cardToPlay instanceof NumberedCard ?
                ((NumberedCard) lastPlayed).getNumber() == ((NumberedCard) cardToPlay).getNumber() :
                lastPlayed instanceof DrawTwoCard && cardToPlay instanceof DrawTwoCard ||
                lastPlayed instanceof SkipCard && cardToPlay instanceof SkipCard ||
                lastPlayed instanceof ReverseCard && cardToPlay instanceof ReverseCard ||
                (lastPlayed instanceof ColoredCard && cardToPlay instanceof ColoredCard ?
                 ((ColoredCard) lastPlayed).getCardColor().equals(((ColoredCard) cardToPlay).getCardColor()) :
                 cardToPlay instanceof AbstractWildCard));
    }
}
