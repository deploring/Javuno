package solar.rpg.javuno.server.models;

import solar.rpg.javuno.models.AbstractGameModel;
import solar.rpg.javuno.models.IPlayer;
import solar.rpg.javuno.models.UnoDeckFactory;
import solar.rpg.javuno.models.cards.ICard;

import java.util.List;
import java.util.Stack;

public class PrivateGameModel extends AbstractGameModel {

    private final Stack<ICard> drawPile;

    public PrivateGameModel() {
        super(new Stack<>());
        drawPile = new UnoDeckFactory().getNewDrawPile(2);
    }

    public ICard drawCard() {
        assert drawPile.size() > 0 : "Expected non-empty draw pile";
        return drawPile.pop();
    }
}
