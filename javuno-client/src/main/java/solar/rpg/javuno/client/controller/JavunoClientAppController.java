package solar.rpg.javuno.client.controller;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.client.views.MainView;
import solar.rpg.javuno.client.views.ServerConnectView;
import solar.rpg.javuno.mvc.IController;
import solar.rpg.javuno.mvc.IView;
import solar.rpg.javuno.mvc.JMVC;

import java.io.IOException;
import java.util.logging.Logger;

public class JavunoClientAppController implements IController {

    @NotNull
    private final JMVC<MainView, JavunoClientAppController> mvc;
    @NotNull
    private final Logger logger;
    @NotNull
    private final JavunoClientConnectionController connectionController;

    public JavunoClientAppController(@NotNull Logger logger) {
        this.mvc = new JMVC<>();
        this.logger = logger;
        connectionController = new JavunoClientConnectionController(logger);
    }

    @Override
    @NotNull
    public JMVC<MainView, JavunoClientAppController> getMVC() {
        return mvc;
    }

    @NotNull
    public JavunoClientConnectionController getConnectionController() {
        return connectionController;
    }
}
