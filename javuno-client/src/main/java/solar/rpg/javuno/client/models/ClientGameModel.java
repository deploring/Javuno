package solar.rpg.javuno.client.models;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import solar.rpg.javuno.models.cards.ICard;
import solar.rpg.javuno.models.game.AbstractGameModel;
import solar.rpg.javuno.models.game.ClientOpponent;
import solar.rpg.javuno.models.game.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public final class ClientGameModel extends AbstractGameModel<ClientOpponent> {

    @Nullable
    private final List<ICard> clientCards;

    public ClientGameModel(
            @Nullable List<ICard> clientCards,
            @NotNull Stack<ICard> discardPile,
            @NotNull List<ClientOpponent> players,
            int currentPlayerIndex,
            @NotNull Direction currentDirection,
            @NotNull GameState gameState,
            @NotNull UnoChallengeState unoChallengeState) {
        super(discardPile, players, currentDirection, gameState, unoChallengeState);
        this.clientCards = clientCards;
        setCurrentPlayerIndex(currentPlayerIndex);
    }

    public int getCardAmount(int playerIndex) {
        return getPlayers().get(playerIndex).getCardCount();
    }

    public boolean isParticipating() {
        return clientCards != null;
    }

    @NotNull
    public List<ICard> getClientCards() {
        if (clientCards == null) throw new IllegalStateException("Cards not found (are you spectating?)");
        return new ArrayList<>(clientCards);
    }

    public void addCards(@NotNull List<ICard> cards) {
        if (clientCards == null) throw new IllegalStateException("Cards not found (are you spectating?)");
        clientCards.addAll(cards);
    }

    public void removeClientCard(int cardIndex) {
        if (clientCards == null) throw new IllegalStateException("Cards not found (are you spectating?)");
        clientCards.remove(cardIndex);
    }
}
