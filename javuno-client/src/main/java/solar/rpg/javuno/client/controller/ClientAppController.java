package solar.rpg.javuno.client.controller;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.client.mvc.JavunoClientMVC;
import solar.rpg.javuno.client.views.MainFrame;
import solar.rpg.javuno.mvc.IController;

import java.util.logging.Logger;

public class ClientAppController implements IController {

    @NotNull
    private final JavunoClientMVC<MainFrame, ClientAppController> mvc;
    @NotNull
    private final Logger logger;
    @NotNull
    private final ConnectionController connectionController;
    @NotNull
    private final ClientGameController clientGameController;

    public ClientAppController(@NotNull Logger logger) {
        this.mvc = new JavunoClientMVC<>();
        this.logger = logger;
        connectionController = new ConnectionController(logger);
        clientGameController = new ClientGameController(logger);
    }

    public boolean hasActiveConnection() {
        return connectionController.isValid();
    }

    @NotNull
    public ConnectionController getConnectionController() {
        return connectionController;
    }

    @NotNull
    public ClientGameController getGameController() {
        return clientGameController;
    }

    @Override
    @NotNull
    public JavunoClientMVC<MainFrame, ClientAppController> getMVC() {
        return mvc;
    }
}
