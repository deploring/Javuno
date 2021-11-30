package solar.rpg.javuno.client.controller;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.client.mvc.JavunoClientMVC;
import solar.rpg.javuno.client.views.MainFrame;
import solar.rpg.javuno.mvc.IController;

import java.util.logging.Logger;

/**
 * Main controller for the Javuno app. It serves as a link between all client controller instances, along with being
 * linked to the {@link MainFrame} via an MVC object.
 *
 * @author jskinner
 * @since 1.0.0
 */
public class ClientAppController implements IController {

    @NotNull
    private final JavunoClientMVC<MainFrame, ClientAppController> mvc;
    @NotNull
    private final Logger logger;
    @NotNull
    private final ConnectionController connectionController;
    @NotNull
    private final ClientGameController clientGameController;

    /**
     * Constructs a new {@code ClientAppController} instance.
     *
     * @param logger Logger object.
     */
    public ClientAppController(@NotNull Logger logger) {
        this.mvc = new JavunoClientMVC<>();
        this.logger = logger;
        connectionController = new ConnectionController(logger);
        clientGameController = new ClientGameController(logger);
    }

    public boolean hasActiveConnection() {
        return connectionController.isValid();
    }

    /**
     * @return Instance of the connection controller.
     */
    @NotNull
    public ConnectionController getConnectionController() {
        return connectionController;
    }

    /**
     * @return Instance of the game controller.
     */
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
