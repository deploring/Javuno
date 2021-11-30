package solar.rpg.javuno.models.packets.in;

import org.jetbrains.annotations.NotNull;

import static solar.rpg.javuno.models.cards.ColoredCard.CardColor;

/**
 * This packet is sent out by a client when they play a wild card from their hand.
 * It is an extension of {@link JavunoPacketInPlayCard}, but includes the desired wild card color.
 *
 * @author jskinner
 * @since 1.0.0
 */
public class JavunoPacketInPlayWildCard extends JavunoPacketInPlayCard {

    /**
     * The desired color chosen by the player.
     */
    private final CardColor chosenColor;

    /**
     * Constructs a new {@code JavunoPacketInOutPlayCard} instance.
     *
     * @param cardIndex   The index of the card to play.
     * @param chosenColor The desired color chosen by the player.
     */
    public JavunoPacketInPlayWildCard(int cardIndex, @NotNull CardColor chosenColor) {
        super(cardIndex);
        this.chosenColor = chosenColor;
    }

    /**
     * @return The desired color chosen by the player.
     */
    public CardColor getChosenColor() {
        return chosenColor;
    }
}
