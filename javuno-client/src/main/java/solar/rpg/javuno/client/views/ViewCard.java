package solar.rpg.javuno.client.views;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * This view represents a singular card to display in the game view. The use cases are:
 * <ul>
 *     <li>As a card in the client player's hand.</li>
 *     <li>As a card representing the draw pile.</li>
 *     <li>As a card representing the top of the discard pile.</li>
 * </ul>
 *
 * @author jskinner
 * @since 1.0.0
 */
public class ViewCard {

    private JPanel cardPanel;
    private JLabel topSymbol;
    private JLabel bottomSymbol;
    private JLabel logo;

    @Nullable
    private final String description;

    /**
     * Constructs a new {@code ViewCard} instance.
     *
     * @param description Verbal description of this card if part of the player's hand, otherwise {@code null}.
     * @param symbol      Card symbol (displayed in the top left and bottom right corners).
     * @param color       Color of this card.
     * @param enabled     True, if this card can be interacted with.
     */
    public ViewCard(@Nullable String description, String symbol, @NotNull Color color, boolean enabled) {
        this.description = description;

        topSymbol.setText(symbol);
        bottomSymbol.setText(symbol);
        cardPanel.setBackground(color);
        setEnabled(enabled);

        cardPanel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!cardPanel.isEnabled()) return;
                cardPanel.setBorder(BorderFactory.createLoweredBevelBorder());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                cardPanel.setBorder(BorderFactory.createRaisedBevelBorder());
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
    }

    /**
     * Sets if this card can currently be interacted with.
     *
     * @param enabled True, if this card can be interacted with.
     */
    private void setEnabled(boolean enabled) {
        cardPanel.setEnabled(enabled);
        logo.setForeground(enabled ? Color.WHITE : Color.GRAY);
    }

    /**
     * This method is called when the game view must update the cards in a player's hand. It will update the tooltip and
     * set whether the card can be interacted with.
     *
     * @param isPlayable      True, if this card can be played by the client player.
     * @param isCurrentPlayer True, if it is the client player's turn to play a card.
     * @throws IllegalStateException This card is not part of the player's hand.
     */
    public void updateCardInHand(boolean isPlayable, boolean isCurrentPlayer) {
        if (description == null) throw new IllegalStateException("This card cannot be updated");

        setEnabled(isPlayable);
        cardPanel.setToolTipText(
            String.format(
                "(%s) %s",
                description,
                isPlayable ? "Click to play this card." :
                    isCurrentPlayer ? "This card cannot be played." : "It is not your turn, please wait."
            )
        );
    }

    /**
     * @return The panel representing this card, so that it can be added to the GUI.
     */
    public JPanel getCardPanel() {
        return cardPanel;
    }
}
