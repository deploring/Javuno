package solar.rpg.javuno.server.controllers;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.packets.JavunoPacketInOutChatMessage;
import solar.rpg.javuno.models.packets.JavunoPacketInServerConnect;
import solar.rpg.javuno.mvc.IController;
import solar.rpg.javuno.mvc.JMVC;
import solar.rpg.javuno.server.models.ServerGameLobbyModel;
import solar.rpg.javuno.server.views.MainFrame;
import solar.rpg.jserver.packet.JServerPacket;

import java.util.logging.Logger;

public class ServerAppController implements IController {

    @NotNull
    private final Logger logger;
    @NotNull
    private final JMVC<MainFrame, ServerAppController> mvc;
    @NotNull
    private final HostController serverHostController;
    @NotNull
    private final ServerGameController serverGameController;

    public ServerAppController(@NotNull Logger logger) {
        this.logger = logger;
        mvc = new JMVC<>();

        serverHostController = new HostController(logger);
        serverGameController = new ServerGameController(logger);
    }

    @NotNull
    public HostController getHostController() {
        return serverHostController;
    }

    @NotNull
    public ServerGameController getGameController(){
        return serverGameController;
    }

    @Override
    @NotNull
    public JMVC<MainFrame, ServerAppController> getMVC() {
        return mvc;
    }
}
