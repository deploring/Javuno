package solar.rpg.javuno.models.packets.out;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.cards.ICard;
import solar.rpg.jserver.packet.JServerPacket;

import java.util.List;


public class JavunoPacketOutGameStart extends JServerPacket {

    @NotNull
    private final List<String> gamePlayerNames;
    @NotNull
    private final List<ICard> startingCards;
    private final int startingIndex;

    public JavunoPacketOutGameStart(
            @NotNull List<String> gamePlayerNames,
            @NotNull List<ICard> startingCards,
            int startingIndex) {
        this.gamePlayerNames = gamePlayerNames;
        this.startingCards = startingCards;
        this.startingIndex = startingIndex;
    }

    @NotNull
    public List<String> getGamePlayerNames() {
        return gamePlayerNames;
    }

    @NotNull
    public List<ICard> getStartingCards() {
        return startingCards;
    }

    public int getStartingIndex() {
        return startingIndex;
    }
}
