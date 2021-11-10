package solar.rpg.javuno.models.game;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.game.AbstractGamePlayer;

public final class ClientGamePlayer extends AbstractGamePlayer {

    private int cardCount;

    public ClientGamePlayer(@NotNull String name, boolean uno, int cardCount) {
        super(name, uno);
        setCardCount(cardCount);
    }

    public void setCardCount(int cardCount) {
        this.cardCount = cardCount;
    }

    @Override
    public int getCardCount() {
        return cardCount;
    }
}
