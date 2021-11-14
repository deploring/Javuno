package solar.rpg.javuno.server.models;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.cards.ICard;
import solar.rpg.javuno.models.cards.standard.DrawTwoCard;
import solar.rpg.javuno.models.cards.standard.WildDrawFourCard;
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
        super(new Stack<>(), players, Direction.FORWARD, GameState.UNKNOWN);
        random = new Random();
        drawPile = new UnoDeckFactory().getNewDrawPile(2);
        discardPile.push(drawPile.pop());
        setCurrentPlayerIndex(random.nextInt(players.size()));
        IntStream.range(0, players.size()).forEachOrdered(
                i -> getPlayer(i).getCards().addAll(drawCards(7)));
    }

    private List<ICard> drawCards(int amount) {
        if (drawPile.size() < amount)
            throw new IllegalArgumentException(String.format("Draw pile does not have at least %d cards", amount));
        return IntStream.range(0, amount).mapToObj(i -> drawPile.pop()).toList();
    }

    public List<ICard> drawCards() {
        int amount = 1;

        ICard card = getLastPlayedCard();
        if (card instanceof WildDrawFourCard drawFourCard
            && !drawFourCard.isApplied()
            && getCurrentGameState() == GameState.AWAITING_DRAW_FOUR_RESPONSE)
            amount = drawFourCard.getDrawAmount();
        else if (card instanceof DrawTwoCard drawTwoCard
                 && !drawTwoCard.isApplied()
                 && getCurrentGameState() == GameState.AWAITING_DRAW_TWO_RESPONSE)
            amount = getDrawTwoMultiplier() * drawTwoCard.getDrawAmount();

        return drawCards(amount);
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
