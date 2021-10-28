package solar.rpg.javuno.server.controllers;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.packets.out.JavunoPacketOutPlayerDisconnect;
import solar.rpg.javuno.mvc.IController;
import solar.rpg.javuno.mvc.JMVC;
import solar.rpg.javuno.server.models.JavunoServerPacketValidatorHandler;
import solar.rpg.javuno.server.models.ServerGameLobbyModel;
import solar.rpg.javuno.server.views.MainFrame;

import java.net.InetSocketAddress;
import java.util.logging.Logger;

public class ServerGameController implements IController {

    @NotNull
    private final Logger logger;
    @NotNull
    private final JMVC<MainFrame, ServerGameController> mvc;
    @NotNull
    private final ServerGameLobbyModel gameLobbyModel;
    @NotNull
    private final JavunoServerPacketValidatorHandler packetHandler;

    public ServerGameController(@NotNull Logger logger) {
        this.logger = logger;
        mvc = new JMVC<>();
        gameLobbyModel = new ServerGameLobbyModel();
        packetHandler = new JavunoServerPacketValidatorHandler(mvc);
    }

    /* I/O Events */

    public void handleDisconnect(@NotNull InetSocketAddress originAddress) {
        String oldPlayerName = gameLobbyModel.getPlayerName(originAddress);
        gameLobbyModel.removePlayer(originAddress);
        getHostController().getServerHost().writePacketAll(new JavunoPacketOutPlayerDisconnect(oldPlayerName));
    }

    /* Field Getters/Setters */

    @NotNull
    public JavunoServerPacketValidatorHandler getPacketHandler() {
        return packetHandler;
    }

    @NotNull
    public ServerGameLobbyModel getGameLobbyModel() {
        return gameLobbyModel;
    }

    /* MVC */

    @NotNull
    private HostController getHostController() {
        return mvc.getView().getMVC().getController().getHostController();
    }

    @Override
    public JMVC<MainFrame, ServerGameController> getMVC() {
        return mvc;
    }
}
