package solar.rpg.javuno.server.models;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.cards.ICard;
import solar.rpg.javuno.models.game.AbstractGamePlayer;

import java.util.ArrayList;
import java.util.List;

public final class ServerGamePlayer extends AbstractGamePlayer {

    @NotNull
    private final List<ICard> cards;

    public ServerGamePlayer(@NotNull String name) {
        super(name, false);
        this.cards = new ArrayList<>();
    }

    @Override
    public int getCardCount() {
        return cards.size();
    }

    @NotNull
    public List<ICard> getCards() {
        return cards;
    }
}
