package solar.rpg.javuno.server.controllers;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.mvc.IController;
import solar.rpg.javuno.mvc.JMVC;
import solar.rpg.javuno.server.views.MainView;

import java.util.logging.Logger;

public class JavunoServerAppController implements IController {

    @NotNull
    private final Logger logger;
    @NotNull
    private final JMVC<MainView, JavunoServerAppController> mvc;
    @NotNull
    private final JavunoServerHostController serverHostController;

    public JavunoServerAppController(@NotNull Logger logger) {
        this.logger = logger;
        mvc = new JMVC<>();

        serverHostController = new JavunoServerHostController(logger);
    }

    @Override
    @NotNull
    public JMVC<MainView, JavunoServerAppController> getMVC() {
        return mvc;
    }

    @NotNull
    public JavunoServerHostController getServerHostController() {
        return serverHostController;
    }
}
