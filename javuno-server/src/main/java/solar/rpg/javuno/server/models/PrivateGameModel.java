package solar.rpg.javuno.server.models;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.AbstractGameModel;
import solar.rpg.javuno.models.Direction;
import solar.rpg.javuno.models.UnoDeckFactory;
import solar.rpg.javuno.models.cards.ICard;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.IntStream;

public class PrivateGameModel extends AbstractGameModel {

    @NotNull
    private final List<SimpleImmutableEntry<String, List<ICard>>> playerCards;
    @NotNull
    private final Stack<ICard> drawPile;

    public PrivateGameModel() {
        super(new Stack<>());
        drawPile = new UnoDeckFactory().getNewDrawPile(2);
        playerCards = new ArrayList<>();
    }

    public ICard[] drawCards(int amount) {
        return IntStream.range(0, amount).mapToObj(i -> drawCard()).toArray(ICard[]::new);
    }

    public ICard drawCard() {
        assert drawPile.size() > 0 : "Expected non-empty draw pile";
        return drawPile.pop();
    }

    @NotNull
    public String getCurrentPlayerName() {
        return playerCards.get(playerIndex).getKey();
    }

    @NotNull
    public List<ICard> getCurrentPlayerCards() {
        return playerCards.get(playerIndex).getValue();
    }

    public void addPlayer(@NotNull String playerName) {
        if (isPlayerExists(playerName))
            throw new IllegalArgumentException(String.format("Player %s already exists", playerName));
        playerCards.put(playerName, new ArrayList<>());
    }

    public void removePlayer(@NotNull String playerName) {
        if (!isPlayerExists(playerName))
            throw new IllegalArgumentException(String.format("Player %s does not exist", playerName));
        playerCards.remove(playerName);
    }

    public boolean isPlayerExists(@NotNull String playerName) {
        return playerCards.containsKey(playerName);
    }
}
