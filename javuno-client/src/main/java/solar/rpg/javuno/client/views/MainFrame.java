package solar.rpg.javuno.client.views;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.client.controller.AppController;
import solar.rpg.javuno.client.controller.ConnectionController;
import solar.rpg.javuno.client.mvc.JavunoClientMVC;
import solar.rpg.javuno.mvc.IView;
import solar.rpg.jserver.packet.JServerPacket;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class MainFrame extends JFrame implements IView {

    @NotNull
    private final Logger logger;
    @NotNull
    private final JavunoClientMVC<MainFrame, AppController> mvc;
    @NotNull
    private final ViewServerConnect viewServerConnect;
    @NotNull
    private final ViewInformation viewInformation;
    @NotNull
    private final JPanel mainPanel;

    public MainFrame(@NotNull Logger logger) {
        super("Javuno Client 1.0.0");
        this.logger = logger;

        AppController appController = new AppController(logger);
        Consumer<JServerPacket> outgoingPacketConsumer = appController.getConnectionController();

        JavunoClientMVC<ViewInformation, AppController> informationMVC = new JavunoClientMVC<>();
        viewInformation = new ViewInformation(informationMVC);
        informationMVC.set(viewInformation, appController, viewInformation, outgoingPacketConsumer);

        mvc = appController.getMVC();
        mvc.set(this, appController, viewInformation, null);

        ConnectionController connectionController = appController.getConnectionController();
        JavunoClientMVC<ViewServerConnect, ConnectionController> serverConnectMVC = connectionController.getMVC();
        viewServerConnect = new ViewServerConnect(serverConnectMVC);
        serverConnectMVC.set(viewServerConnect, connectionController, viewInformation, null);

        mainPanel = new JPanel();
        generateUI();

        showView(ViewType.SERVER_CONNECT);
        getMVC().logClientEvent(
                "> Hello, welcome to Javuno. To get started, please enter the connection details " +
                "of a host/server. You can also host this server yourself. Check the README for more information.");
    }

    public void showView(ViewType viewType) {
        switch (viewType) {
            case SERVER_CONNECT -> swapPanel(viewServerConnect.getPanel());
            case MAIN_GAME -> throw new IllegalCallerException();
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

    @Override
    public JPanel getPanel() {
        return mainPanel;
    }

    @NotNull
    @Override
    public JavunoClientMVC<MainFrame, AppController> getMVC() {
        return mvc;
    }

    /**
     * Denotes all the different views that can be shown inside {@code MainView}.
     */
    public enum ViewType {
        SERVER_CONNECT,
        MAIN_GAME
    }
}
