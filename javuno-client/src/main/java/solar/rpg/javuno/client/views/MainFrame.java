package solar.rpg.javuno.client.views;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.client.controller.ClientAppController;
import solar.rpg.javuno.client.controller.ClientGameController;
import solar.rpg.javuno.client.controller.ConnectionController;
import solar.rpg.javuno.client.mvc.JavunoClientMVC;
import solar.rpg.javuno.mvc.IView;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Logger;

/**
 * {@code MainFrame} acts as the primary view of the application, which is responsible for creating, maintaining, and
 * linking all controllers & sub-views. Using a {@link JSplitPane}, the {@link ViewInformation} sub-view is displayed on
 * the left pane; various other sub-views are displayed on the right pane.
 *
 * @author jskinner
 * @since 1.0.0
 */
public class MainFrame extends JFrame implements IView {

    @NotNull
    private final Logger logger;
    @NotNull
    private final JavunoClientMVC<MainFrame, ClientAppController> mvc;
    @NotNull
    private final ViewServerConnect viewServerConnect;
    @NotNull
    private final ViewInformation viewInformation;
    @NotNull
    private final ViewLobby viewLobby;
    @NotNull
    private final ViewGame viewGame;

    private JMenuItem menuItemDisconnect;
    private JMenuItem menuItemAbout;

    /**
     * Constructs a new {@code MainFrame} instance.
     *
     * @param logger Logger object.
     */
    public MainFrame(@NotNull Logger logger) {
        super("Javuno Client 1.0.0");
        this.logger = logger;

        ClientAppController appController = new ClientAppController(logger);
        mvc = appController.getMVC();
        mvc.set(this, appController, appController);

        JavunoClientMVC<ViewInformation, ClientAppController> informationMVC = mvc.copy();
        viewInformation = new ViewInformation(informationMVC);
        informationMVC.set(viewInformation, appController, appController);

        ConnectionController connectionController = appController.getConnectionController();
        JavunoClientMVC<ViewServerConnect, ConnectionController> serverConnectMVC = connectionController.getMVC();
        viewServerConnect = new ViewServerConnect(serverConnectMVC);
        serverConnectMVC.set(viewServerConnect, connectionController, appController);

        ClientGameController clientGameController = appController.getGameController();
        JavunoClientMVC<ViewLobby, ClientGameController> lobbyMVC = clientGameController.getLobbyMVC();
        viewLobby = new ViewLobby(lobbyMVC);
        lobbyMVC.set(viewLobby, clientGameController, appController);
        JavunoClientMVC<ViewGame, ClientGameController> gameMVC = clientGameController.getMVC();
        viewGame = new ViewGame(gameMVC);
        gameMVC.set(viewGame, clientGameController, appController);

        mainPanel = new JPanel();

        generateUI();
    }
}
