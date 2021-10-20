package solar.rpg.javuno.server.controllers;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.packets.JavunoPacketInOutChatMessage;
import solar.rpg.javuno.models.packets.JavunoPacketInServerConnect;
import solar.rpg.javuno.models.packets.JavunoPacketOutConnectionAccepted;
import solar.rpg.javuno.models.packets.JavunoPacketOutConnectionRejected;
import solar.rpg.javuno.mvc.IController;
import solar.rpg.javuno.mvc.JMVC;
import solar.rpg.javuno.server.models.ServerGameLobbyModel;
import solar.rpg.javuno.server.views.MainFrame;
import solar.rpg.jserver.connection.handlers.packet.JServerHost;
import solar.rpg.jserver.packet.JServerPacket;

import java.net.InetSocketAddress;

public class ServerGameController implements IController {

    @NotNull
    private final JMVC<MainFrame, ServerGameController> mvc;
    @NotNull
    private final ServerGameLobbyModel gameLobbyModel;

    public ServerGameController() {
        mvc = new JMVC<>();
        gameLobbyModel = new ServerGameLobbyModel();
    }

    public void handleGamePacket(JServerPacket packet) {
        if (packet instanceof JavunoPacketInServerConnect)
            handleIncomingConnection((JavunoPacketInServerConnect) packet);
        else if (packet instanceof JavunoPacketInOutChatMessage)
            handleIncomingChat((JavunoPacketInOutChatMessage) packet);
    }

    private void handleIncomingConnection(@NotNull JavunoPacketInServerConnect connectPacket) {
        HostController hostController = getHostController();
        String serverPassword = hostController.getServerPassword();
        String playerName = connectPacket.getPlayerName();
        InetSocketAddress originAddress = connectPacket.getOriginAddress();

        boolean closeSocket = false;
        JServerPacket packetToWrite;
        if (!serverPassword.isEmpty() && !serverPassword.equals(connectPacket.getServerPassword())) {
            packetToWrite = new JavunoPacketOutConnectionRejected(JavunoPacketOutConnectionRejected.ConnectionRejectionReason.INCORRECT_PASSWORD);
            closeSocket = true;
        } else if (gameLobbyModel.doesPlayerExist(playerName)) {
            packetToWrite = new JavunoPacketOutConnectionRejected(JavunoPacketOutConnectionRejected.ConnectionRejectionReason.USERNAME_ALREADY_TAKEN);
            closeSocket = true;
        } else {
            packetToWrite = new JavunoPacketOutConnectionAccepted();
            gameLobbyModel.addPlayer(playerName, originAddress);
        }

        JServerHost serverHost = hostController.getServerHost();
        serverHost.writePacket(connectPacket.getOriginAddress(), packetToWrite);
        if (closeSocket) serverHost.closeSocket(originAddress);
    }

    /**
     * Sends an incoming chat packet back out to all clients, except for the sender.
     *
     * @param chatPacket Chat packet to distribute.
     */
    private void handleIncomingChat(@NotNull JavunoPacketInOutChatMessage chatPacket) {
        gameLobbyModel.getAddressesExcept(chatPacket.getOriginAddress()).forEach(
                (originAddress) -> getHostController().getServerHost().writePacket(originAddress, chatPacket));
    }

    @NotNull
    public ServerGameLobbyModel getGameLobbyModel() {
        return gameLobbyModel;
    }

    @NotNull
    private HostController getHostController() {
        return mvc.getView().getMVC().getController().getHostController();
    }

    @Override
    public JMVC<MainFrame, ServerGameController> getMVC() {
        return mvc;
    }
}
