package solar.rpg.javuno.client.mvc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import solar.rpg.javuno.client.views.ViewInformation;
import solar.rpg.javuno.mvc.IController;
import solar.rpg.javuno.mvc.IView;
import solar.rpg.javuno.mvc.JMVC;
import solar.rpg.jserver.packet.JServerPacket;

import java.util.function.Consumer;

public class JavunoClientMVC<V extends IView, C extends IController> extends JMVC<V, C> {

    @Nullable
    private Consumer<JServerPacket> outgoingPacketConsumer;
    @Nullable
    private ViewInformation viewInformation;

    public void writePacket(@NotNull JServerPacket packetToWrite) {
        assert outgoingPacketConsumer != null : "Packet consumer does not exist";
        outgoingPacketConsumer.accept(packetToWrite);
    }

    public void logClientEvent(@NotNull String log) {
        assert viewInformation != null : "Information view does not exist";
        viewInformation.appendEventToLog(log);
    }

    public void setChatEnabled(boolean enabled) {
        assert viewInformation != null : "Information view does not exist";
        viewInformation.setChatEnabled(enabled);
    }

    public void set(
            @NotNull V view,
            @NotNull C controller,
            @Nullable ViewInformation viewInformation,
            @Nullable Consumer<JServerPacket> outgoingPacketConsumer) {
        this.viewInformation = viewInformation;
        this.outgoingPacketConsumer = outgoingPacketConsumer;
        set(view, controller);
    }
}
