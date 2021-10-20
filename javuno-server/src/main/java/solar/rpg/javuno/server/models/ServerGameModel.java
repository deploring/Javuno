package solar.rpg.javuno.server.models;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.cards.ICard;
import solar.rpg.javuno.models.game.AbstractGameModel;
import solar.rpg.javuno.models.game.UnoDeckFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.IntStream;

public class ServerGameModel extends AbstractGameModel {

    @NotNull
    private final List<List<ICard>> playerCards;
    @NotNull
    private final Stack<ICard> drawPile;

    public ServerGameModel(int playerCount) {
        super(new Stack<>(), playerCount);
        drawPile = new UnoDeckFactory().getNewDrawPile(2);
        playerCards = new ArrayList<>();
        IntStream.range(0, playerCount).<List<ICard>>mapToObj(i -> new ArrayList<>()).forEachOrdered(playerCards::add);
        IntStream.range(0, playerCount).forEachOrdered(i -> playerCards.get(i).addAll(List.of(drawCards(7))));
    }

    public ICard[] drawCards(int amount) {
        return IntStream.range(0, amount).mapToObj(i -> drawCard()).toArray(ICard[]::new);
    }

    public ICard drawCard() {
        assert drawPile.size() > 0 : "Expected non-empty draw pile";
        return drawPile.pop();
    }

    @Override
    public int getCardAmount(int playerIndex) {
        return playerCards.get(playerIndex).size();
    }

    @NotNull
    public List<ICard> getCurrentPlayerCards() {
        return playerCards.get(currentPlayerIndex);
    }

    public void removePlayer(int playerIndex) {
        playerCards.remove(playerIndex);
    }
}
