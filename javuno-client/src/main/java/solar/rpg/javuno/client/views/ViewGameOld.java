package solar.rpg.javuno.client.views;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.client.controller.ClientGameController;
import solar.rpg.javuno.client.models.ClientGameModel;
import solar.rpg.javuno.client.mvc.JavunoClientMVC;
import solar.rpg.javuno.models.cards.AbstractWildCard;
import solar.rpg.javuno.models.cards.ColoredCard.CardColor;
import solar.rpg.javuno.models.cards.ICard;
import solar.rpg.javuno.models.cards.standard.ReverseCard;
import solar.rpg.javuno.models.cards.standard.SkipCard;
import solar.rpg.javuno.models.game.AbstractGameModel.GameState;
import solar.rpg.javuno.mvc.IView;
import solar.rpg.javuno.mvc.JMVC;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code ViewGame} displays the state of the current game when connected to a server.
 *
 * @author jskinner
 * @since 1.0.0
 */
public class ViewGameOld implements IView {
    @NotNull
    private final JPanel topRow = new JPanel();
    private TitledBorder topRowBorder;
    @NotNull
    private final JPanel bottomRow = new JPanel();
    private TitledBorder bottomRowBorder;
    @NotNull
    private final JButton deckButton = createCardButton("Draw", Color.LIGHT_GRAY, true);

    /**
     * Denotes the different states the right-hand box in the middle row can be in.
     */
    private enum ActionPanelState {
        /**
         * Default state.
         */
        UNKNOWN,
        /**
         * Action buttons, e.g. Call Uno, Challenge Uno, etc.
         */
        ACTION_BUTTONS,
        /**
         * Select color (after attempting to play a wild card).
         */
        SELECT_COLOR,
        /**
         * Select color of a wild card that was played as the starting card.
         */
        SELECT_INITIAL_COLOR
    }
}
