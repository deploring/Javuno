package solar.rpg.javuno.server.controllers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import solar.rpg.javuno.model.packets.JavunoPacketInServerConnect;
import solar.rpg.javuno.model.packets.JavunoPacketOutConnectionAccepted;
import solar.rpg.javuno.model.packets.JavunoPacketOutConnectionRejected;
import solar.rpg.javuno.model.packets.JavunoPacketOutConnectionRejected.ConnectionRejectionReason;
import solar.rpg.javuno.mvc.IController;
import solar.rpg.javuno.mvc.JMVC;
import solar.rpg.javuno.server.views.MainView;
import solar.rpg.jserver.connection.handlers.packet.JServerHost;
import solar.rpg.jserver.packet.JServerPacket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavunoServerHostController implements IController {

    @NotNull
    private final JMVC<MainView, JavunoServerHostController> mvc;
    @NotNull
    private final Logger logger;
    @NotNull
    private final ExecutorService executor;
    @Nullable
    private JavunoServerHost serverHost;
    @NotNull
    private String serverPassword = "ABC123";

    public JavunoServerHostController(@NotNull Logger logger) {
        this.mvc = new JMVC<>();
        this.logger = logger;
        executor = Executors.newFixedThreadPool(1);
    }

    public void handleIncomingConnection(
            @NotNull InetSocketAddress originAddress,
            @NotNull JavunoPacketInServerConnect connectPacket) {
        assert serverHost != null : "Server host does not exist";

        logger.log(Level.INFO, "Handling connection packet");

        boolean closeSocket = false;
        JServerPacket packetToWrite;
        if (!serverPassword.isEmpty() && !serverPassword.equals(connectPacket.getServerPassword())) {
            packetToWrite = new JavunoPacketOutConnectionRejected(ConnectionRejectionReason.INCORRECT_PASSWORD);
            closeSocket = true;
        } else if (connectPacket.getPlayerName().equals("Joshua")) {
            packetToWrite = new JavunoPacketOutConnectionRejected(ConnectionRejectionReason.USERNAME_ALREADY_TAKEN);
            closeSocket = true;
        } else {
            packetToWrite = new JavunoPacketOutConnectionAccepted();
        }

        try {
            serverHost.writePacket(originAddress, packetToWrite);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Unable to handle incoming connection from %s: %s");
            closeSocket = true;
        } finally {
            if (closeSocket) serverHost.closeSocket(originAddress);
        }
    }

    public void startHost(InetAddress bindAddr, int port) {
        assert serverHost == null : "Server host is already active";
        try {
            serverHost = new JavunoServerHost(bindAddr, port, executor, logger);
        } catch (IOException e) {
            getMVC().getView().showErrorDialog(
                    "Unable to establish server host",
                    String.format("Could not establish server host on %s:%s:\n%s", bindAddr, port, e.getMessage()));
            System.exit(0);
        }
    }

    @Override
    public JMVC<MainView, JavunoServerHostController> getMVC() {
        return mvc;
    }

    private final class JavunoServerHost extends JServerHost {

        public JavunoServerHost(
                @NotNull InetAddress bindAddr,
                int port,
                @NotNull ExecutorService executor,
                @NotNull Logger logger) throws IOException {
            super(bindAddr, port, executor, logger);
        }

        @Override
        public void onNewConnection(@NotNull InetSocketAddress originAddress) {

        }

        @Override
        public void onSocketClosed(@NotNull InetSocketAddress originAddress) {

        }

        @Override
        public void onPacketReceived(@NotNull InetSocketAddress originAddress, @NotNull JServerPacket packet) {
            if (packet instanceof JavunoPacketInServerConnect)
                handleIncomingConnection(originAddress, (JavunoPacketInServerConnect) packet);
        }
    }
}
