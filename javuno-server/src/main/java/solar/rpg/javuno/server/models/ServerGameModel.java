package solar.rpg.javuno.server.models;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.cards.ICard;
import solar.rpg.javuno.models.game.AbstractGameModel;
import solar.rpg.javuno.models.game.Direction;
import solar.rpg.javuno.models.game.UnoDeckFactory;

import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ServerGameModel extends AbstractGameModel<ServerGamePlayer> {

    @NotNull
    private final Random random;
    @NotNull
    private final Stack<ICard> drawPile;

    public ServerGameModel(@NotNull List<ServerGamePlayer> players) {
        super(new Stack<>(), players, Direction.FORWARD);
        random = new Random();
        drawPile = new UnoDeckFactory().getNewDrawPile(2);
        discardPile.push(drawPile.pop());
        setCurrentPlayerIndex(random.nextInt(players.size()));
        IntStream.range(0, players.size()).forEachOrdered(
                i -> getPlayer(i).getCards().addAll(List.of(drawCards(7))));
    }

    public ICard[] drawCards(int amount) {
        return IntStream.range(0, amount).mapToObj(i -> drawCard()).toArray(ICard[]::new);
    }

    public ICard drawCard() {
        if (drawPile.size() == 0) throw new IllegalStateException("Draw pile is empty");
        return drawPile.pop();
    }

    @NotNull
    public List<ICard> getCurrentPlayerCards() {
        return getPlayer(getCurrentPlayerIndex()).getCards();
    }

    @NotNull
    public List<Integer> getPlayerCardCounts() {
        return getPlayers().stream().map(ServerGamePlayer::getCardCount).collect(Collectors.toList());
    }

    public void removePlayer(int playerIndex) {
        players.remove(playerIndex);
    }
}
