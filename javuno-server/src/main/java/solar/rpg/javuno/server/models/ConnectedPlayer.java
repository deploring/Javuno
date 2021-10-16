package solar.rpg.javuno.server.models;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.IPlayer;
import solar.rpg.javuno.models.cards.ICard;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class ConnectedPlayer implements IPlayer {

    @NotNull
    private final String name;
    @NotNull
    private final InetSocketAddress originAddress;

    public ConnectedPlayer(@NotNull String name, @NotNull InetSocketAddress originAddress) {
        this.name = name;
        this.originAddress = originAddress;
    }

    @NotNull
    public InetSocketAddress getOriginAddress() {
        return originAddress;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }
}
