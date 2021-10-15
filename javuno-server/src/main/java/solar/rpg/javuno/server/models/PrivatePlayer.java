package solar.rpg.javuno.server.models;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.IPlayer;
import solar.rpg.javuno.models.cards.ICard;

import java.net.InetSocketAddress;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public class PrivatePlayer implements IPlayer {

    @NotNull
    private final String name;
    @NotNull
    private final InetSocketAddress originAddress;
    @NotNull
    private final List<ICard> hand;

    public PrivatePlayer(
            @NotNull String name,
            @NotNull InetSocketAddress originAddress) {
        this.name = name;
        this.originAddress = originAddress;
        this.hand = new ArrayList<>();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getHandAmount() {
        return hand.size();
    }

    @Override
    public PlayerState getState() {
        return null;
    }
}
