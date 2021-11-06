package solar.rpg.javuno.server.models;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.cards.ICard;
import solar.rpg.javuno.models.game.AbstractGameModel;
import solar.rpg.javuno.models.game.UnoDeckFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ServerGameModel extends AbstractGameModel {

    @NotNull
    private final Random random;
    @NotNull
    private final List<List<ICard>> playerCards;
    @NotNull
    private final Stack<ICard> drawPile;

    public ServerGameModel(@NotNull List<String> playerNames) {
        super(new Stack<>(), playerNames);
        random = new Random();
        playerCards = new ArrayList<>();
        drawPile = new UnoDeckFactory().getNewDrawPile(2);
        setCurrentPlayerIndex(random.nextInt(playerNames.size()));
        IntStream.range(0, playerNames.size()).<List<ICard>>mapToObj(
                i -> new ArrayList<>()).forEachOrdered(playerCards::add);
        IntStream.range(0, playerNames.size()).forEachOrdered(
                i -> playerCards.get(i).addAll(List.of(drawCards(7))));
    }

    public ICard[] drawCards(int amount) {
        return IntStream.range(0, amount).mapToObj(i -> drawCard()).toArray(ICard[]::new);
    }

    public ICard drawCard() {
        assert drawPile.size() > 0 : "Expected non-empty draw pile";
        return drawPile.pop();
    }

    @NotNull
    public List<ICard> getPlayerCards(int playerIndex) {
        return playerCards.get(playerIndex);
    }

    @NotNull
    public List<ICard> getCurrentPlayerCards() {
        return getPlayerCards(getCurrentPlayerIndex());
    }

    @NotNull
    public List<Integer> getPlayerCardCounts() {
        return getPlayerNames().stream().map(
                playerName -> getCardAmount(getPlayerIndex(playerName))).collect(Collectors.toList());
    }

    public void removePlayer(int playerIndex) {
        playerCards.remove(playerIndex);
    }

    @Override
    public int getCardAmount(int playerIndex) {
        return playerCards.get(playerIndex).size();
    }
}
