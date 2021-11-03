package solar.rpg.javuno.client.models;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.cards.ICard;
import solar.rpg.javuno.models.game.AbstractGameModel;

import java.util.List;
import java.util.Stack;

public class ClientGameModel extends AbstractGameModel {

    public ClientGameModel(
            @NotNull Stack<ICard> discardPile,
            @NotNull List<String> playerNames) {
        super(discardPile, playerNames);
    }

    @Override
    public int getCardAmount(int playerIndex) {
        return 0;
    }
}
