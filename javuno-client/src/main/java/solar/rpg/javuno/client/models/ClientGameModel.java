package solar.rpg.javuno.client.models;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import solar.rpg.javuno.models.cards.ICard;
import solar.rpg.javuno.models.game.AbstractGameModel;
import solar.rpg.javuno.models.game.Direction;

import java.util.List;
import java.util.Stack;

public final class ClientGameModel extends AbstractGameModel {

    @Nullable
    private final List<ICard> clientCards;
    @NotNull
    private final List<Integer> playerCardCounts;

    public ClientGameModel(
            @Nullable List<ICard> clientCards,
            @NotNull Stack<ICard> discardPile,
            @NotNull List<Integer> playerCardCounts,
            @NotNull List<String> gamePlayerNames,
            int currentPlayerIndex,
            @NotNull Direction currentDirection) {
        super(discardPile, gamePlayerNames, currentDirection);
        this.clientCards = clientCards;
        this.playerCardCounts = playerCardCounts;
        setCurrentPlayerIndex(currentPlayerIndex);
    }

    @Override
    public int getCardAmount(int playerIndex) {
        return playerCardCounts.get(playerIndex);
    }

    public boolean isParticipating() {
        return clientCards != null;
    }

    @NotNull
    public List<ICard> getClientCards() {
        if (clientCards == null) throw new IllegalStateException("Cards not found (are you spectating?)");
        return clientCards;
    }
}
