package solar.rpg.javuno.server.views;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.mvc.IView;
import solar.rpg.javuno.mvc.JMVC;
import solar.rpg.javuno.server.controllers.JavunoServerAppController;
import solar.rpg.javuno.server.controllers.JavunoServerHostController;

import javax.swing.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

public class MainView extends JFrame implements IView {

    @NotNull
    private final Logger logger;
    @NotNull
    private final JPanel mainPanel;
    @NotNull
    private final JMVC<MainView, JavunoServerAppController> mvc;

    public MainView(@NotNull Logger logger) {
        super("Javuno Server 1.0.0");
        this.logger = logger;
        mainPanel = new JPanel();

        JavunoServerAppController appController = new JavunoServerAppController(logger);
        mvc = appController.getMVC();
        mvc.set(this, appController);

        JavunoServerHostController serverHostController = appController.getServerHostController();
        JMVC<MainView, JavunoServerHostController> hostControllerMVC = serverHostController.getMVC();
        hostControllerMVC.set(this, serverHostController);

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
    public void reset() {

    }

    @Override
    public JPanel getPanel() {
        return null;
    }

    @NotNull
    @Override
    public JMVC<MainView, JavunoServerAppController> getMVC() {
        return mvc;
    }
}
