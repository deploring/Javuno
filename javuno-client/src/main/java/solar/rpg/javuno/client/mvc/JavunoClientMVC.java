package solar.rpg.javuno.client.mvc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import solar.rpg.javuno.client.controller.AppController;
import solar.rpg.javuno.mvc.IController;
import solar.rpg.javuno.mvc.IView;
import solar.rpg.javuno.mvc.JMVC;
import solar.rpg.jserver.packet.JServerPacket;

import java.io.IOException;

public class JavunoClientMVC<V extends IView, C extends IController> extends JMVC<V, C> {

    @Nullable
    private AppController appController;

    public void writePacket(@NotNull JServerPacket packetToWrite) {
        if (appController == null) throw new IllegalStateException("App controller not set");
        if (!appController.getConnectionController().isValid())
            throw new IllegalStateException("Connection to server is not valid");
        try {
            appController.getConnectionController().getClientConnection().writePacket(packetToWrite);
        } catch (IOException ignored) {
        }
    }

    public void logClientEvent(@NotNull String log) {
        if (appController == null) throw new IllegalStateException("App controller not set");
        appController.getMVC().getView().getViewInformation().appendEventToLog(log);
    }

    public void setChatEnabled(boolean enabled) {
        if (appController == null) throw new IllegalStateException("App controller not set");
        appController.getMVC().getView().getViewInformation().setChatEnabled(enabled);
    }

    public void set(
            @NotNull V view,
            @NotNull C controller,
            @NotNull AppController appController) {
        set(view, controller);
        this.appController = appController;
    }

    @NotNull
    public <nV extends IView, nC extends IController> JavunoClientMVC<nV, nC> copy() {
        JavunoClientMVC<nV, nC> result = new JavunoClientMVC<>();
        result.appController = this.appController;
        return result;
    }
}
