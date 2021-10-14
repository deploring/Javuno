package solar.rpg.javuno.server.models;

import solar.rpg.javuno.model.AbstractGameModel;
import solar.rpg.javuno.model.UnoDeckFactory;
import solar.rpg.javuno.model.cards.ICard;

import java.util.Stack;

public class ServerGameModel extends AbstractGameModel {

    private final Stack<ICard> drawPile;

    public ServerGameModel() {
        drawPile = new UnoDeckFactory().getNewDrawPile(2);
    }
}
