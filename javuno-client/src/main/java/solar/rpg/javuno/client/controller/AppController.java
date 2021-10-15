package solar.rpg.javuno.client.controller;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.client.mvc.JavunoClientMVC;
import solar.rpg.javuno.client.views.MainFrame;
import solar.rpg.javuno.mvc.IController;

import java.util.logging.Logger;

public class AppController implements IController {

    @NotNull
    private final JavunoClientMVC<MainFrame, AppController> mvc;
    @NotNull
    private final Logger logger;
    @NotNull
    private final ConnectionController connectionController;

    public AppController(@NotNull Logger logger) {
        this.mvc = new JavunoClientMVC<>();
        this.logger = logger;
        connectionController = new ConnectionController(logger);
    }

    public boolean hasActiveConnection() {
        return connectionController.isValid();
    }

    @Override
    @NotNull
    public JavunoClientMVC<MainFrame, AppController> getMVC() {
        return mvc;
    }

    @NotNull
    public ConnectionController getConnectionController() {
        return connectionController;
    }
}
