package solar.rpg.javuno.client.controller;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.client.views.JavunoClientMVC;
import solar.rpg.javuno.client.views.MainView;
import solar.rpg.javuno.mvc.IController;
import solar.rpg.javuno.mvc.JMVC;

import java.util.logging.Logger;

public class JavunoClientAppController implements IController {

    @NotNull
    private final JavunoClientMVC<MainView, JavunoClientAppController> mvc;
    @NotNull
    private final Logger logger;
    @NotNull
    private final JavunoClientConnectionController connectionController;

    public JavunoClientAppController(@NotNull Logger logger) {
        this.mvc = new JavunoClientMVC<>();
        this.logger = logger;
        connectionController = new JavunoClientConnectionController(logger);
    }

    @Override
    @NotNull
    public JavunoClientMVC<MainView, JavunoClientAppController> getMVC() {
        return mvc;
    }

    @NotNull
    public JavunoClientConnectionController getConnectionController() {
        return connectionController;
    }
}
