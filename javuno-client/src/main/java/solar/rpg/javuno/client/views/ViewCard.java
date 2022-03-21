package solar.rpg.javuno.client.views;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class ViewCard {

    private JPanel rootPanel;
    private JLabel topSymbol;
    private JLabel bottomSymbol;
    private JLabel logo;
    private JPanel cardPanel;

    @Nullable
    private final String description;

    public ViewCard(@Nullable String description, char symbol, @NotNull Color color, boolean enabled) {
        this.description = description;

        topSymbol.setText(String.valueOf(symbol));
        bottomSymbol.setText(String.valueOf(symbol));
        cardPanel.setBackground(color);
        logo.setForeground(enabled ? Color.WHITE : Color.GRAY);

        cardPanel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
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

    public void update(boolean isPlayable, boolean isCurrentPlayer) {
        if (description == null) throw new IllegalStateException("This card cannot be updated");

        cardPanel.setEnabled(isPlayable);
        logo.setForeground(isPlayable ? Color.WHITE : Color.GRAY);
        cardPanel.setToolTipText(
            String.format(
                "(%s) %s",
                description,
                isPlayable ? "Click to play this card." :
                    isCurrentPlayer ? "This card cannot be played." : "It is not your turn, please wait."
            )
        );
    }
}
