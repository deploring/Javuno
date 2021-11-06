package solar.rpg.javuno.client.models;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import solar.rpg.javuno.models.cards.ICard;
import solar.rpg.javuno.models.game.AbstractGameModel;

import java.util.List;
import java.util.Stack;

public final class ClientGameModel extends AbstractGameModel {

    @Nullable
    private final List<ICard> cards;
    @NotNull
    private final List<Integer> playerCardCounts;

    public ClientGameModel(
            @Nullable List<ICard> cards,
            @NotNull List<Integer> playerCardCounts,
            @NotNull Stack<ICard> discardPile,
            @NotNull List<String> playerNames,
            int currentPlayerIndex) {
        super(discardPile, playerNames);
        this.cards = cards;
        this.playerCardCounts = playerCardCounts;
        setCurrentPlayerIndex(currentPlayerIndex);
    }

    public ClientGameModel(
            @Nullable List<ICard> cards,
            @NotNull List<Integer> playerCardCounts,
            @NotNull List<String> playerNames,
            int currentPlayerIndex) {
        super(playerNames);
        this.cards = cards;
        this.playerCardCounts = playerCardCounts;
        setCurrentPlayerIndex(currentPlayerIndex);
    }

    @Override
    public int getCardAmount(int playerIndex) {
        return playerCardCounts.get(playerIndex);
    }

    @NotNull
    public List<ICard> getCards() {
        if (cards == null) throw new IllegalStateException("Cards not found (are you spectating?)");
        return cards;
    }
}
