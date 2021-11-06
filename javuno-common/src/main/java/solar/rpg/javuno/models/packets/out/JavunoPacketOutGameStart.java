package solar.rpg.javuno.models.packets.out;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.cards.ICard;
import solar.rpg.jserver.packet.JServerPacket;

import java.util.List;


public class JavunoPacketOutGameStart extends JServerPacket {

    @NotNull
    private final List<ICard> startingCards;
    @NotNull
    private final List<Integer> playerCardCounts;
    @NotNull
    private final List<String> gamePlayerNames;
    private final int startingIndex;

    public JavunoPacketOutGameStart(
            @NotNull List<ICard> startingCards,
            @NotNull List<Integer> playerCardCounts,
            @NotNull List<String> gamePlayerNames,
            int startingIndex) {
        this.startingCards = startingCards;
        this.playerCardCounts = playerCardCounts;
        this.gamePlayerNames = gamePlayerNames;
        this.startingIndex = startingIndex;
    }

    @NotNull
    public List<ICard> getStartingCards() {
        return startingCards;
    }

    @NotNull
    public List<Integer> getPlayerCardCounts() {
        return playerCardCounts;
    }

    @NotNull
    public List<String> getGamePlayerNames() {
        return gamePlayerNames;
    }

    public int getStartingIndex() {
        return startingIndex;
    }
}
