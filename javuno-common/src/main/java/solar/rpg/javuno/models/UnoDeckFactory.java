package solar.rpg.javuno.models;

import solar.rpg.javuno.models.cards.ColoredCard.CardColor;
import solar.rpg.javuno.models.cards.ICard;
import solar.rpg.javuno.models.cards.standard.*;

import java.util.Collections;
import java.util.Stack;
import java.util.stream.IntStream;

/**
 * Factory class responsible for generating new game states from standard UNO decks.
 *
 * @author jskinner
 * @since 1.0.0
 */
public final class UnoDeckFactory {

    /**
     * Generates an UNO draw pile with the specified amount of standard UNO decks to include.
     * A standard UNO deck contains:
     * <ul>
     *     <li>Number cards: each color contains one number 0 card and two sets of cards numbered 1-9.</li>
     *     <li>Action cards: each color contains 2x draw two cards, 2x skip cards, and 2x reverse cards.</li>
     *     <li>Wild cards: 4x wild cards and 4x wild draw four cards.</li>
     * </ul>
     * The draw pile will be shuffled.
     *
     * @param deckAmount Amount of standard UNO decks to put into the draw pile.
     * @return The new draw pile, shuffled.
     */
    public Stack<ICard> getNewDrawPile(int deckAmount) {
        assert deckAmount > 0 : "Expected deck amount to be greater than 0";

        Stack<ICard> result = new Stack<>();

        for (int i = 1; i <= deckAmount; i++) {
            for (CardColor cardColor : CardColor.values()) {
                IntStream.rangeClosed(0, 9).forEachOrdered(cardNumber -> {
                    result.add(new NumberedCard(cardColor, cardNumber));
                    if (cardNumber > 0)
                        result.add(new NumberedCard(cardColor, cardNumber));
                });

                IntStream.rangeClosed(1, 2).forEachOrdered(cardNumber -> {
                    result.add(new DrawTwoCard(cardColor));
                    result.add(new SkipCard(cardColor));
                    result.add(new ReverseCard(cardColor));
                });
            }

            for (int cardNumber = 1; cardNumber <= 4; cardNumber++) {
                result.add(new WildCard());
                result.add(new WildDrawFourCard());
            }
        }

        Collections.shuffle(result);

        return result;
    }
}
