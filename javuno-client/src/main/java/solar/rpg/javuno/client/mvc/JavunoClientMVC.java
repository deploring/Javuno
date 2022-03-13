package solar.rpg.javuno.client.mvc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import solar.rpg.javuno.client.controller.ClientAppController;
import solar.rpg.javuno.client.views.ViewInformation;
import solar.rpg.javuno.client.views.ViewInformationOld;
import solar.rpg.javuno.mvc.IController;
import solar.rpg.javuno.mvc.IView;
import solar.rpg.javuno.mvc.JMVC;

public class JavunoClientMVC<V extends IView, C extends IController> extends JMVC<V, C> {

    @Nullable
    private ClientAppController appController;

    public ClientAppController getAppController() {
        assert appController != null : "App controller not set";
        return appController;
    }

    public ViewInformation getViewInformation() {
        return getAppController().getMVC().getView().getViewInformation();
    }

    public void logClientEvent(@NotNull String log) {
        getViewInformation().appendEventToLog(log);
    }

    public void set(
            @NotNull V view,
            @NotNull C controller,
            @NotNull ClientAppController appController) {
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
