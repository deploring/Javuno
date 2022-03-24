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
 * {@code ViewMain} acts as the primary view of the application, which is responsible for creating, maintaining, and
 * linking all controllers & sub-views. Using a {@link JSplitPane}, the {@link ViewInformation} sub-view is displayed on
 * the left pane; various other sub-views are displayed on the right pane.
 *
 * @author jskinner
 * @since 1.0.0
 */
public class ViewMain implements IView {

    @NotNull
    private final JavunoClientMVC<ViewMain, ClientAppController> mvc;
    @NotNull
    private final ViewServerConnect viewServerConnect;
    @NotNull
    private final ViewInformation viewInformation;
    @NotNull
    private final ViewLobby viewLobby;
    @NotNull
    private final ViewGame viewGame;

    private JPanel rootPanel;
    private JPanel mainPanel;
    private JPanel informationPanel;
    private JSplitPane contentSplitPane;

    private JMenuBar menuBar;
    private JMenuItem menuItemDisconnect;

    public ViewMain(@NotNull Logger logger) {
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

        informationPanel.add(viewInformation.getPanel(), BorderLayout.CENTER);
        contentSplitPane.setDividerLocation((int) viewInformation.getPanel().getMinimumSize().getWidth());
        showView(viewServerConnect);

        mvc.logClientEvent(
            "Hello, welcome to <strong>Javuno</strong>. To get started, please enter the connection details of a " +
                "server. You can also host this server yourself. Check the README for more information."
        );

        //frame.onDisconnected(false);

        generateMenu();
    }

    /* Server Events */

    public void onConnected() {
        viewInformation.onConnected();
        if (mvc.getController().getGameController().getGameLobbyModel().isInGame()) {
            viewGame.onJoinGame();
            showView(viewGame);
        } else {
            viewLobby.onShowLobby();
            showView(viewLobby);
        }

        menuItemDisconnect.setEnabled(true);
    }

    //FIXME: Probably don't need notify if we don't need to call it with false at the start of the program.
    public void onDisconnected(boolean notify) {
        viewInformation.onDisconnected();
        viewServerConnect.onDisconnected(notify);
        viewGame.getMVC().getController().onDisconnected();
        showView(viewServerConnect);

        menuItemDisconnect.setEnabled(false);
    }

    /* UI Manipulation */

    private void showView(IView view) {
        mainPanel.removeAll();
        mainPanel.add(view.getPanel(), BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void onDisconnectExecute() {
        viewServerConnect.getMVC().getController().close();
    }

    private void generateMenu() {
        menuBar = new JMenuBar();
        JMenu actionMenu = new JMenu("Action");
        menuItemDisconnect = new JMenuItem("Disconnect");
        menuItemDisconnect.addActionListener((e) -> onDisconnectExecute());
        actionMenu.add(menuItemDisconnect);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem menuItemAbout = new JMenuItem("About");
        helpMenu.add(menuItemAbout);

        menuBar.add(actionMenu);
        menuBar.add(helpMenu);

        //mainPanel.setMinimumSize(new Dimension(600, 700));
        //mainPanel.setPreferredSize(mainPanel.getMinimumSize());
    }

    /* MVC */

    @NotNull
    public ViewInformation getViewInformation() {
        return viewInformation;
    }

    @NotNull
    public JMenuBar getMenuBar() {
        return menuBar;
    }

    @NotNull
    @Override
    public JPanel getPanel() {
        return rootPanel;
    }

    @NotNull
    @Override
    public JavunoClientMVC<ViewMain, ClientAppController> getMVC() {
        return mvc;
    }
}
