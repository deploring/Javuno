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
 * the left pane; various other sub-views are displayed on the right pane. {@code MainView} is responsible for
 * coordinating this.
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
    private final ViewGame viewGame;
    @NotNull
    private final JPanel mainPanel;

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
        JavunoClientMVC<ViewGame, ClientGameController> gameMVC = clientGameController.getMVC();
        viewGame = new ViewGame(gameMVC);
        gameMVC.set(viewGame, clientGameController, appController);

        mainPanel = new JPanel();
        generateUI();

        showView(ViewType.SERVER_CONNECT);
        mvc.logClientEvent(
                "> Hello, welcome to Javuno. To get started, please enter the connection details of a" +
                "server. You can also host this server yourself. Check the README for more information.");
    }

    public void showView(ViewType viewType) {
        switch (viewType) {
            case SERVER_CONNECT -> swapPanel(viewServerConnect.getPanel());
            case GAME_LOBBY -> swapPanel(viewGame.getPanel());
        }
    }

    private void swapPanel(JPanel panel) {
        mainPanel.removeAll();
        mainPanel.add(panel, BorderLayout.CENTER);
        revalidate();
        repaint();
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public void generateUI() {
        JMenuBar menuBar = new JMenuBar();
        JMenu actionMenu = new JMenu("Action");
        JMenu helpMenu = new JMenu("Help");
        JMenuItem itemAbout = new JMenuItem("About");
        helpMenu.add(itemAbout);

        menuBar.add(actionMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);

        mainPanel.setLayout(new BorderLayout());
        mainPanel.setMinimumSize(new Dimension(600, 700));
        mainPanel.setPreferredSize(mainPanel.getMinimumSize());

        getContentPane().setLayout(new BorderLayout());
        JSplitPane contentSplitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                viewInformation.getPanel(),
                mainPanel);
        contentSplitPane.setDividerLocation(300);
        getContentPane().add(contentSplitPane, BorderLayout.CENTER);
    }

    @NotNull
    public ViewInformation getViewInformation() {
        return viewInformation;
    }

    @Override
    public JPanel getPanel() {
        return mainPanel;
    }

    @NotNull
    @Override
    public JavunoClientMVC<MainFrame, ClientAppController> getMVC() {
        return mvc;
    }

    /**
     * Denotes all the different views that can be shown inside {@code MainView}.
     */
    public enum ViewType {
        SERVER_CONNECT,
        GAME_LOBBY
    }
}
