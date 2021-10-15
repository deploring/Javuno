package solar.rpg.javuno.server.controllers;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.mvc.IController;
import solar.rpg.javuno.mvc.JMVC;
import solar.rpg.javuno.server.views.MainFrame;

import java.util.logging.Logger;

public class AppController implements IController {

    @NotNull
    private final Logger logger;
    @NotNull
    private final JMVC<MainFrame, AppController> mvc;
    @NotNull
    private final HostController serverHostController;

    public AppController(@NotNull Logger logger) {
        this.logger = logger;
        mvc = new JMVC<>();

        serverHostController = new HostController(logger);
    }

    @Override
    @NotNull
    public JMVC<MainFrame, AppController> getMVC() {
        return mvc;
    }

    @NotNull
    public HostController getServerHostController() {
        return serverHostController;
    }
}
