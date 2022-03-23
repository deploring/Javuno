package solar.rpg.javuno.models.cards;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.Serializable;

/**
 * Represents a playable UNO card of any type.
 *
 * @author jskinner
 * @since 1.0.0
 */
public interface ICard extends Serializable {

    @NotNull
    String getDescription();

    @NotNull
    String getSymbol();

    @NotNull
    String getHexColorCode();

    default Color getDisplayColor() {
        return Color.decode("#" + getHexColorCode());
    }
}
