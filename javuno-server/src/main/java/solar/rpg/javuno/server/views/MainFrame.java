package solar.rpg.javuno.server.views;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.mvc.IView;
import solar.rpg.javuno.mvc.JMVC;
import solar.rpg.javuno.server.controllers.ServerAppController;
import solar.rpg.javuno.server.controllers.HostController;
import solar.rpg.javuno.server.controllers.ServerGameController;

import javax.swing.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

public class MainFrame extends JFrame implements IView {

    @NotNull
    private final Logger logger;
    @NotNull
    private final JPanel mainPanel;
    @NotNull
    private final JMVC<MainFrame, ServerAppController> mvc;

    public MainFrame(@NotNull Logger logger) {
        super("Javuno Server 1.0.0");
        this.logger = logger;
        mainPanel = new JPanel();

        ServerAppController appController = new ServerAppController(logger);
        mvc = appController.getMVC();
        mvc.set(this, appController);

        HostController serverHostController = appController.getHostController();
        serverHostController.getMVC().set(this, serverHostController);

        ServerGameController serverGameController = appController.getGameController();
        serverGameController.getMVC().set(this, serverGameController);

        try {
            serverHostController.startHost(InetAddress.getLocalHost(), 25565);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    @Override
    public void generateUI() {

    }

    @Override
    public JPanel getPanel() {
        return null;
    }

    @NotNull
    @Override
    public JMVC<MainFrame, ServerAppController> getMVC() {
        return mvc;
    }
}
